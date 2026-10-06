package com.alessandro.tedesco.data.remote

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data object UpToDate : UpdateState()
    data class Available(val version: VersionInfo) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String, val retryable: Boolean = true) : UpdateState()
}

class UpdaterViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var downloadJob: Job? = null

    val versioneCorrente: String = runCatching {
        val pm = getApplication<Application>().packageManager
        pm.getPackageInfo(getApplication<Application>().packageName, 0).versionName ?: "0"
    }.getOrDefault("0")

    val versioneCorrenteCode: Int = runCatching {
        val pm = getApplication<Application>().packageManager
        pm.getPackageInfo(getApplication<Application>().packageName, 0).longVersionCode.toInt()
    }.getOrDefault(0)

    fun checkForUpdate() {
        if (_state.value is UpdateState.Checking) return
        _state.value = UpdateState.Checking
        android.util.Log.d("Updater", "Controllo aggiornamenti… (versione locale: $versioneCorrenteCode)")

        viewModelScope.launch {
            try {
                val version = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://raw.githubusercontent.com/ItsZale0/tedesco-app/master/version.json")
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("HTTP ${response.code}")
                        }
                        val body = response.body?.string()
                            ?: throw IOException("Risposta vuota dal server")
                        Json.decodeFromString<VersionInfo>(body)
                    }
                }

                android.util.Log.d("Updater", "Versione server: ${version.versionCode} (${version.versionName}), locale: $versioneCorrenteCode")

                if (version.versionCode > versioneCorrenteCode) {
                    _state.value = UpdateState.Available(version)
                } else {
                    _state.value = UpdateState.UpToDate
                }
            } catch (e: Exception) {
                android.util.Log.e("Updater", "Errore controllo aggiornamenti", e)
                _state.value = UpdateState.Error(messaggioErrore(e), retryable = true)
            }
        }
    }

    fun downloadUpdate(version: VersionInfo) {
        downloadJob?.cancel()
        _state.value = UpdateState.Downloading(0)
        android.util.Log.d("Updater", "Download v${version.versionName} da ${version.apkUrl}")

        downloadJob = viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val dir = File(getApplication<Application>().cacheDir, "updates")
                    dir.mkdirs()

                    // Cancella APK vecchi nella cartella
                    dir.listFiles()?.forEach { f ->
                        if (f.extension == "apk") {
                            f.delete()
                        }
                    }

                    val apk = File(dir, "Tedesco-v${version.versionName}.apk")

                    val request = Request.Builder()
                        .url(version.apkUrl)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("HTTP ${response.code}")
                        }
                        val body = response.body ?: throw IOException("Risposta vuota dal server")

                        val total = body.contentLength()
                        var downloaded = 0L

                        apk.outputStream().use { out ->
                            body.byteStream().use { input ->
                                val buffer = ByteArray(8192)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    out.write(buffer, 0, read)
                                    downloaded += read
                                    if (total > 0) {
                                        val progress = (downloaded * 100 / total).toInt()
                                        _state.value = UpdateState.Downloading(progress)
                                    }
                                }
                            }
                        }
                    }

                    // Verifica integrità APK
                    if (!VerificaApk.isApkValido(apk)) {
                        apk.delete()
                        throw IOException("APK scaricato non valido o corrotto")
                    }

                    apk
                }

                android.util.Log.d("Updater", "Download completato: ${file.length()} byte")
                _state.value = UpdateState.ReadyToInstall(file)
            } catch (e: Exception) {
                android.util.Log.e("Updater", "Errore download", e)
                _state.value = UpdateState.Error(messaggioErrore(e), retryable = true)
            }
        }
    }

    fun installUpdate(file: File) {
        viewModelScope.launch {
            try {
                // Verifica permesso installazione app sconosciute
                val pm = getApplication<Application>().packageManager
                val canInstall = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pm.canRequestPackageInstalls()
                } else {
                    true
                }

                if (!canInstall) {
                    android.util.Log.w("Updater", "Permesso installazione app sconosciuti NON concesso")
                    _state.value = UpdateState.Error(
                        "Per installare l'aggiornamento devi concedere il permesso 'Installa app sconosciuti'. " +
                            "Tocca 'Impostazioni' per aprire le impostazioni di sistema.",
                        retryable = true
                    )
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val uri = FileProvider.getUriForFile(
                        getApplication<Application>(),
                        "${getApplication<Application>().packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    getApplication<Application>().startActivity(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("Updater", "Errore installazione", e)
                _state.value = UpdateState.Error("Installazione fallita: ${e.message}", retryable = true)
            }
        }
    }

    fun apriImpostazioniInstallazione() {
        val packageName = getApplication<Application>().packageName
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun dismiss() {
        downloadJob?.cancel()
        _state.value = UpdateState.Idle
    }

    private fun messaggioErrore(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "Nessuna connessione a internet. Controlla la rete e riprova."
            is SocketTimeoutException -> "Timeout della connessione. Il server non risponde, riprova più tardi."
            is IOException -> {
                val msg = e.message ?: ""
                when {
                    msg.startsWith("HTTP ") -> "Errore del server (${msg.removePrefix("HTTP ")}). Riprova più tardi."
                    msg.contains("vuota", ignoreCase = true) -> "Il server ha risposto in modo non valido. Riprova più tardi."
                    else -> "Errore di rete: $msg"
                }
            }
            is kotlinx.serialization.SerializationException -> "Il file di versione del server non è valido. Riprova più tardi."
            else -> "Errore imprevisto: ${e.message ?: e.javaClass.simpleName}"
        }
    }
}
