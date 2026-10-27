package com.alessandro.tedesco.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.limita
import com.alessandro.tedesco.data.AdaptiveSessionEngine
import com.alessandro.tedesco.data.AdaptiveTestEngine
import com.alessandro.tedesco.data.CalcoloProgresso
import com.alessandro.tedesco.data.ParserValutazione
import com.alessandro.tedesco.data.CalcoloProduzione
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.data.VerificaLessico
import com.alessandro.tedesco.data.CalcoloStatistiche
import com.alessandro.tedesco.data.CalcoloPercorsoAdattivo
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
import com.alessandro.tedesco.data.ProfileManager
import com.alessandro.tedesco.data.SessionState
import com.alessandro.tedesco.data.Statistiche
import com.alessandro.tedesco.data.SyncResult
import com.alessandro.tedesco.data.TestB1
import com.alessandro.tedesco.data.selezionaProssimaLezione
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.CalcoloCompetenze
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.TestGrammatica
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.remote.GeneratoreLezioni
import com.alessandro.tedesco.data.remote.TutorService
import com.alessandro.tedesco.data.remote.EsercizioGenerato
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.data.local.EsercizioGenEntity
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
import com.alessandro.tedesco.data.local.ConversazioneEntry
import com.alessandro.tedesco.data.local.FeedbackEntry
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.data.local.SessioneEntity
import com.alessandro.tedesco.data.ErroreEntity
import com.alessandro.tedesco.data.ErroriSRS
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.UnitaConStato

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
    /**
     * Progresso REALE: unica fonte di verità per livello, streak, parole, traguardi.
     * Tutto derivato da dati reali (parole, log ripassi, test) — mai valori inventati.
     */
    val progressoReale: StateFlow<CalcoloProgresso.ProgressoReale?> = combine(
        repo.observeWords(),
        repo.reviewsFlow,
        profileManager.repositoryFlow,
        repo.ripassiLogFlow
    ) { parole, reviews, repoState, log ->
        val profilo = repoState.profiloAttivoId?.let { repoState.profili[it] } ?: return@combine null
        CalcoloProgresso.calcola(
            parole = parole,
            reviews = reviews,
            progresso = profilo.stato.progresso,
            log = log,
            obiettivo = profilo.stato.progresso.obiettivoLivello,
            now = System.currentTimeMillis()
        )
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Parole da ripassare: STESSA lista che alimenta il contatore `paroleFragili`.
     * Deriva dalle stesse sorgenti (parole, review, log) con lo stesso calcolo, quindi
     * "2 da ripassare" in home == 2 carte nella sessione di ripasso.
     */
    val paroleDaRipassare: StateFlow<List<WordEntity>> = combine(
        repo.observeWords(),
        repo.reviewsFlow,
        repo.ripassiLogFlow
    ) { parole, reviews, log ->
        val verifiche = VerificaLessico.verificheDaLog(log)
        VerificaLessico.paroleDaRipassare(
            parole, log, verifiche, reviews, System.currentTimeMillis()
        )
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Percorso grammaticale strutturato: unita' + stato di sblocco/padronanza.
     * La lista e' statica (per ora 3 unita' pilota); cambia solo lo stato salvato
     * nel profilo attivo.
     */
    val percorsoGrammatica: StateFlow<List<UnitaConStato>> = profileManager.repositoryFlow
        .map { r ->
            val stato = r.profiloAttivoId?.let { r.profili[it]?.stato } ?: ProfiloStato()
            GrammaticaSyllabus.percorso(stato.padronanzaGrammatica)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GrammaticaSyllabus.percorso(emptyMap())
        )

    val statistiche: StateFlow<Statistiche?> = combine(
        repo.observeWords(),
        repo.reviewsFlow,
        profileManager.repositoryFlow,
        repo.ripassiLogFlow
    ) { parole, reviews, repo, ripassiLog ->
        val profilo = repo.profiloAttivoId?.let { repo.profili[it] }
        CalcoloStatistiche.calcola(
            parole = parole,
            reviews = reviews,
            progresso = profilo?.stato?.progresso ?: ProgressoUtente(),
            now = System.currentTimeMillis(),
            ripassiLog = ripassiLog
        )
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
    // PIGRO: questo flusso calcola (Calcolo*, AdaptiveSessionEngine, ...) e viene
        // ricostruito a ogni evento. Con Eagerly il lavoro avveniva SEMPRE, anche
        // quando nessuna schermata mostrava il valore: era il grosso delle
        // allocazioni che riempivano l'heap. Con WhileSubscribed si calcola solo
        // mentre una schermata lo osserva (5s di grazia per la navigazione).
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** IDs dei passi del percorso giornaliero completati oggi. */
    fun completaPassoGiornaliero(passoId: String) {
        _passiCompletatiOggi.value = _passiCompletatiOggi.value + passoId
    }

    /** Segna le parole mostrate come studiate e completa il passo vocabolario del giorno. */
    fun segnaParoleStudiate(wordIds: List<String>) {
        if (wordIds.isEmpty()) return
        viewModelScope.launch {
            repo.segnaParoleStudiate(wordIds)
            completaPassoGiornaliero("vocabolario")
        }
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

    /** True se il profilo attivo deve vedere il contesto medico. */
    val mostraContestoMedico: StateFlow<Boolean> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id != null && r.profili[id]?.config?.mostraContestoMedico == true
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Livello CEFR iniziale del profilo attivo. */
    val livelloIniziale: StateFlow<LivelloIniziale> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id?.let { r.profili[it]?.config?.livelloIniziale } ?: LivelloIniziale.A0
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LivelloIniziale.A0)

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
        // Al cambio di profilo attivo ricarica gli esercizi generati dal nuovo profilo
        // (evita che restino visibili quelli del profilo precedente).
        viewModelScope.launch {
            var ultimoProfilo: String? = "__init__"
            profileManager.repositoryFlow.collect { repo ->
                val id = repo.profiloAttivoId
                if (id != ultimoProfilo) {
                    ultimoProfilo = id
                    if (id != null) {
                        // Il ViewModel e' un singleton: senza il reset restano in memoria
                        // lezione, esercizi, domande e stato del profilo precedente.
                        resetStatoProfilo()
                        caricaEserciziGenerati()
                        // Riallinea il livello CEFR ai criteri reali: corregge eventuali
                        // livelli gonfiati salvati in precedenza (es. falso "A2").
                        verificaEPromuovi()
                    }
                }
            }
        }
    }

    /** Ricarica i profili (usato dal pulsante "Riprova" nella selezione). */
    fun ricaricaProfili() {
        viewModelScope.launch { profileManager.ricarica() }
    }

    // ---- Profili ----

    fun selezionaProfilo(id: String) {
        viewModelScope.launch {
            profileManager.selezionaProfilo(id)
            // Subito dopo il cambio: la memoria del profilo precedente non deve
            // restare visibile (Emma non deve vedere le lezioni di Alessandro).
            resetStatoProfilo()
            _messaggio.value = "Profilo attivato"
            sincronizza(mostraMessaggio = false)
        }
    }

    /**
     * Azzera tutto lo stato in memoria che appartiene al profilo attivo.
     *
     * Il ViewModel vive per tutta la durata dell'app (singleton in AppNav): i dati
     * del profilo precedente resterebbero in memoria e verrebbero mostrati al nuovo
     * profilo finche' qualche schermata non li ricarica. Per questo ogni cambio
     * profilo passa da qui: il nuovo profilo parte vuoto e genera i propri
     * contenuti (lezione ed esercizi) con il suo livello.
     */
    fun resetStatoProfilo() {
        _domandeVerifica.value = emptyList()
        _risposte.value = emptyList()
        _sessione.value = SessionState()
        _lezioneStato.value = StatoLezione.Idle
        setGenFlow(TipoEsercizio.COMPRENSIONE, emptyList())
        setGenFlow(TipoEsercizio.PRODUZIONE, emptyList())
        setGenFlow(TipoEsercizio.ASCOLTO, emptyList())
        setGenFlow(TipoEsercizio.GRAMMATICA, emptyList())
        repo.resetContenutiProfilo()
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
            resetStatoProfilo()
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

    /**
     * Carica la sessione di ripasso dalla fonte unica: la stessa lista del contatore,
     * gia' calcolata dal flow `paroleDaRipassare`. Prima usava solo `dueReviews()`:
     * per questo una sezione che mostrava "niente da ripassare" poteva lasciare
     * il contatore su "2 da ripassare".
     */
    fun caricaSessione() {
        val cards = paroleDaRipassare.value
        _sessione.value = SessionState(cards = cards)
        _messaggio.value = if (cards.isEmpty()) "Niente da ripassare. Torna domani."
        else "Ripasso di ${cards.size} parole"
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

    // ---- Verifica oggettiva del lessico (scelta multipla) ----

    private val _domandeVerifica = MutableStateFlow<List<VerificaLessico.Domanda>>(emptyList())
    val domandeVerifica: StateFlow<List<VerificaLessico.Domanda>> = _domandeVerifica
    private val _indiceVerifica = MutableStateFlow(0)
    val indiceVerifica: StateFlow<Int> = _indiceVerifica
    private val _sceltaVerifica = MutableStateFlow<String?>(null)
    val sceltaVerifica: StateFlow<String?> = _sceltaVerifica
    private val _esitoVerifica = MutableStateFlow<VerificaLessico.Esito?>(null)
    val esitoVerifica: StateFlow<VerificaLessico.Esito?> = _esitoVerifica
    private var risposteVerifica = mutableListOf<Pair<VerificaLessico.Domanda, String>>()

    /**
     * Avvia il quiz di verifica: prende le parole da verificare (fragili prima),
     * costruisce domande a scelta multipla con distrattori reali dal vocabolario.
     */
    fun avviaVerifica(limite: Int = 10) {
        viewModelScope.launch {
            val parole = repo.observeWords().first().filter { ParolaTedesca.utilizzabile(it) }
            val log = repo.ripassiLogFlow.first()
            val verifiche = VerificaLessico.verificheDaLog(log)
            // Prima le fragili (verificate e sbagliate), poi quelle mai verificate.
            val fragili = VerificaLessico.paroleFragili(parole, log, verifiche)
            val daVerificare = VerificaLessico.paroleDaVerificare(parole, log, verifiche, limite)
            // NIENTE fallback su tutte le parole: se non c'e' nulla da verificare,
            // il quiz e' vuoto e la schermata lo dice (prima riproponeva le stesse).
            val base = (fragili + daVerificare).distinctBy { it.id }.take(limite)
            _domandeVerifica.value = if (base.size >= 2) VerificaLessico.costruisciDomande(base) else emptyList()
            _indiceVerifica.value = 0
            _sceltaVerifica.value = null
            _esitoVerifica.value = null
            risposteVerifica = mutableListOf()
        }
    }

    /** Registra la risposta scelta e la logga come ripasso reale (evidenza). */
    fun rispondiVerifica(opzione: String) {
        val d = _domandeVerifica.value.getOrNull(_indiceVerifica.value) ?: return
        if (_sceltaVerifica.value != null) return
        val corretta = opzione == d.corretta
        _sceltaVerifica.value = opzione
        risposteVerifica.add(d to opzione)
        // Logga l'evidenza: la verifica e' un ripasso oggettivo, non self-report
        viewModelScope.launch { repo.registraVerifica(d.parola.id, corretta) }
    }

    fun prossimaVerifica() {
        val domande = _domandeVerifica.value
        val i = _indiceVerifica.value
        if (i + 1 >= domande.size) {
            val verificate = risposteVerifica.filter { (d, r) -> r == d.corretta }.map { it.first.parola.id }
            val daRivedere = risposteVerifica.filter { (d, r) -> r != d.corretta }.map { it.first.parola.id }
            _esitoVerifica.value = VerificaLessico.Esito(
                totale = domande.size,
                corrette = verificate.size,
                paroleVerificate = verificate,
                paroleDaRivedere = daRivedere
            )
        } else {
            _indiceVerifica.value = i + 1
            _sceltaVerifica.value = null
        }
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

    /** Rimuove un esercizio generato (per id) dalla lista in memoria e dal profilo. */
    fun eliminaEsercizioGenerato(tipo: TipoEsercizio, id: String) {
        viewModelScope.launch {
            when (tipo) {
                TipoEsercizio.COMPRENSIONE -> {
                    val aggiornati = _eserciziGenComprensione.value.filterNot { it.id == id }
                    _eserciziGenComprensione.value = aggiornati
                    val profilo = profileManager.profiloAttivo() ?: return@launch
                    val nuovoStato = profilo.stato.copy(eserciziGeneratiComprensione = aggiornati)
                    profileManager.aggiornaStatoAttivo(nuovoStato)
                }
                TipoEsercizio.PRODUZIONE -> {
                    val aggiornati = _eserciziGenProduzione.value.filterNot { it.id == id }
                    _eserciziGenProduzione.value = aggiornati
                    val profilo = profileManager.profiloAttivo() ?: return@launch
                    val nuovoStato = profilo.stato.copy(eserciziGeneratiProduzione = aggiornati)
                    profileManager.aggiornaStatoAttivo(nuovoStato)
                }
                TipoEsercizio.ASCOLTO -> {
                    val aggiornati = _eserciziGenAscolto.value.filterNot { it.id == id }
                    _eserciziGenAscolto.value = aggiornati
                    val profilo = profileManager.profiloAttivo() ?: return@launch
                    val nuovoStato = profilo.stato.copy(eserciziGeneratiAscolto = aggiornati)
                    profileManager.aggiornaStatoAttivo(nuovoStato)
                }
                TipoEsercizio.GRAMMATICA -> {
                    val aggiornati = _eserciziGenGrammatica.value.filterNot { it.id == id }
                    _eserciziGenGrammatica.value = aggiornati
                    val profilo = profileManager.profiloAttivo() ?: return@launch
                    val nuovoStato = profilo.stato.copy(eserciziGeneratiGrammatica = aggiornati)
                    profileManager.aggiornaStatoAttivo(nuovoStato)
                }
            }
            _messaggio.value = "Esercizio eliminato"
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

    /**
     * Allinea il livello del profilo al livello CEFR REALE calcolato dai test.
     * Promuove solo se tutte le dimensioni raggiungono la soglia; declassa se il
     * livello attuale non è più supportato dai dati (evita falsi "pronto per A2").
     */
    fun verificaEPromuovi() {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val p = profilo.stato.progresso
            val parole = repo.observeWords().first()
            val reviews = repo.reviewsFlow.first()
            val log = repo.ripassiLogFlow.first()
            // Passa il log: senza, il livello non vede le parole verificate nel quiz.
            val comp = CalcoloCompetenze.calcola(parole, reviews, p, System.currentTimeMillis(), log)
            val stimato = comp.livelloComplessivo
            if (stimato == p.livelloCorrente) return@launch

            // Un profilo SENZA dati non deve tornare al livello base: il livello
            // iniziale e' una scelta dell'utente (es. Emma parte da B1). Senza questa
            // guardia ogni cambio profilo resettava a A0 il livello selezionato.
            if (parole.isEmpty()) return@launch

            // Mai RETROCEDERE: il ricalcolo puo' solo promuovere, non degradare.
            // Un livello piu' alto di quello dimostrato resta valido finche' non
            // viene cambiato esplicitamente dall'utente.
            if (stimato.ordine <= p.livelloCorrente.ordine) return@launch

            val nuovoStato = profilo.stato.copy(progresso = p.copy(livelloCorrente = stimato))
            profileManager.aggiornaStatoAttivo(nuovoStato)
            _messaggio.value = if (stimato.ordine > p.livelloCorrente.ordine)
                "Complimenti! I tuoi progressi ti portano a ${stimato.label}!"
            else
                "Livello riallineato a ${stimato.label}: completa più test per avanzare."
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

    /** Giorni rimanenti prima che la prossima lezione sia disponibile (0 = disponibile ora). */
    val giorniProssimaLezione: StateFlow<Int> = profileManager.repositoryFlow
        .map { r ->
            val inizio = r.profiloAttivoId?.let { r.profili[it]?.stato?.progresso?.dataInizioLezione } ?: 0L
            if (inizio <= 0L) GIORNI_PER_LEZIONE
            else {
                val trascorsi = (System.currentTimeMillis() - inizio) / MS_PER_GIORNO
                (GIORNI_PER_LEZIONE - trascorsi).toInt().coerceAtLeast(0)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Inizializza la data di inizio lezione se non e' mai stata impostata (primo avvio). */
    fun inizializzaDataLezione() {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            if (profilo.stato.progresso.dataInizioLezione == 0L) {
                val nuovoStato = profilo.stato.copy(
                    progresso = profilo.stato.progresso.copy(dataInizioLezione = System.currentTimeMillis())
                )
                profileManager.aggiornaStatoAttivo(nuovoStato)
            }
        }
    }

    /** Avanza alla lezione successiva, solo se sono passati >= 2 giorni dall'inizio della corrente. */
    fun avanzaLezione() {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val p = profilo.stato.progresso
            // Parole disponibili e lezioni del feed per capire che lezioni sono "vere".
            val parole = repo.observeWords().first().filter { !it.archived }
            val lezioniFeed = repo.observeLessons().first()
            val nuova = p.lezioneCorrente + 1
            // Selettore: prossima lezione con parole nel vocabolario o contenuto.
            val prossima = selezionaProssimaLezione(nuova, parole, lezioniFeed)
            if (prossima <= p.lezioneCorrente) return@launch

            val nuovoStato = profilo.stato.copy(
                progresso = p.copy(
                    lezioneCorrente = prossima,
                    dataInizioLezione = System.currentTimeMillis(),
                    lezioniCompletate = p.lezioniCompletate + 1
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
            repo.aggiornaLezioneContenuto(prossima)
            // Genera SUBITO il contenuto della lezione appena raggiunta: cosi'
            // quando l'utente la apre e' già pronta.
            if (repo.observeLezioneContenuto().first().isBlank()) {
                generaLezioneDinamica(prossima)
            }
            _messaggio.value = "Lezione $prossima iniziata. La prossima la clicca quando e' pronta!"
        }
    }    private val _lezioneStato = MutableStateFlow<StatoLezione>(StatoLezione.Idle)
    val lezioneStato: StateFlow<StatoLezione> = _lezioneStato.asStateFlow()

    private val generatoreLezioni = GeneratoreLezioni()

    /** Tema della lezione: titolo dal feed se c'e', altrimenti dalla scaletta base. */
    private fun argomentoLezione(numero: Int, titoloFeed: String?): String {
        if (!titoloFeed.isNullOrBlank()) {
            val pulito = titoloFeed.substringAfter("—", titoloFeed)
                .substringAfter(" - ", titoloFeed)
                .substringAfter(": ", titoloFeed)
                .trim()
            if (pulito.length > 2) return pulito
        }
        val temi = listOf(
            "Presentarsi e salutare", "Numeri, eta' e date", "La famiglia",
            "Citta' e paese", "Cibo e bevande", "La giornata e le ore",
            "Tempo libero e hobby", "Casa e stanze", "Vestiti e colori",
            "Salute e corpo", "Viaggi e mezzi", "Lavoro e professioni",
            "Tempo atmosferico", "Animali", "Soldi, negozi e prezzi",
            "Al telefono", "Al ristorante", "In farmacia"
        )
        return temi[(numero - 1).mod(temi.size)]
    }

    /**
     * Genera la lezione [numero] (o quella corrente) con l'AI: contenuto completo +
     * 12 parole nuove che entrano nel VOCABOLARIO del profilo.
     *
     * Perche' serve: il feed puo' non avere la lezione (profilo Emma: 0 lezioni) o
     * averla senza parole (Alessandro: lezione 5 con 0 parole) -> la lezione non
     * usciva e non aggiornava il lessico.
     */
    fun generaLezioneDinamica(numero: Int = -1) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val n = if (numero > 0) numero else profilo.stato.progresso.lezioneCorrente
            val livello = profilo.stato.progresso.livelloCorrente.label.substringBefore(" ")
            if (_lezioneStato.value is StatoLezione.InCorso) return@launch

            _lezioneStato.value = StatoLezione.InCorso
            try {
                val titoloFeed = repo.observeLessons().first().firstOrNull { it.numero == n }?.titolo
                val paroleNote = repo.observeWords().first().map { it.german }
                val lezione = generatoreLezioni.genera(
                    apiKey = tutorApiKey.value,
                    numero = n,
                    livello = livello,
                    argomento = argomentoLezione(n, titoloFeed),
                    paroleNote = paroleNote
                )
                repo.salvaLezioneGenerata(n, lezione.contenuto)
                val aggiunte = repo.aggiungiParoleLezione(lezione.parole, n, livello)
                _lezioneStato.value = StatoLezione.Fatto(aggiunte)
                _messaggio.value = "Lezione $n generata: $aggiunte parole nuove nel lessico"
            } catch (e: Exception) {
                _lezioneStato.value = StatoLezione.Errore(e.message ?: "Generazione non riuscita")
            }
        }
    }

    fun resetLezioneStato() { _lezioneStato.value = StatoLezione.Idle }

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

    /** Modello Groq scelto dall'utente (null = automatico tra i free). */
    private val _modelloTutor = MutableStateFlow<String?>(null)

    // Lista modelli Groq disponibili (fetchata in tempo reale)
    private val _modelliDisponibili = MutableStateFlow<List<Pair<String?, String>>>(listOf(null to "Automatico (consigliato)"))
    val modelliDisponibili: StateFlow<List<Pair<String?, String>>> = _modelliDisponibili
    // Livello di ragionamento (null = default/none)
    private val _ragionamento = MutableStateFlow<String?>(null)
    val ragionamento: StateFlow<String?> = _ragionamento

    fun cambiaRagionamento(v: String?) {
        _ragionamento.value = v
        app.tutorServiceInstance.reasoningEffort = v
    }
    private var modelliCaricati = false

    fun caricaModelli() {
        if (modelliCaricati) return
        viewModelScope.launch {
            val chiave = tutorApiKey.value
            val lista = app.tutorServiceInstance.modelliDisponibili(chiave)
            if (lista.size > 1) { _modelliDisponibili.value = lista; modelliCaricati = true }
        }
    }
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

    // ---- Esercizi generati dall'AI (infiniti) ----

    private val _eserciziGenComprensione = MutableStateFlow<List<EsercizioGenEntity>>(emptyList())
    val eserciziGenComprensione: StateFlow<List<EsercizioGenEntity>> = _eserciziGenComprensione.asStateFlow()
    private val _eserciziGenProduzione = MutableStateFlow<List<EsercizioGenEntity>>(emptyList())
    val eserciziGenProduzione: StateFlow<List<EsercizioGenEntity>> = _eserciziGenProduzione.asStateFlow()
    private val _eserciziGenAscolto = MutableStateFlow<List<EsercizioGenEntity>>(emptyList())
    val eserciziGenAscolto: StateFlow<List<EsercizioGenEntity>> = _eserciziGenAscolto.asStateFlow()
    private val _eserciziGenGrammatica = MutableStateFlow<List<EsercizioGenEntity>>(emptyList())
    val eserciziGenGrammatica: StateFlow<List<EsercizioGenEntity>> = _eserciziGenGrammatica.asStateFlow()

    private fun statoGenerati(tipo: TipoEsercizio, stato: ProfiloStato): List<EsercizioGenEntity> =
        when (tipo) {
            TipoEsercizio.COMPRENSIONE -> stato.eserciziGeneratiComprensione
            TipoEsercizio.PRODUZIONE -> stato.eserciziGeneratiProduzione
            TipoEsercizio.ASCOLTO -> stato.eserciziGeneratiAscolto
            TipoEsercizio.GRAMMATICA -> stato.eserciziGeneratiGrammatica
        }

    private fun setGenFlow(tipo: TipoEsercizio, lista: List<EsercizioGenEntity>) {
        when (tipo) {
            TipoEsercizio.COMPRENSIONE -> _eserciziGenComprensione.value = lista
            TipoEsercizio.PRODUZIONE -> _eserciziGenProduzione.value = lista
            TipoEsercizio.ASCOLTO -> _eserciziGenAscolto.value = lista
            TipoEsercizio.GRAMMATICA -> _eserciziGenGrammatica.value = lista
        }
    }

    private fun copiaStatoConNuovi(tipo: TipoEsercizio, stato: ProfiloStato, nuovi: List<EsercizioGenEntity>): ProfiloStato {
        // Cap a 80: la lista serve solo come memoria "gia visto" per evitare ripetizioni,
        // non deve crescere all'infinito (gonfierebbe il DataStore).
        fun cap(l: List<EsercizioGenEntity>) = (l + nuovi).takeLast(80)
        return when (tipo) {
            TipoEsercizio.COMPRENSIONE -> stato.copy(eserciziGeneratiComprensione = cap(stato.eserciziGeneratiComprensione))
            TipoEsercizio.PRODUZIONE -> stato.copy(eserciziGeneratiProduzione = cap(stato.eserciziGeneratiProduzione))
            TipoEsercizio.ASCOLTO -> stato.copy(eserciziGeneratiAscolto = cap(stato.eserciziGeneratiAscolto))
            TipoEsercizio.GRAMMATICA -> stato.copy(eserciziGeneratiGrammatica = cap(stato.eserciziGeneratiGrammatica))
        }
    }

    /** Carica gli esercizi generati salvati nel profilo attivo (onEnter). */
    fun caricaEserciziGenerati() {
        viewModelScope.launch {
            val p = profileManager.profiloAttivo() ?: return@launch
            setGenFlow(TipoEsercizio.COMPRENSIONE, p.stato.eserciziGeneratiComprensione)
            setGenFlow(TipoEsercizio.PRODUZIONE, p.stato.eserciziGeneratiProduzione)
            setGenFlow(TipoEsercizio.ASCOLTO, p.stato.eserciziGeneratiAscolto)
            setGenFlow(TipoEsercizio.GRAMMATICA, p.stato.eserciziGeneratiGrammatica)
        }
    }

    /**
     * Genera nuovi esercizi via AI (Groq free) e li salva nel profilo attivo.
     * Ritorna la lista di esercizi generati (validi).
     */
    suspend fun generaEsercizi(
        tipo: TipoEsercizio,
        quanti: Int = 5,
        modello: String? = null
    ): List<EsercizioGenerato> {
        val chiave = tutorApiKey.value
        val livelloUtente = profiloAttivo.value?.stato?.progresso?.livelloCorrente ?: LivelloCEFR.A0
        val livello = livelloUtente.label
        val medico = profiloAttivo.value?.config?.mostraContestoMedico == true
        // evita duplicati: passa le domande già generate
        val esistenti = statoGenerati(tipo, profileManager.profiloAttivo()?.stato ?: ProfiloStato())
        val evita = esistenti.mapNotNull { it.domanda }
        val risultato = app.generatoreEserciziInstance.genera(
            apiKey = chiave, tipo = tipo, livello = livello,
            quanti = quanti, contestoMedico = medico, evita = evita, modello = modello
        )
        // converte in entità e persisti
        // SOGLIA DI LIVELLO: un esercizio piu' alto del profilo non viene mai
        // proposto (il modello a volte supera il livello richiesto). Si filtra qui,
        // in un solo punto, cosi' TUTTE le sezioni restano calibrate al livello.
        val ammessi = risultato.filter { LivelloCEFR.daTesto(it.livello).ordine <= livelloUtente.ordine }
        val nuovi = ammessi.map { e ->
            EsercizioGenEntity(
                id = e.id, tipo = tipo.jsonTipo, domanda = e.domanda,
                opzioni = e.opzioni, rispostaCorretta = e.rispostaCorretta,
                spiegazione = e.spiegazione,
                livello = LivelloCEFR.daTesto(e.livello).codiceBreve(),
                fraseTedesca = e.fraseTedesca, traduzioneItaliana = e.traduzioneItaliana,
                testoLettura = e.testoLettura, glossario = e.glossario
            )
        }
        val profilo = profileManager.profiloAttivo()
        if (profilo != null) {
            val nuovoStato = copiaStatoConNuovi(tipo, profilo.stato, nuovi)
            profileManager.aggiornaStatoAttivo(nuovoStato)
            setGenFlow(tipo, statoGenerati(tipo, nuovoStato))
        }
        return risultato
    }

    // ---- Memoria esercizi fatti + pre-generazione passiva in background ----

    private val _generazioneInCorso = MutableStateFlow<Set<TipoEsercizio>>(emptySet())
    val generazioneInCorso: StateFlow<Set<TipoEsercizio>> = _generazioneInCorso.asStateFlow()

    private fun copiaStatoCon(tipo: TipoEsercizio, stato: ProfiloStato, lista: List<EsercizioGenEntity>): ProfiloStato =
        when (tipo) {
            TipoEsercizio.COMPRENSIONE -> stato.copy(eserciziGeneratiComprensione = lista)
            TipoEsercizio.PRODUZIONE -> stato.copy(eserciziGeneratiProduzione = lista)
            TipoEsercizio.ASCOLTO -> stato.copy(eserciziGeneratiAscolto = lista)
            TipoEsercizio.GRAMMATICA -> stato.copy(eserciziGeneratiGrammatica = lista)
        }

    /**
     * Marca un esercizio generato come completato: non verra piu riproposto
     * (resta fuori dalla lista finche non scade il cooldown / ci sono pochi nuovi).
     */
    fun segnaEsercizioCompletato(tipo: TipoEsercizio, id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val esistenti = statoGenerati(tipo, profilo.stato)
            if (esistenti.none { it.id == id }) return@launch
            val aggiornati = esistenti.map {
                if (it.id == id) it.copy(completato = true, dataCompletamento = System.currentTimeMillis()) else it
            }
            profileManager.aggiornaStatoAttivo(copiaStatoCon(tipo, profilo.stato, aggiornati))
            setGenFlow(tipo, aggiornati)
            verificaEPromuovi()
        }
    }

    /**
     * Pre-genera esercizi in background (passivo) se il buffer di NON completati
     * scende sotto la soglia. Da chiamare all'ingresso della schermata e dopo
     * aver completato un esercizio: cosi lo studente trova sempre quiz freschi
     * senza premere "Genera altri".
     */
    fun preGeneraInBackground(tipo: TipoEsercizio, soglia: Int = 3, quanti: Int = 5) {
        if (tipo in _generazioneInCorso.value) return
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val nonCompletati = statoGenerati(tipo, profilo.stato).count { !it.completato }
            if (nonCompletati >= soglia) return@launch
            _generazioneInCorso.value = _generazioneInCorso.value + tipo
            try {
                generaEsercizi(tipo, quanti = quanti)
            } catch (e: Exception) {
                // silenzioso: riprovera al prossimo ingresso schermata
            } finally {
                _generazioneInCorso.value = _generazioneInCorso.value - tipo
            }
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
                    testGrammatica = (profilo.stato.progresso.testGrammatica + nuovoTest).limita(),
                    erroriGrammatica = nuoviErrori
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
            verificaEPromuovi()
        }
    }

    /**
     * Salva il risultato di una unita' del percorso grammaticale.
     * Chiamato una sola volta alla fine della sessione, non a ogni risposta.
     */
    fun salvaPadronanzaGrammatica(unitaId: String, corrette: Int, totali: Int) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val precedente = profilo.stato.padronanzaGrammatica[unitaId]
            val nuova = GrammaticaSyllabus.valuta(
                unitaId = unitaId,
                corrette = corrette,
                totali = totali,
                precedente = precedente
            )
            profileManager.aggiornaStatoAttivo(
                profilo.stato.copy(
                    padronanzaGrammatica = profilo.stato.padronanzaGrammatica + (unitaId to nuova)
                ),
                // Operazione rara e importante: scrive subito, cosi' il percorso
                // resta aggiornato anche se l'app viene chiusa.
                immediato = true
            )
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
                    testComprensione = (profilo.stato.progresso.testComprensione + nuovoTest).limita()
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
            verificaEPromuovi()
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
                    testProduzione = (profilo.stato.progresso.testProduzione + nuovoTest).limita()
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
            verificaEPromuovi()
        }
    }

    // ---- Ripasso errori ----

    fun eserciziErrori(): List<com.alessandro.tedesco.data.EsercizioGrammatica> {
        val profilo = profileManager.profiloAttivo() ?: return emptyList()
        val errori = profilo.stato.progresso.erroriGrammatica
        return com.alessandro.tedesco.data.GrammaticaB1.esercizi.filter { it.domanda in errori }
    }

    // ==================== QUADERNO ERRORI (SRS) ====================

    /** Tutti gli errori registrati nel profilo attivo. */
    val errori: StateFlow<List<ErroreEntity>> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id?.let { r.profili[it]?.stato?.errori }.orEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Errori non ancora risolti e arrivati alla data di ripasso. */
    val erroriDaRipassare: StateFlow<List<ErroreEntity>> = errori
        .map { lista -> lista.filter { !it.risolto && ErroriSRS.daRipassare(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Registra un errore reale (grammatica, traduzione, dettato...).
     * Se lo stesso errore esiste gia' non lo duplica: azzera la scadenza SRS,
     * cosi' torna subito nel ripasso.
     */
    fun registraErrore(
        tipo: String,
        domanda: String,
        rispostaCorretta: String,
        rispostaData: String = "",
        spiegazione: String = "",
        livello: String = "A1"
    ) {
        if (domanda.isBlank() || rispostaCorretta.isBlank()) return
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val adesso = System.currentTimeMillis()
            val nuovo = ErroriSRS.nuovo(
                tipo = tipo,
                domanda = domanda,
                rispostaCorretta = rispostaCorretta,
                rispostaData = rispostaData,
                spiegazione = spiegazione,
                livello = livello,
                now = adesso
            )
            val esistenti = profilo.stato.errori
            val indice = esistenti.indexOfFirst { it.chiave == nuovo.chiave }
            val aggiornati = if (indice >= 0) {
                esistenti.toMutableList().apply {
                    this[indice] = esistenti[indice].copy(
                        rispostaData = rispostaData,
                        spiegazione = spiegazione.ifBlank { esistenti[indice].spiegazione },
                        ultimaVolta = adesso,
                        ripetizioni = 0,
                        intervalloGiorni = 1,
                        prossimaRipasso = adesso + 24L * 60L * 60L * 1000L,
                        risolto = false
                    )
                }
            } else {
                (esistenti + nuovo).takeLast(300)
            }
            profileManager.aggiornaStatoAttivo(profilo.stato.copy(errori = aggiornati))
        }
    }

    /** Risposta al ripasso di un errore: saputo = richiamo riuscito. */
    fun rispondiErrore(id: String, saputo: Boolean) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val aggiornati = profilo.stato.errori.map { errore ->
                if (errore.id == id) ErroriSRS.rispondi(errore, saputo) else errore
            }
            profileManager.aggiornaStatoAttivo(profilo.stato.copy(errori = aggiornati))
        }
    }

    fun eliminaErrore(id: String) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            profileManager.aggiornaStatoAttivo(
                profilo.stato.copy(errori = profilo.stato.errori.filterNot { it.id == id })
            )
        }
    }

    fun svuotaErroriRisolti() {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            profileManager.aggiornaStatoAttivo(
                profilo.stato.copy(errori = profilo.stato.errori.filterNot { it.risolto })
            )
        }
    }

    // ==================== PLACEMENT TEST ====================

    val placementFatto: StateFlow<Boolean> = profileManager.repositoryFlow
        .map { r ->
            val id = r.profiloAttivoId
            id?.let { r.profili[it]?.stato?.placementFatto } ?: false
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Imposta il livello emerso dal test di ingresso (o scelto a mano). */
    fun impostaLivello(livello: LivelloCEFR) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            profileManager.aggiornaStatoAttivo(
                profilo.stato.copy(
                    progresso = profilo.stato.progresso.copy(livelloCorrente = livello),
                    placementFatto = true
                )
            )
            _messaggio.value = "Livello impostato: ${livello.label}"
        }
    }

    /** Salta il test di ingresso: il livello resta quello attuale. */
    fun saltaPlacement() {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            profileManager.aggiornaStatoAttivo(profilo.stato.copy(placementFatto = true))
        }
    }

    // ==================== PUNTEGGI LEZIONI ====================

    /** Miglior punteggio (0-100) della lezione: serve al gate morbido. */
    fun registraPunteggioLezione(lezione: Int, punteggio: Int) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            val precedente = profilo.stato.progresso.punteggiLezioni[lezione] ?: -1
            val migliore = maxOf(precedente, punteggio.coerceIn(0, 100))
            profileManager.aggiornaStatoAttivo(
                profilo.stato.copy(
                    progresso = profilo.stato.progresso.copy(
                        punteggiLezioni = profilo.stato.progresso.punteggiLezioni + (lezione to migliore)
                    )
                )
            )
        }
    }

    /** Aggiunge alla Wortliste personale una parola curata (B1). */
    fun aggiungiParolaWortliste(tedesco: String, italiano: String, esempio: String, livello: String = "B1") {
        val articolo = listOf("der ", "die ", "das ").firstOrNull { tedesco.startsWith(it) }
        val parola = articolo?.let { tedesco.removePrefix(it) } ?: tedesco
        aggiungiParolaCustom(
            german = parola,
            italian = italiano,
            example = esempio,
            article = articolo?.trim(),
            pronunciation = null,
            lesson = 0,
            tags = "Wortliste"
        )
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
            // In free mode la chiave puo' essere vuota: TutorService usa l'endpoint free.
            val chiave = tutorApiKey.value
            _correzioneInCorso.value = true
            try {
                val profilo = profileManager.profiloAttivo() ?: return@launch
                val livello = profilo.stato.progresso.livelloCorrente.label.substringBefore(" ")
                val lezione = lezioneCorrente.value
                // Valutazione STRUTTURATA (criteri ufficiali Goethe), non solo testo.
                val rispostaGrezza = app.tutorServiceInstance.valutaProduzione(
                    apiKey = chiave,
                    testoStudente = testo,
                    livello = livello,
                    lezione = lezione,
                    modello = _modelloTutor.value
                )
                val valutazione = ParserValutazione.parse(rispostaGrezza)
                val numParole = CalcoloProduzione.contaParole(testo)
                val v = CalcoloProduzione.costruisci(
                    numErrori = valutazione.numErrori,
                    numParole = numParole,
                    livello = livello,
                    erfuellungTutor = valutazione.erfuellung,
                    kohaerenzTutor = valutazione.kohaerenz,
                    wortschatzTutor = valutazione.wortschatz,
                    correzione = valutazione.correzione,
                    now = System.currentTimeMillis()
                )
                // Testo leggibile degli errori + commento, per la schermata
                val erroriLetti = ParserValutazione.elencaErrori(rispostaGrezza)
                val correzione = buildString {
                    if (erroriLetti.isNotEmpty()) {
                        append("**Errori trovati (${erroriLetti.size})**\n\n")
                        append(erroriLetti.joinToString("\n\n"))
                    } else {
                        append("**Nessun errore trovato. Ottimo lavoro!**\n")
                    }
                    if (valutazione.correzione.isNotBlank()) {
                        append("\n\n").append(valutazione.correzione)
                    }
                }
                // Salva la valutazione nel profilo (entra nelle statistiche)
                val statoCorrente = profileManager.statoAttivo()
                profileManager.aggiornaStatoAttivo(
                    statoCorrente.copy(
                        progresso = statoCorrente.progresso.copy(
                            valutazioniProduzione = (statoCorrente.progresso.valutazioniProduzione + v).limita()
                        )
                    )
                )
                val entry = FeedbackEntry(
                    id = "fb_${System.currentTimeMillis()}",
                    testo = testo,
                    timestamp = System.currentTimeMillis(),
                    corretto = true,
                    correzione = correzione
                )
                // Cap: la cronologia delle correzioni cresce altrimenti all'infinito.
                _risposte.value = (listOf(entry) + _risposte.value).takeLast(LIMITE_RISPOSTE)
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

    /** Salva le conversazioni (tutor/speaking) nel profilo. */
    fun saveConversazioni(profiloId: String, tipo: String, conversazioni: List<ConversazioneEntry>) {
        viewModelScope.launch {
            val profilo = profileManager.profiloAttivo() ?: return@launch
            if (profilo.id != profiloId) return@launch
            val nuovoStato = profilo.stato.copy(
                conversazioni = profilo.stato.conversazioni
                    .filter { it.tipo != tipo } + conversazioni
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
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
            // Goethe B1 e' MODULARE: ogni modulo e' superato con >= 60% (Bestehensgrenze
            // ufficiale). Il "complessivo" NON e' una media che salva un modulo debole:
            // e' il minimo tra i moduli, cosi' riflette il criterio reale "tutti >= 60".
            val moduli = listOf(punteggioLesen, punteggioHoeren, punteggioSchreiben, punteggioSprechen)
            val nuovoTest = com.alessandro.tedesco.data.TestB1(
                id = "test_${System.currentTimeMillis()}",
                data = System.currentTimeMillis(),
                punteggioLesen = punteggioLesen,
                punteggioHoeren = punteggioHoeren,
                punteggioSchreiben = punteggioSchreiben,
                punteggioSprechen = punteggioSprechen,
                punteggioComplessivo = moduli.min()  // criterio "maglia piu' debole" (ufficiale)
            )
            val nuovoStato = profilo.stato.copy(
                progresso = profilo.stato.progresso.copy(
                    testB1 = (profilo.stato.progresso.testB1 + nuovoTest).limita()
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
                    testAdattivi = (profilo.stato.progresso.testAdattivi + nuovoTest).limita(),
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
                    testAscolto = (profilo.stato.progresso.testAscolto + nuovoTest).limita()
                )
            )
            profileManager.aggiornaStatoAttivo(nuovoStato)
        }
    }

    companion object {
        const val GIORNI_PER_LEZIONE = 2

        // I tetti alle liste storiche vivono in data/Storico.kt (testabili).
        /** Cronologia delle correzioni del tutor tenuta in memoria (piu' il backup). */
        private const val LIMITE_RISPOSTE = 60
        private const val MS_PER_GIORNO = 24L * 60 * 60 * 1000
        fun formattaData(ts: Long): String {
            if (ts == 0L) return "mai"
            return SimpleDateFormat("d/M HH:mm", Locale.ITALIAN).format(Date(ts))
        }
    }
}

/** Stato della generazione dinamica di una lezione. */
sealed interface StatoLezione {
    data object Idle : StatoLezione
    data object InCorso : StatoLezione
    data class Fatto(val paroleAggiunte: Int) : StatoLezione
    data class Errore(val messaggio: String) : StatoLezione
}
