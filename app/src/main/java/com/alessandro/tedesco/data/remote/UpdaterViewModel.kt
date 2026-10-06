package com.alessandro.tedesco.data.remote

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val version: VersionInfo) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdaterViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp
    private val client = OkHttpClient()

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val currentVersionCode: Int = runCatching {
        val pm = getApplication<Application>().packageManager
        pm.getPackageInfo(getApplication<Application>().packageName, 0).longVersionCode.toInt()
    }.getOrDefault(0)

    fun checkForUpdate() {
        if (_state.value is UpdateState.Checking) return
        _state.value = UpdateState.Checking

        viewModelScope.launch {
            try {
                val version = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://raw.githubusercontent.com/ItsZale0/tedesco-app/main/version.json")
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        val body = response.body?.string() ?: throw Exception("Empty body")
                        kotlinx.serialization.json.Json.decodeFromString<VersionInfo>(body)
                    }
                }

                if (version.versionCode > currentVersionCode) {
                    _state.value = UpdateState.Available(version)
                } else {
                    _state.value = UpdateState.Idle
                }
            } catch (e: Exception) {
                // silenzioso: non bloccare l'app se il check fallisce
                _state.value = UpdateState.Idle
            }
        }
    }

    fun downloadUpdate(version: VersionInfo) {
        _state.value = UpdateState.Downloading(0)

        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val dir = File(getApplication<Application>().cacheDir, "updates")
                    dir.mkdirs()
                    val apk = File(dir, "Tedesco-v${version.versionName}.apk")

                    val request = Request.Builder()
                        .url(version.apkUrl)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        val body = response.body ?: throw Exception("Empty body")

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
                    apk
                }

                _state.value = UpdateState.ReadyToInstall(file)
            } catch (e: Exception) {
                _state.value = UpdateState.Error(e.message ?: "Download fallito")
            }
        }
    }

    fun installUpdate(file: File) {
        viewModelScope.launch {
            try {
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
                _state.value = UpdateState.Error("Installazione fallita: ${e.message}")
            }
        }
    }

    fun dismiss() {
        _state.value = UpdateState.Idle
    }
}
