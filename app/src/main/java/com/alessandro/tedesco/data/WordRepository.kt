package com.alessandro.tedesco.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alessandro.tedesco.data.local.FeedLogEntity
import com.alessandro.tedesco.data.local.GuidaEntity
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.SezioneEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.remote.FeedDto
import com.alessandro.tedesco.data.local.WordSource
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.data.remote.WordDto
import com.alessandro.tedesco.settings.SettingsStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.comparisons.compareBy

private const val VOCAB_DATASTORE = "vocab_data"

private val Context.vocabDataStore by preferencesDataStore(name = VOCAB_DATASTORE)

private object Keys {
    val VOCAB_JSON = stringPreferencesKey("vocab_json")
}

sealed class SyncResult {
    data object NotModified : SyncResult()
    data class Updated(val newWords: Int, val updatedWords: Int, val total: Int) : SyncResult()
    data class Failed(val message: String) : SyncResult()
}

@Serializable
private data class VocabData(
    val words: List<WordEntity> = emptyList(),
    val reviews: Map<String, ReviewEntity> = emptyMap(),
    val feedLog: List<FeedLogEntity> = emptyList(),
    val guida: GuidaEntity? = null
)

class WordRepository(
    private val context: Context,
    private val service: FeedService,
    private val settings: SettingsStore,
    private val json: Json,
    private val io: CoroutineDispatcher
) {

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    private val _reviews = MutableStateFlow<Map<String, ReviewEntity>>(emptyMap())
    private val _feedLog = MutableStateFlow<List<FeedLogEntity>>(emptyList())
    private val _guida = MutableStateFlow<GuidaEntity?>(null)

    val wordsFlow: Flow<List<WordEntity>> = _words
    val reviewsFlow: Flow<Map<String, ReviewEntity>> = _reviews
    val feedLogFlow: Flow<List<FeedLogEntity>> = _feedLog
    val guidaFlow: Flow<GuidaEntity?> = _guida

    init {
        loadFromDataStore()
    }

    private fun loadFromDataStore() {
        CoroutineScope(io).launch {
            val data = context.vocabDataStore.data.first()
            val jsonStr = data[Keys.VOCAB_JSON] ?: "{}"
            val vocabData = json.decodeFromString(VocabData.serializer(), jsonStr)
            _words.value = vocabData.words
            _reviews.value = vocabData.reviews
            _feedLog.value = vocabData.feedLog
            _guida.value = vocabData.guida
        }
    }

    private suspend fun saveToDataStore() = withContext(io) {
        val vocabData = VocabData(
            words = _words.value,
            reviews = _reviews.value,
            feedLog = _feedLog.value,
            guida = _guida.value
        )
        val jsonStr = json.encodeToString(VocabData.serializer(), vocabData)
        context.vocabDataStore.edit {
            it[Keys.VOCAB_JSON] = jsonStr
        }
    }

    fun observeGuida(): Flow<GuidaEntity?> = guidaFlow

    fun observeWords() = wordsFlow
        .map { it.filter { !it.archived }.sortedWith(compareBy({ it.lesson }, { it.german })) }

    fun observeWordsByLesson(lesson: Int) = wordsFlow
        .map { it.filter { !it.archived && it.lesson == lesson }.sortedBy { it.german } }

    fun observeLessons() = wordsFlow
        .map { it.filter { !it.archived }.map { it.lesson }.distinct().sorted() }

    fun observeLastSync() = feedLogFlow
        .map { it.maxByOrNull { it.syncedAt } }

    fun observeDueCount(): Flow<Int> = reviewsFlow
        .map { reviews ->
            val now = System.currentTimeMillis()
            reviews.values.count { it.dueAt <= now }
        }

    suspend fun sync(): SyncResult = withContext(io) {
        val url = settings.feedUrl()
        if (url.isBlank()) {
            return@withContext SyncResult.Failed("URL del feed non configurato")
        }

        try {
            val etag = settings.etag()
            val response = service.getRaw(url)

            if (response.code == 304) {
                return@withContext SyncResult.NotModified
            }
            if (!response.isSuccessful) {
                return@withContext SyncResult.Failed("HTTP ${response.code}")
            }

            val newEtag = response.headers["ETag"]
            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                return@withContext SyncResult.Failed("Risposta vuota dal server")
            }

            val feed = json.decodeFromString(FeedDto.serializer(), body)

            if (etag != null && newEtag != null && etag == newEtag && feed.words.isEmpty()) {
                return@withContext SyncResult.NotModified
            }

            val (added, changed) = merge(feed)

            // la guida arriva dallo stesso feed: salviamola insieme alle parole
            feed.guida?.let { g ->
                _guida.value = GuidaEntity(
                    titolo = g.titolo,
                    docUrl = g.docUrl,
                    sezioni = g.sezioni.map { SezioneEntity(it.titolo, it.testo) }
                )
            }

            newEtag?.let { settings.setEtag(it) }
            settings.setLastSync(System.currentTimeMillis())

            val logEntry = FeedLogEntity(
                syncedAt = System.currentTimeMillis(),
                newWords = added,
                updatedWords = changed,
                status = "ok",
                message = "${feed.words.size} parole · guida ${feed.guida?.sezioni?.size ?: 0} sez."
            )
            _feedLog.value = (_feedLog.value + logEntry).takeLast(30)
            saveToDataStore()

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
            saveToDataStore()
            SyncResult.Failed(e.message ?: "Errore sconosciuto")
        }
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

        for (dto in feed.words) {
            if (dto.id in newWordIds) {
                val review = ReviewEntity(
                    wordId = dto.id,
                    easeFactor = 2.5f,
                    intervalDays = 0,
                    repetitions = 0,
                    lapses = 0,
                    dueAt = now,
                    lastReviewedAt = null
                )
                currentReviews[dto.id] = review
            }
        }
        _reviews.value = currentReviews
        saveToDataStore()
        added to changed
    }

    suspend fun setArchived(id: String, archived: Boolean) = withContext(io) {
        val currentWords = _words.value.associateBy { it.id }.toMutableMap()
        currentWords[id]?.let { word ->
            currentWords[id] = word.copy(archived = archived)
            _words.value = currentWords.values.toList()
            saveToDataStore()
        }
    }

    /**
     * Parole in scadenza, al massimo [limite] per sessione.
     * Un A0 con 100 parole nuove avrebbe 100 carte in scadenza il primo giorno:
     * una sessione cosi' non si finisce mai. Meglio poche carte al giorno.
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
                lapses = r.lapses,
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
        saveToDataStore()
    }

    suspend fun resetAll() = withContext(io) {
        _words.value = emptyList()
        _reviews.value = emptyMap()
        _feedLog.value = emptyList()
        saveToDataStore()
        settings.setEtag("")
    }

    /** Aggiunge una parola personalizzata dall'utente */
    suspend fun addCustomWord(
        german: String,
        italian: String,
        example: String = "",
        article: String? = null,
        pronunciation: String? = null,
        lesson: Int = 0,
        tags: String = ""
    ) = withContext(io) {
        val now = System.currentTimeMillis()
        val id = "custom_${now}_${german.hashCode()}"
        val word = WordEntity(
            id = id,
            german = german.trim(),
            italian = italian.trim(),
            example = example.trim(),
            article = article,
            pronunciation = pronunciation,
            level = "A1",
            lesson = lesson,
            tags = tags,
            archived = false,
            createdAt = now,
            source = WordSource.CUSTOM
        )
        val currentWords = _words.value.toMutableList()
        currentWords.removeAll { it.german.equals(word.german, ignoreCase = true) }
        currentWords.add(0, word)
        _words.value = currentWords

        val review = ReviewEntity(
            wordId = word.id,
            easeFactor = 2.5f,
            intervalDays = 0,
            repetitions = 0,
            lapses = 0,
            dueAt = now,
            lastReviewedAt = null
        )
        val currentReviews = _reviews.value.toMutableMap()
        currentReviews[word.id] = review
        _reviews.value = currentReviews

        saveToDataStore()
        word
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
    createdAt = now
)