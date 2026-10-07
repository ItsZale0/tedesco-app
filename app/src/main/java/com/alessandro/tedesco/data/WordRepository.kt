package com.alessandro.tedesco.data

import android.content.Context
import com.alessandro.tedesco.data.local.FeedLogEntity
import com.alessandro.tedesco.data.local.GuidaEntity
import com.alessandro.tedesco.data.local.LessonEntity
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.SezioneEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import com.alessandro.tedesco.data.remote.FeedDto
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.data.remote.WordDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.comparisons.compareBy
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.data.local.SessioneEntity
import com.alessandro.tedesco.data.local.TappaEntity
import com.alessandro.tedesco.data.local.CertificazioneEntity
import com.alessandro.tedesco.data.local.RisorsaEntity

sealed class SyncResult {
    data object NotModified : SyncResult()
    data class Updated(val newWords: Int, val updatedWords: Int, val total: Int) : SyncResult()
    data class Failed(val message: String) : SyncResult()
}

/**
 * Repository delle parole del corso.
 *
 * I dati (parole, ripassi, guida) sono letti e salvati nello stato del
 * profilo attivo: ogni profilo ha il proprio vocabolario e i propri progressi.
 * Quando l'utente cambia profilo, il repository ricarica automaticamente.
 */
class WordRepository(
    context: Context,
    private val service: FeedService,
    private val json: Json,
    private val io: CoroutineDispatcher,
    private val profileManager: ProfileManager
) {

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    private val _reviews = MutableStateFlow<Map<String, ReviewEntity>>(emptyMap())
    private val _feedLog = MutableStateFlow<List<FeedLogEntity>>(emptyList())
    private val _guida = MutableStateFlow<GuidaEntity?>(null)
    private val _lezioneContenuto = MutableStateFlow("")
    private val _lezioni = MutableStateFlow<List<LessonEntity>>(emptyList())
    private val _piano = MutableStateFlow<PianoEntity?>(null)
    private val _progressoFeed = MutableStateFlow<ProgressoFeedEntity?>(null)
    private val _sessioni = MutableStateFlow<List<SessioneEntity>>(emptyList())

    val wordsFlow: Flow<List<WordEntity>> = _words
    val reviewsFlow: Flow<Map<String, ReviewEntity>> = _reviews
    val feedLogFlow: Flow<List<FeedLogEntity>> = _feedLog
    val guidaFlow: Flow<GuidaEntity?> = _guida
    val pianoFlow: Flow<PianoEntity?> = _piano
    val progressoFeedFlow: Flow<ProgressoFeedEntity?> = _progressoFeed
    val sessioniFlow: Flow<List<SessioneEntity>> = _sessioni

    init {
        // Ad ogni cambio di profilo (o al primo caricamento) ricarica i dati
        CoroutineScope(io).launch {
            var ultimoProfilo: String? = "__init__"
            profileManager.repositoryFlow.collect { repo ->
                val id = repo.profiloAttivoId
                if (id != ultimoProfilo) {
                    ultimoProfilo = id
                    caricaDaProfilo()
                }
            }
        }
    }

    private fun caricaDaProfilo() {
        val stato = profileManager.statoAttivo()
        _words.value = stato.parole
        _reviews.value = stato.reviews
        _feedLog.value = stato.feedLog
        _guida.value = stato.guida
        _piano.value = stato.piano
        _progressoFeed.value = stato.progressoFeed
        _sessioni.value = stato.sessioni
    }

    private suspend fun salvaSuProfilo() {
        profileManager.aggiornaStatoAttivo(
            ProfiloStato(
                parole = _words.value,
                reviews = _reviews.value,
                feedLog = _feedLog.value,
                guida = _guida.value,
                piano = _piano.value,
                progressoFeed = _progressoFeed.value,
                sessioni = _sessioni.value,
                progresso = profileManager.statoAttivo().progresso,
                etag = profileManager.statoAttivo().etag,
                lastSync = System.currentTimeMillis()
            )
        )
    }

    fun observeGuida(): Flow<GuidaEntity?> = guidaFlow
    fun observeLezioneContenuto(): Flow<String> = _lezioneContenuto
    fun observeLezioni(): Flow<List<LessonEntity>> = _lezioni

    /** Aggiorna il contenuto mostrato quando l'utente cambia lezione. */
    fun aggiornaLezioneContenuto(numero: Int) {
        val lezione = _lezioni.value.firstOrNull { it.numero == numero }
            ?: _lezioni.value.firstOrNull()
        lezione?.let { _lezioneContenuto.value = it.contenuto }
    }

    fun observeWords() = wordsFlow
        .map { it.filter { !it.archived }.sortedWith(compareBy({ it.lesson }, { it.german })) }

    fun observeWordsByLesson(lesson: Int) = wordsFlow
        .map { it.filter { !it.archived && it.lesson == lesson }.sortedBy { it.german } }

    fun observeLessons(): Flow<List<LessonEntity>> = _lezioni

    fun observeWordsByLevel(level: String): Flow<List<WordEntity>> = wordsFlow
        .map { it.filter { !it.archived && it.level == level }.sortedBy { it.german } }

    fun observeLevels(): Flow<List<String>> = wordsFlow
        .map { it.filter { !it.archived }.map { it.level }.distinct().sorted() }

    fun observeLastSync() = feedLogFlow.map { it.maxByOrNull { log -> log.syncedAt } }

    fun observeDueCount(): Flow<Int> = reviewsFlow.map { reviews ->
        val now = System.currentTimeMillis()
        reviews.values.count { it.dueAt <= now }
    }

    suspend fun sync(): SyncResult = withContext(io) {
        val url = profileManager.getFeedUrl()
        if (url.isBlank()) {
            return@withContext SyncResult.Failed("URL del feed non configurato")
        }

        try {
            val etag = profileManager.statoAttivo().etag
            val response = service.getRaw(url)

            if (response.code == 304) return@withContext SyncResult.NotModified
            if (!response.isSuccessful) {
                return@withContext SyncResult.Failed("HTTP ${response.code}")
            }

            val newEtag = response.headers["ETag"]
            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                return@withContext SyncResult.Failed("Risposta vuota dal server")
            }

            val feed = json.decodeFromString(FeedDto.serializer(), body)

            if (etag.isNotBlank() && newEtag != null && etag == newEtag && feed.words.isEmpty()) {
                return@withContext SyncResult.NotModified
            }

            val (added, changed) = merge(feed)

            _lezioni.value = feed.lessons.map { LessonEntity(it.numero, it.titolo, it.contenuto, it.audioUrl) }
            val lezioneCorrente = profileManager.statoAttivo().progresso.lezioneCorrente
            val lezione = feed.lessons.firstOrNull { it.numero == lezioneCorrente }
                ?: feed.lessons.firstOrNull()
            lezione?.let { _lezioneContenuto.value = it.contenuto }
            feed.guida?.let { g ->
                _guida.value = GuidaEntity(
                    titolo = g.titolo,
                    docUrl = g.docUrl,
                    sezioni = g.sezioni.map { SezioneEntity(it.titolo, it.testo) }
                )
            }
            feed.piano?.let { p ->
                _piano.value = PianoEntity(
                    obiettivo = p.obiettivo,
                    orizzonte = p.orizzonte,
                    tappe = p.tappe.map { TappaEntity(it.nome, it.descrizione, it.lezioni, it.stato) },
                    certificazioni = p.certificazioni.map {
                        CertificazioneEntity(it.nome, it.ente, it.livello, it.note, it.url)
                    },
                    risorse = p.risorse.map { RisorsaEntity(it.nome, it.tipo, it.nota) }
                )
            }
            feed.progresso?.let { pr ->
                _progressoFeed.value = ProgressoFeedEntity(
                    lezioneCorrente = pr.lezioneCorrente,
                    streakCorrente = pr.streakCorrente,
                    streakRecord = pr.streakRecord,
                    totaleFatte = pr.totaleFatte,
                    paroleTotali = pr.paroleTotali,
                    paroleMature = pr.paroleMature
                )
            }
            _sessioni.value = feed.sessioni.map {
                SessioneEntity(it.tipo, it.titolo, it.descrizione, it.durata)
            }

            val logEntry = FeedLogEntity(
                syncedAt = System.currentTimeMillis(),
                newWords = added,
                updatedWords = changed,
                status = "ok",
                message = "${feed.words.size} parole · guida ${feed.guida?.sezioni?.size ?: 0} sez."
            )
            _feedLog.value = (_feedLog.value + logEntry).takeLast(30)

            salvaSuProfiloConEtag(newEtag)

            SyncResult.Updated(added, changed, feed.words.size)
        } catch (e: Exception) {
            val logEntry = FeedLogEntity(
                syncedAt = System.currentTimeMillis(),
                newWords = 0,
                updatedWords = 0,
                status = "errore",
                message = e.message?.take(80)
            )
            _feedLog.value = (_feedLog.value + logEntry).takeLast(30)
            salvaSuProfilo()
            SyncResult.Failed(e.message ?: "Errore sconosciuto")
        }
    }

    private suspend fun salvaSuProfiloConEtag(nuovoEtag: String?) {
        val corrente = profileManager.statoAttivo()
        profileManager.aggiornaStatoAttivo(
            corrente.copy(
                parole = _words.value,
                reviews = _reviews.value,
                feedLog = _feedLog.value,
                guida = _guida.value,
                etag = nuovoEtag ?: corrente.etag,
                lastSync = System.currentTimeMillis()
            )
        )
    }

    private suspend fun merge(feed: FeedDto): Pair<Int, Int> = withContext(io) {
        var added = 0
        var changed = 0
        val now = System.currentTimeMillis()
        val toUpsert = mutableListOf<WordEntity>()
        val newWordIds = mutableSetOf<String>()
        val currentWords = _words.value.associateBy { it.id }.toMutableMap()
        val currentReviews = _reviews.value.toMutableMap()

        for (dto in feed.words) {
            val entity = dto.toEntity(now)
            val esistente = currentWords[dto.id]

            when {
                esistente == null -> {
                    toUpsert.add(entity)
                    newWordIds.add(dto.id)
                    added++
                }
                esistente != entity -> {
                    toUpsert.add(entity)
                    changed++
                }
                else -> Unit
            }
        }

        if (toUpsert.isNotEmpty()) {
            toUpsert.forEach { currentWords[it.id] = it }
            _words.value = currentWords.values.toList()
        }

        // Prima le parole, poi i ripassi (evita problemi di coerenza)
        for (dto in feed.words) {
            if (dto.id in newWordIds) {
                currentReviews[dto.id] = ReviewEntity(
                    wordId = dto.id,
                    easeFactor = 2.5f,
                    intervalDays = 0,
                    repetitions = 0,
                    lapses = 0,
                    dueAt = now,
                    lastReviewedAt = null
                )
            }
        }
        _reviews.value = currentReviews
        added to changed
    }

    suspend fun setArchived(id: String, archived: Boolean) = withContext(io) {
        val currentWords = _words.value.associateBy { it.id }.toMutableMap()
        currentWords[id]?.let { word ->
            currentWords[id] = word.copy(archived = archived)
            _words.value = currentWords.values.toList()
            salvaSuProfilo()
        }
    }

    /**
     * Parole in scadenza, al massimo [limite] per sessione.
     * Un A0 con 100 parole nuove avrebbe 100 carte in scadenza il primo giorno:
     * una sessione così non si finisce mai. Meglio poche carte al giorno.
     */
    suspend fun dueReviews(limite: Int = 20): List<ReviewEntity> = withContext(io) {
        val now = System.currentTimeMillis()
        _reviews.value.values
            .filter { it.dueAt <= now }
            .sortedBy { it.dueAt }
            .take(limite)
    }

    suspend fun answer(wordId: String, knewIt: Boolean) = withContext(io) {
        val reviews = _reviews.value.toMutableMap()
        val r = reviews[wordId] ?: return@withContext

        val updated = if (knewIt) {
            val next = INTERVALS.getOrElse(r.repetitions) { INTERVALS.last() }
            r.copy(
                repetitions = r.repetitions + 1,
                intervalDays = next,
                easeFactor = (r.easeFactor + 0.05f).coerceAtMost(3.0f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(next.toLong()),
                lastReviewedAt = System.currentTimeMillis()
            )
        } else {
            r.copy(
                repetitions = 0,
                intervalDays = 1,
                lapses = r.lapses + 1,
                easeFactor = max(1.3f, r.easeFactor - 0.2f),
                dueAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                lastReviewedAt = System.currentTimeMillis()
            )
        }
        reviews[wordId] = updated
        _reviews.value = reviews
        salvaSuProfilo()
    }

    /** Svuota i dati del profilo attivo (parole, ripassi, log, guida). */
    suspend fun resetAll() = withContext(io) {
        _words.value = emptyList()
        _reviews.value = emptyMap()
        _feedLog.value = emptyList()
        _guida.value = null
        val corrente = profileManager.statoAttivo()
        profileManager.aggiornaStatoAttivo(
            corrente.copy(
                parole = emptyList(),
                reviews = emptyMap(),
                feedLog = emptyList(),
                guida = null,
                etag = ""
            )
        )
    }

    /** Aggiunge una parola personalizzata (solo se il profilo lo consente). */
    suspend fun addCustomWord(
        german: String,
        italian: String,
        example: String = "",
        article: String? = null,
        pronunciation: String? = null,
        lesson: Int = 0,
        tags: String = ""
    ): WordEntity = withContext(io) {
        if (!profileManager.isCustomWordsEnabled()) {
            throw UnsupportedOperationException("Parole personalizzate non abilitate per questo profilo")
        }
        val now = System.currentTimeMillis()
        val word = WordEntity(
            id = "custom_${now}_${german.hashCode()}",
            german = german.trim(),
            italian = italian.trim(),
            example = example.trim(),
            article = article?.trim()?.ifBlank { null },
            pronunciation = pronunciation?.trim()?.ifBlank { null },
            level = "A1",
            lesson = lesson,
            tags = tags.trim(),
            archived = false,
            createdAt = now,
            source = WordSource.CUSTOM
        )
        val currentWords = _words.value.toMutableList()
        currentWords.removeAll { it.german.equals(word.german, ignoreCase = true) }
        currentWords.add(0, word)
        _words.value = currentWords

        val currentReviews = _reviews.value.toMutableMap()
        currentReviews[word.id] = ReviewEntity(
            wordId = word.id,
            easeFactor = 2.5f,
            intervalDays = 0,
            repetitions = 0,
            lapses = 0,
            dueAt = now,
            lastReviewedAt = null
        )
        _reviews.value = currentReviews

        salvaSuProfilo()
        word
    }

    /** Elimina una parola personalizzata. */
    suspend fun deleteCustomWord(id: String) = withContext(io) {
        _words.value = _words.value.filterNot { it.id == id }
        _reviews.value = _reviews.value - id
        salvaSuProfilo()
    }

    companion object {
        val INTERVALS = listOf(1, 3, 7, 16, 35, 75, 150, 300)
    }
}

private fun WordDto.toEntity(now: Long): WordEntity = WordEntity(
    id = this.id,
    german = this.german,
    italian = this.italian,
    example = this.example,
    article = this.article,
    pronunciation = this.pronunciation,
    level = this.level,
    lesson = this.lesson,
    tags = this.tags.joinToString(","),
    archived = this.archived,
    createdAt = now,
    source = WordSource.SYNCED
)
