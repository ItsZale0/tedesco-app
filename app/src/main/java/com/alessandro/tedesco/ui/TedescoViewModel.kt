package com.alessandro.tedesco.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.CalcoloStatistiche
import com.alessandro.tedesco.data.ProfileManager
import com.alessandro.tedesco.data.SessionState
import com.alessandro.tedesco.data.Statistiche
import com.alessandro.tedesco.data.SyncResult
import com.alessandro.tedesco.data.TestB1
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.TestGrammatica
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.WordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TedescoViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TedescoApp
    private val repo = app.wordRepositoryInstance
    private val profileManager: ProfileManager = app.profileManagerInstance

    /** true quando i profili sono stati caricati da DataStore */
    val pronto: StateFlow<Boolean> = profileManager.prontoFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Profilo attivo; null = si mostra la schermata di selezione */
    val profiloAttivo: StateFlow<ProfiloUtente?> = profileManager.repositoryFlow
        .map { r -> r.profiloAttivoId?.let { id -> r.profili[id] } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val profiliDisponibili: StateFlow<List<ProfiloUtente>> = profileManager.repositoryFlow
        .map { r ->
            val ordine = TipoProfilo.entries.map { it.id }
            r.profili.values.sortedBy { p ->
                ordine.indexOf(p.config.tipo.id).let { if (it < 0) Int.MAX_VALUE else it }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val parole = repo.observeWords()
    val lezioni = repo.observeLessons()
    val ultimoSync = repo.observeLastSync()
    val daRipassare = repo.observeDueCount()
    val guida = repo.observeGuida()
    val lezioneContenuto = repo.observeLezioneContenuto()

    /** Lezione corrente dal profilo attivo. */
    val lezioneCorrente: StateFlow<Int> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id?.let { r.profili[it]?.stato?.progresso?.lezioneCorrente } ?: 1
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    /** Statistiche complete per la schermata Progressi. */
    val statistiche: StateFlow<Statistiche?> = combine(
        repo.observeWords(),
        repo.reviewsFlow,
        profileManager.repositoryFlow
    ) { parole, reviews, repo ->
        val profilo = repo.profiloAttivoId?.let { repo.profili[it] }
        CalcoloStatistiche.calcola(
            parole = parole,
            reviews = reviews,
            progresso = profilo?.stato?.progresso ?: ProgressoUtente(),
            now = System.currentTimeMillis()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val enableCustomWords: StateFlow<Boolean> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id != null && r.profili[id]?.config?.enableCustomWords == true
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val enableGoogleSheets: StateFlow<Boolean> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id != null && r.profili[id]?.config?.enableGoogleSheets == true
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val feedUrl: StateFlow<String> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id?.let { r.profili[it]?.config?.feedUrl } ?: ""
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _sessione = MutableStateFlow(SessionState())
    val sessione: StateFlow<SessionState> = _sessione.asStateFlow()

    private val _messaggio = MutableStateFlow<String?>(null)
    val messaggio: StateFlow<String?> = _messaggio.asStateFlow()

    private val _caricamento = MutableStateFlow(false)
    val caricamento: StateFlow<Boolean> = _caricamento.asStateFlow()

    /** Versione installata dell'app, letta dal manifest. */
    val versioneApp: String = runCatching {
        application.packageManager
            .getPackageInfo(application.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")

    private var syncAvviato = false

    init {
        viewModelScope.launch { profileManager.inizializza() }
    }

    /** Ricarica i profili (usato dal pulsante "Riprova" nella selezione). */
    fun ricaricaProfili() {
        viewModelScope.launch { profileManager.ricarica() }
    }

    // ---- Profili ----

    fun selezionaProfilo(id: String) {
        viewModelScope.launch {
            profileManager.selezionaProfilo(id)
            _messaggio.value = "Profilo attivato"
            sincronizza(mostraMessaggio = false)
        }
    }

    fun esciDalProfilo() {
        viewModelScope.launch {
            profileManager.esciDalProfilo()
            _sessione.value = SessionState()
            syncAvviato = false
        }
    }

    // ---- Sincronizzazione ----

    /**
     * Controlla aggiornamenti all'apertura dell'app.
     * Va chiamato una sola volta per processo: senza il guard, ogni
     * ricomposizione della schermata rifarebbe la richiesta di rete.
     */
    fun controllaAggiornamentiAllAvvio() {
        if (syncAvviato) return
        syncAvviato = true
        sincronizza(mostraMessaggio = false)
    }

    fun sincronizza(mostraMessaggio: Boolean = true) {
        if (profileManager.profiloAttivoId() == null) return
        viewModelScope.launch {
            _caricamento.value = true
            when (val r = repo.sync()) {
                is SyncResult.Updated ->
                    if (mostraMessaggio) {
                        _messaggio.value = "Aggiornate: ${r.newWords} nuove, ${r.updatedWords} modificate"
                    }

                is SyncResult.NotModified ->
                    if (mostraMessaggio) _messaggio.value = "Già aggiornato"

                is SyncResult.Failed ->
                    if (mostraMessaggio) _messaggio.value = "Errore: ${r.message}"
            }
            _caricamento.value = false
        }
    }

    // ---- Sessione di ripasso ----

    fun caricaSessione() {
        viewModelScope.launch {
            _caricamento.value = true
            val reviews: List<ReviewEntity> = repo.dueReviews()
            val tutte: List<WordEntity> = parole.first()
            val cards = reviews.mapNotNull { r -> tutte.firstOrNull { w -> w.id == r.wordId } }
            _sessione.value = SessionState(cards = cards)
            _caricamento.value = false
            if (cards.isEmpty()) _messaggio.value = "Niente da ripassare. Torna domani."
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
            val nuoveErrori = if (sa) s.sbagliate else s.sbagliate.apply { add(carta.german) }
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

    // ---- Parole ----

    fun archivia(id: String) {
        viewModelScope.launch { repo.setArchived(id, true) }
    }

    fun aggiungiParolaCustom(
        german: String,
        italian: String,
        example: String,
        article: String?,
        pronunciation: String?,
        lesson: Int,
        tags: String
    ) {
        viewModelScope.launch {
            runCatching {
                repo.addCustomWord(german, italian, example, article, pronunciation, lesson, tags)
            }.onSuccess {
                _messaggio.value = "Parola aggiunta"
            }.onFailure {
                _messaggio.value = "Errore: ${it.message}"
            }
        }
    }

    fun eliminaParolaCustom(id: String) {
        viewModelScope.launch {
            repo.deleteCustomWord(id)
            _messaggio.value = "Parola eliminata"
        }
    }

    fun reset() {
        viewModelScope.launch {
            repo.resetAll()
            _messaggio.value = "Dati del profilo cancellati"
            sincronizza()
        }
    }

    fun pulisciMessaggio() {
        _messaggio.value = null
    }

    // ---- Livello ----

    fun cambiaLivello(nuovoLivello: com.alessandro.tedesco.data.local.LivelloCEFR) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(livelloCorrente = nuovoLivello)
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
            _messaggio.value = "Livello aggiornato a ${nuovoLivello.label}"
        }
    }

    fun cambiaLezione(nuovaLezione: Int) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(lezioneCorrente = nuovaLezione)
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
            _messaggio.value = "Lezione ${nuovaLezione} selezionata"
        }
    }

    // ---- Test di grammatica ----

    fun salvaTestGrammatica(punteggio: Float, errori: Int, totale: Int, domandeErrate: List<String> = emptyList()) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoTest = com.alessandro.tedesco.data.local.TestGrammatica(
                data = System.currentTimeMillis(),
                punteggio = punteggio,
                errori = errori,
                totale = totale
            )
            val nuoviErrori = (profilo.stato.progresso.erroriGrammatica + domandeErrate).distinct()
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testGrammatica = profilo.stato.progresso.testGrammatica + nuovoTest,
                    erroriGrammatica = nuoviErrori
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
        }
    }

    // ---- Ripasso errori ----

    fun eserciziErrori(): List<com.alessandro.tedesco.data.EsercizioGrammatica> {
        val profilo = profileManager.profiloAttivo() ?: return emptyList()
        val errori = profilo.stato.progresso.erroriGrammatica
        return com.alessandro.tedesco.data.GrammaticaB1.esercizi.filter { it.domanda in errori }
    }

    fun ripassaErrori() {
        val errori = eserciziErrori()
        if (errori.isNotEmpty()) {
            _messaggio.value = "Ripasso di ${errori.size} errori"
        }
    }

    // ---- Test B1 ----

    fun salvaTestB1(
        punteggioLesen: Float,
        punteggioHoeren: Float,
        punteggioSchreiben: Float,
        punteggioSprechen: Float
    ) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoTest = com.alessandro.tedesco.data.TestB1(
                id = "test_${System.currentTimeMillis()}",
                data = System.currentTimeMillis(),
                punteggioLesen = punteggioLesen,
                punteggioHoeren = punteggioHoeren,
                punteggioSchreiben = punteggioSchreiben,
                punteggioSprechen = punteggioSprechen,
                punteggioComplessivo = (punteggioLesen + punteggioHoeren + punteggioSchreiben + punteggioSprechen) / 4
            )
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testB1 = profilo.stato.progresso.testB1 + nuovoTest
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
        }
    }

    companion object {
        fun formattaData(ts: Long): String {
            if (ts == 0L) return "mai"
            return SimpleDateFormat("d/M HH:mm", Locale.ITALIAN).format(Date(ts))
        }
    }
}
