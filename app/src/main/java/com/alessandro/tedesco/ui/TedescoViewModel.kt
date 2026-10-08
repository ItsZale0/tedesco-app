package com.alessandro.tedesco.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.AdaptiveSessionEngine
import com.alessandro.tedesco.data.AdaptiveTestEngine
import com.alessandro.tedesco.data.CalcoloStatistiche
import com.alessandro.tedesco.data.CalcoloPercorsoAdattivo
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
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
import com.alessandro.tedesco.data.remote.TutorService
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
import com.alessandro.tedesco.data.local.FeedbackEntry
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.data.local.SessioneEntity

class TedescoViewModel(application: Application) : AndroidViewModel(application) {

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)

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
    val numeriLezioni: StateFlow<List<Int>> = lezioni.map { l -> l.map { it.numero } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val ultimoSync = repo.observeLastSync()
    val daRipassare = repo.observeDueCount()
    val guida = repo.observeGuida()
    val piano = repo.pianoFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val progressoFeed = repo.progressoFeedFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val sessioni = repo.sessioniFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val lezioneContenuto = repo.observeLezioneContenuto()
    val livelliDisponibili = repo.observeLevels()
    val parolePerLivello: StateFlow<List<WordEntity>> = combine(
        repo.observeWords(),
        profileManager.repositoryFlow
    ) { words, repo ->
        val livello = repo.profiloAttivoId?.let { repo.profili[it]?.stato?.progresso?.livelloCorrente?.label } ?: "A0"
        words.filter { !it.archived && it.level == livello }.sortedBy { it.german }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Lezione corrente dal profilo attivo. */
    val lezioneCorrente: StateFlow<Int> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id?.let { r.profili[it]?.stato?.progresso?.lezioneCorrente } ?: 1
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    /** URL dell'audio della lezione corrente (vuoto se non disponibile). */
    val lezioneAudioUrl: StateFlow<String> = combine(
        repo.observeLessons(),
        lezioneCorrente
    ) { lezioni, n ->
        lezioni.firstOrNull { it.numero == n }?.audioUrl ?: ""
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

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

    /** Percorso adattivo calcolato dai progressi dell'utente. */
    val percorsoAdattivo: StateFlow<CalcoloPercorsoAdattivo.PercorsoAdattivo?> = combine(
        statistiche,
        daRipassare,
        profileManager.repositoryFlow
    ) { stats, due, repo ->
        if (stats == null) return@combine null
        val profilo = repo.profiloAttivoId?.let { repo.profili[it] } ?: return@combine null
        CalcoloPercorsoAdattivo.calcola(
            statistiche = stats,
            competenze = stats.punteggiCompetenze ?: return@combine null,
            daRipassare = due,
            livelloCorrente = profilo.stato.progresso.livelloCorrente,
            obiettivoLivello = profilo.stato.progresso.obiettivoLivello,
            ultimoTest = profilo.stato.progresso.ultimoTest
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** IDs dei passi del percorso giornaliero completati oggi. */
    private val _passiCompletatiOggi = MutableStateFlow<Set<String>>(emptySet())
    val passiCompletatiOggi: StateFlow<Set<String>> = _passiCompletatiOggi.asStateFlow()

    /** Percorso personalizzato del giorno, con passi sequenziali e tracking. */
    val percorsoGiornaliero: StateFlow<CalcoloPercorsoGiornaliero.PercorsoGiornaliero?> = combine(
        statistiche,
        daRipassare,
        profileManager.repositoryFlow,
        _passiCompletatiOggi
    ) { stats, due, repo, completati ->
        if (stats == null) return@combine null
        val profilo = repo.profiloAttivoId?.let { repo.profili[it] } ?: return@combine null
        CalcoloPercorsoGiornaliero.genera(
            statistiche = stats,
            competenze = stats.punteggiCompetenze ?: return@combine null,
            daRipassare = due,
            livelloCorrente = profilo.stato.progresso.livelloCorrente,
            obiettivoLivello = profilo.stato.progresso.obiettivoLivello,
            ultimoTest = profilo.stato.progresso.ultimoTest,
            passiCompletatiOggi = completati
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Sessioni adattive generate dinamicamente dai progressi dell'utente. */
    val sessioniAdattive: StateFlow<AdaptiveSessionEngine.RisultatoGenerazione?> = combine(
        statistiche,
        daRipassare,
        profileManager.repositoryFlow,
        parole,
        repo.reviewsFlow
    ) { stats, due, repo, words, reviews ->
        if (stats == null) return@combine null
        val profilo = repo.profiloAttivoId?.let { repo.profili[it] } ?: return@combine null
        val competenze = stats.punteggiCompetenze ?: return@combine null
        AdaptiveSessionEngine.generaSessioni(
            parole = words,
            reviews = reviews,
            progresso = profilo.stato.progresso,
            competenze = competenze,
            daRipassare = due,
            livelloCorrente = profilo.stato.progresso.livelloCorrente,
            obiettivoLivello = profilo.stato.progresso.obiettivoLivello
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Domande di test adattive generate dinamicamente dai progressi dell'utente. */
    val testiAdattivi: StateFlow<List<com.alessandro.tedesco.data.local.DomandaTest>?> = combine(
        statistiche,
        daRipassare,
        profileManager.repositoryFlow,
        parole,
        repo.reviewsFlow
    ) { stats, due, repo, words, reviews ->
        if (stats == null) return@combine null
        val profilo = repo.profiloAttivoId?.let { repo.profili[it] } ?: return@combine null
        val paroleAttive = words.filter { !it.archived }
        val progresso = profilo.stato.progresso
        AdaptiveTestEngine.generaTestAdattivo(
            progresso = progresso,
            parole = paroleAttive,
            reviews = reviews
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** IDs dei passi del percorso giornaliero completati oggi. */
    fun completaPassoGiornaliero(passoId: String) {
        _passiCompletatiOggi.value = _passiCompletatiOggi.value + passoId
    }

    /** Resetta il percorso giornaliero (nuovo giorno). */
    fun resetPercorsoGiornaliero() {
        _passiCompletatiOggi.value = emptySet()
    }

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

    /** Crea un nuovo profilo personalizzato. */
    fun creaProfilo(nome: String) {
        viewModelScope.launch {
            profileManager.creaProfilo(nome)
            _messaggio.value = "Profilo creato"
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
                // Pubblica sul canale: un cron la committa nel vocabolario su GitHub.
                val topic = profileManager.topicPubblicazione()
                if (topic.isNotBlank()) {
                    app.vocabPublishServiceInstance.pubblica(
                        topic = topic,
                        german = german,
                        italian = italian,
                        example = example,
                        article = article,
                        level = "A1",
                        lesson = lesson
                    )
                }
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
            // Aggiorna subito il contenuto mostrato
            repo.aggiornaLezioneContenuto(nuovaLezione)
            _messaggio.value = "Lezione ${nuovaLezione} selezionata"
        }
    }

    fun aggiornaFeedUrl(nuovoUrl: String) {
        viewModelScope.launch {
            profileManager.aggiornaFeedUrl(nuovoUrl)
            _messaggio.value = "Feed URL aggiornato"
        }
    }

    fun aggiornaGuidaDocId(nuovoDocId: String?) {
        viewModelScope.launch {
            profileManager.aggiornaGuidaDocId(nuovoDocId?.ifBlank { null })
            _messaggio.value = "Documento guida aggiornato"
        }
    }

    /** Cambia la palette colore dell'app. */
    fun cambiaPalette(nuovaPalette: String) {
        viewModelScope.launch {
            profileManager.cambiaPalette(nuovaPalette)
            _messaggio.value = "Colore aggiornato"
        }
    }

    /** Imposta il modello del tutor (null = automatico). */
    fun cambiaModelloTutor(modello: String?) {
        _modelloTutor.value = modello
    }

    fun aggiornaTutorApiKey(nuovaChiave: String) {
        viewModelScope.launch {
            profileManager.aggiornaTutorApiKey(nuovaChiave)
            _messaggio.value = if (nuovaChiave.isBlank()) "Chiave tutor rimossa" else "Chiave tutor salvata"
        }
    }

    val palette: StateFlow<String> = profileManager.paletteFlow

    val tutorApiKey: StateFlow<String> = profileManager.repositoryFlow
        .map { r -> r.profiloAttivoId?.let { r.profili[it]?.config?.tutorApiKey } ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /** Modello OpenRouter scelto dall'utente (null = automatico tra i free). */
    private val _modelloTutor = MutableStateFlow<String?>(null)
    val modelloTutor: StateFlow<String?> = _modelloTutor

    /** Chiede una risposta al tutor AI. */
    suspend fun chiediAlTutor(
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int
    ): Result<String> {
        val chiave = tutorApiKey.value
        return runCatching {
            app.tutorServiceInstance.rispondi(chiave, cronologia, livello, lezione, _modelloTutor.value)
        }
    }

    // ---- Roleplay ----

    private val _roleplayScenario = MutableStateFlow<TutorService.RoleplayScenario?>(null)
    val roleplayScenario: StateFlow<TutorService.RoleplayScenario?> = _roleplayScenario.asStateFlow()

    private val _roleplayAttivo = MutableStateFlow(false)
    val roleplayAttivo: StateFlow<Boolean> = _roleplayAttivo.asStateFlow()

    fun avviaRoleplay(scenario: TutorService.RoleplayScenario) {
        _roleplayScenario.value = scenario
        _roleplayAttivo.value = true
    }

    fun terminaRoleplay() {
        _roleplayAttivo.value = false
        _roleplayScenario.value = null
    }

    suspend fun chiediAlRoleplay(
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int
    ): Result<String> {
        val chiave = tutorApiKey.value
        val scenario = _roleplayScenario.value
            ?: return Result.failure(IllegalStateException("Nessuno scenario selezionato"))
        return runCatching {
            app.tutorServiceInstance.roleplay(chiave, scenario, cronologia, livello, lezione, _modelloTutor.value)
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

    // ---- Test di comprensione ----

    fun salvaTestComprensione(punteggio: Float, errori: Int, totale: Int) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoTest = com.alessandro.tedesco.data.local.TestComprensione(
                data = System.currentTimeMillis(),
                punteggio = punteggio,
                errori = errori,
                totale = totale
            )
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testComprensione = profilo.stato.progresso.testComprensione + nuovoTest
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
        }
    }

    // ---- Test di produzione ----

    fun salvaTestProduzione(punteggio: Float, errori: Int, totale: Int) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoTest = com.alessandro.tedesco.data.local.TestProduzione(
                data = System.currentTimeMillis(),
                punteggio = punteggio,
                errori = errori,
                totale = totale
            )
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testProduzione = profilo.stato.progresso.testProduzione + nuovoTest
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

    /** Avvia una sessione strutturata. */
    fun avviaSessione(tipo: String) {
        when (tipo) {
            "SESSIONE" -> {
                _messaggio.value = "Sessione avviata: inizia dal ripasso"
                nuovaSessione()
            }
            "TEST" -> _messaggio.value = "Test: vai in Altro > Test"
            "ROLEPLAY" -> _messaggio.value = "Roleplay: scrivi il dialogo nel tutor"
            "RIPASSO" -> {
                _messaggio.value = "Ripasso errori"
                ripassaErrori()
            }
            else -> _messaggio.value = "Sessione: $tipo"
        }
    }

    // ---- Feedback / Correzione risposte ----

    private val _risposte = MutableStateFlow<List<FeedbackEntry>>(emptyList())
    val risposte: StateFlow<List<FeedbackEntry>> = _risposte

    private val _correzioneInCorso = MutableStateFlow(false)
    val correzioneInCorso: StateFlow<Boolean> = _correzioneInCorso

    /** Invia una risposta in tedesco e riceve la correzione dal tutor AI. */
    fun inviaRisposta(testo: String) {
        viewModelScope.launch {
            val chiave = tutorApiKey.value
            if (chiave.isBlank()) {
                _messaggio.value = "Per correggere le risposte serve una chiave API OpenRouter. Vai in Profilo → Tutor AI per inserirla."
                return@launch
            }
            _correzioneInCorso.value = true
            try {
                val profilo = profileManager.profiloAttivo() ?: return@launch
                val livello = profilo.stato.progresso.livelloCorrente.label.substringBefore(" ")
                val lezione = lezioneCorrente.value
                val correzione = app.tutorServiceInstance.correggiRisposta(
                    apiKey = chiave,
                    testoStudente = testo,
                    livello = livello,
                    lezione = lezione,
                    modello = _modelloTutor.value
                )
                val entry = FeedbackEntry(
                    id = "fb_${System.currentTimeMillis()}",
                    testo = testo,
                    timestamp = System.currentTimeMillis(),
                    corretto = true,
                    correzione = correzione
                )
                _risposte.value = listOf(entry) + _risposte.value
                val nuovoStato = profilo.stato.copy(
                    risposte = listOf(entry) + profilo.stato.risposte
                )
                profileManager.aggiornaStatoAttivo(nuovoStato)
            } catch (e: Exception) {
                _messaggio.value = "Errore durante la correzione: ${e.message}"
            } finally {
                _correzioneInCorso.value = false
            }
        }
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

    // ---- Test adattivi ----
    fun salvaTestAdattivo(punteggio: Float, errori: Int, totale: Int, livello: String) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoTest = com.alessandro.tedesco.data.local.TestAdattivo(
                data = System.currentTimeMillis(),
                punteggio = punteggio,
                errori = errori,
                totale = totale,
                livello = livello
            )
            // Calcola il nuovo livello adattivo in base alle prestazioni
            val nuovoLivello = AdaptiveTestEngine.calcolaLivelloAdattivo(
                progresso = profilo.stato.progresso,
                domandeTotali = totale,
                errori = errori
            )
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testAdattivi = profilo.stato.progresso.testAdattivi + nuovoTest,
                    livelloCorrente = nuovoLivello
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
        }
    }

    // ---- Test di ascolto ----
    fun salvaTestAscolto(punteggio: Float, errori: Int, totale: Int, livello: String) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nuovoTest = com.alessandro.tedesco.data.local.TestAscolto(
                data = System.currentTimeMillis(),
                punteggio = punteggio,
                errori = errori,
                totale = totale,
                livello = livello
            )
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testAscolto = profilo.stato.progresso.testAscolto + nuovoTest
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
