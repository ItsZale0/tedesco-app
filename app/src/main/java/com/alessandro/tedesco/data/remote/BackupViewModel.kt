package com.alessandro.tedesco.data.remote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.BackupCloud
import com.alessandro.tedesco.data.BackupSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Stato dell'operazione di backup, per messaggi onesti all'utente. */
sealed interface BackupStato {
    data object Idle : BackupStato
    data object InCorso : BackupStato
    data class Ok(val messaggio: String) : BackupStato
    data class Errore(val messaggio: String) : BackupStato
}

/**
 * Backup cloud su GitHub: carica una fotografia di TUTTI i profili su un repo
 * privato, e la ripristina.
 *
 * Il ripristino e' distruttivo: sovrascrive i dati locali. Per questo non parte
 * mai da un tap solo — la schermata mostra il riepilogo di cosa contiene il
 * backup e chiede conferma.
 */
class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp
    private val settings = BackupSettings(application)

    val token: StateFlow<String> = settings.tokenFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val repo: StateFlow<String> = settings.repoFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val ultimoBackup: StateFlow<Long> = settings.ultimoBackupFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    private val _stato = MutableStateFlow<BackupStato>(BackupStato.Idle)
    val stato: StateFlow<BackupStato> = _stato

    /** Riepilogo del backup scaricato, in attesa di conferma per il ripristino. */
    private val _daRipristinare = MutableStateFlow<BackupCloud.Riepilogo?>(null)
    val daRipristinare: StateFlow<BackupCloud.Riepilogo?> = _daRipristinare
    private var contenutoDaRipristinare: String? = null

    fun salvaToken(t: String) { viewModelScope.launch { settings.salvaToken(t) } }
    fun salvaRepo(r: String) { viewModelScope.launch { settings.salvaRepo(r) } }
    fun pulisciStato() { _stato.value = BackupStato.Idle }

    private fun client(): BackupGitHubClient? {
        val t = token.value.trim()
        val r = repo.value.trim()
        if (t.isBlank()) return null
        return BackupGitHubClient(t, if (r.isBlank()) BackupCloud.REPO_DEFAULT else r)
    }

    /** Verifica token + repo senza toccare i dati. */
    fun provaConnessione() {
        val c = client()
        if (c == null) { _stato.value = BackupStato.Errore("Inserisci prima il token GitHub"); return }
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            _stato.value = when (val e = c.verificaAccesso()) {
                is BackupCloud.Esito.Ok -> BackupStato.Ok(e.dettaglio)
                is BackupCloud.Esito.Errore -> BackupStato.Errore(e.messaggio)
            }
        }
    }

    /** Carica il backup (tutti i profili) sul repo privato. */
    fun eseguiBackup(versioneApp: String) {
        val c = client()
        if (c == null) { _stato.value = BackupStato.Errore("Inserisci prima il token GitHub"); return }
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val repoCompleto = app.profileManagerInstance.repositoryFlow.first()
                val json = BackupCloud.serializza(repoCompleto, versioneApp, System.currentTimeMillis())
                c.carica(json, "Backup Tedesco App")
                settings.registraBackup(System.currentTimeMillis())
                val n = repoCompleto.profili.size
                _stato.value = BackupStato.Ok("Backup completato: $n profili, ${json.length / 1024} KB")
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore(e.message ?: "Errore sconosciuto")
            }
        }
    }

    /** Scarica il backup e mostra il riepilogo; il ripristino richiede conferma. */
    fun preparaRipristino() {
        val c = client()
        if (c == null) { _stato.value = BackupStato.Errore("Inserisci prima il token GitHub"); return }
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val json = c.scarica()
                if (json == null) {
                    _stato.value = BackupStato.Errore("Nessun backup trovato su GitHub")
                    return@launch
                }
                val pacchetto = BackupCloud.deserializza(json)
                contenutoDaRipristinare = json
                _daRipristinare.value = BackupCloud.riepilogo(pacchetto)
                _stato.value = BackupStato.Idle
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore(e.message ?: "Errore sconosciuto")
            }
        }
    }

    fun annullaRipristino() {
        _daRipristinare.value = null
        contenutoDaRipristinare = null
    }

    /** Applica il ripristino: sovrascrive i dati locali e ricarica l'app. */
    fun confermaRipristino() {
        val json = contenutoDaRipristinare ?: return
        _stato.value = BackupStato.InCorso
        viewModelScope.launch {
            try {
                val pacchetto = BackupCloud.deserializza(json)
                withContext(Dispatchers.IO) {
                    app.profileManagerInstance.sostituisciRepository(pacchetto.dati)
                }
                _daRipristinare.value = null
                contenutoDaRipristinare = null
                _stato.value = BackupStato.Ok("Ripristino completato: ${pacchetto.dati.profili.size} profili")
            } catch (e: Exception) {
                _stato.value = BackupStato.Errore("Ripristino fallito: ${e.message}")
            }
        }
    }
}
