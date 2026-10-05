package com.alessandro.tedesco.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.SessionState
import com.alessandro.tedesco.data.SyncResult
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TedescoViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp
    private val repo = app.wordRepositoryInstance
    private val settingsStore = app.settingsInstance

    val parole = repo.observeWords()
    val lezioni = repo.observeLessons()
    val ultimoSync = repo.observeLastSync()
    val daRipassare = repo.observeDueCount()

    val feedUrl: StateFlow<String> = settingsStore.feedUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _sessione = MutableStateFlow(SessionState())
    val sessione: StateFlow<SessionState> = _sessione.asStateFlow()

    private val _messaggio = MutableStateFlow<String?>(null)
    val messaggio: StateFlow<String?> = _messaggio.asStateFlow()

    private val _caricamento = MutableStateFlow(false)
    val caricamento: StateFlow<Boolean> = _caricamento.asStateFlow()

    fun caricaSessione() {
        viewModelScope.launch {
            _caricamento.value = true
            val reviews: List<ReviewEntity> = repo.dueReviews()
            val tutte: List<WordEntity> = parole.first()
            val cards = reviews.mapNotNull { r: ReviewEntity ->
                tutte.firstOrNull { w: WordEntity -> w.id == r.wordId }
            }
            _sessione.value = SessionState(cards = cards)
            _caricamento.value = false
            if (cards.isEmpty()) {
                _messaggio.value = "Niente da ripassare. Torna domani."
            }
        }
    }

    fun rivela() {
        _sessione.value = _sessione.value.copy(rispostaMostrata = true)
    }

    fun rispondi(sa: Boolean) {
        val s = _sessione.value
        val carta = s.cartaCorrente ?: return
        viewModelScope.launch {
            repo.answer(carta.id, sa)
            val nuoveErrori = if (sa) s.sbagliate else {
                s.sbagliate.apply { add(carta.german) }
            }
            _sessione.value = s.copy(
                indice = s.indice + 1,
                rispostaMostrata = false,
                sbagliate = nuoveErrori
            )
        }
    }

    fun nuovaSessione() {
        _sessione.value = SessionState()
        caricaSessione()
    }

    fun sincronizza(mostraMessaggio: Boolean = true) {
        viewModelScope.launch {
            _caricamento.value = true
            when (val r = repo.sync()) {
                is SyncResult.Updated ->
                    if (mostraMessaggio) {
                        _messaggio.value = "Aggiornate: ${r.newWords} nuove, " +
                            "${r.updatedWords} modificate"
                    }

                is SyncResult.NotModified ->
                    if (mostraMessaggio) _messaggio.value = "Già aggiornato"

                is SyncResult.Failed ->
                    if (mostraMessaggio) _messaggio.value = "Errore: ${r.message}"
            }
            _caricamento.value = false
        }
    }

    fun archivia(id: String) {
        viewModelScope.launch { repo.setArchived(id, true) }
    }

    fun reset() {
        viewModelScope.launch {
            repo.resetAll()
            _messaggio.value = "Dati cancellati"
            sincronizza()
        }
    }

    fun salvaUrl(url: String) {
        viewModelScope.launch {
            settingsStore.setFeedUrl(url.trim())
            _messaggio.value = "URL salvato"
            sincronizza()
        }
    }

    fun pulisciMessaggio() {
        _messaggio.value = null
    }

    companion object {
        fun formattaData(ts: Long): String {
            if (ts == 0L) return "mai"
            val s = SimpleDateFormat("d/M HH:mm", Locale.ITALIAN)
            return s.format(Date(ts))
        }
    }
}