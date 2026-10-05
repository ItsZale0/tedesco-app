package com.alessandro.tedesco.data

import android.content.Context
import androidx.room.withTransaction
import com.alessandro.tedesco.data.local.AppDatabase
import com.alessandro.tedesco.data.local.FeedLogEntity
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.remote.FeedDto
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.data.remote.WordDto
import com.alessandro.tedesco.di.IoDispatcher
import com.alessandro.tedesco.settings.SettingsStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

sealed class SyncResult {
    data object NotModified : SyncResult()
    data class Updated(val newWords: Int, val updatedWords: Int, val total: Int) : SyncResult()
    data class Failed(val message: String) : SyncResult()
}

@Singleton
class WordRepository @Inject constructor(
    private val db: AppDatabase,
    private val service: FeedService,
    private val settings: SettingsStore,
    private val json: Json,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    private val wordDao = db.wordDao()
    private val reviewDao = db.reviewDao()
    private val feedLogDao = db.feedLogDao()

    fun observeWords() = wordDao.observeAll()
    fun observeWordsByLesson(lesson: Int) = wordDao.observeByLesson(lesson)
    fun observeLessons() = wordDao.observeLessons()
    fun observeLastSync() = feedLogDao.observeLast()
    fun observeDueCount(): kotlinx.coroutines.flow.Flow<Int> =
        reviewDao.observeDueCount(System.currentTimeMillis())

    suspend fun sync(): SyncResult = withContext(io) {
        val url = settings.feedUrl()
        if (url.isBlank()) {
            return@withContext SyncResult.Failed("URL del feed non configurato")
        }

        try {
            val etag = settings.etag()
            val response = service.getRaw(url)

            if (response.code() == 304) {
                return@withContext SyncResult.NotModified
            }
            if (!response.isSuccessful) {
                return@withContext SyncResult.Failed("HTTP ${response.code()}")
            }

            val newEtag = response.headers()["ETag"]
            val body = response.body()?.string()
            if (body.isNullOrBlank()) {
                return@withContext SyncResult.Failed("Risposta vuota dal server")
            }

            // UTF-8 esplicito: il file contiene ä ö ü e ß
            val feed = json.decodeFromString(FeedDto.serializer(), body)

            if (etag != null && newEtag != null && etag == newEtag &&
                feed.words.isEmpty()
            ) {
                return@withContext SyncResult.NotModified
            }

            val (added, changed) = merge(feed)

            newEtag?.let { settings.setEtag(it) }
            settings.setLastSync(System.currentTimeMillis())

            feedLogDao.insert(
                FeedLogEntity(
                    syncedAt = System.currentTimeMillis(),
                    newWords = added,
                    updatedWords = changed,
                    status = "ok",
                    message = "L${feed.lesson} · ${feed.words.size} parole"
                )
            )

            SyncResult.Updated(added, changed, feed.words.size)
        } catch (e: Exception) {
            feedLogDao.insert(
                FeedLogEntity(
                    syncedAt = System.currentTimeMillis(),
                    newWords = 0,
                    updatedWords = 0,
                    status = "errore",
                    message = e.message?.take(80)
                )
            )
            SyncResult.Failed(e.message ?: "Errore sconosciuto")
        }
    }

    /** Merge transazionale: se l'app viene chiusa a meta', il database
     *  resta integro. Gli id gia' presenti non creano duplicati.
     */
    private suspend fun merge(feed: FeedDto): Pair<Int, Int> = db.withTransaction {
        var added = 0
        var changed = 0
        val now = System.currentTimeMillis()
        val toUpsert = mutableListOf<WordEntity>()
        val newWordIds = mutableSetOf<String>()

        // FASE 1: inserisci prima TUTTE le parole (senza review)
        for (dto in feed.words) {
            val entity = dto.toEntity(now)
            val esistente = wordDao.getById(dto.id)

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
            wordDao.upsertAll(toUpsert)
        }

        // FASE 2: ora che le parole esistono, inserisci le review
        for (dto in feed.words) {
            if (dto.id in newWordIds) {
                reviewDao.upsert(
                    ReviewEntity(
                        wordId = dto.id,
                        easeFactor = 2.5f,
                        intervalDays = 0,
                        repetitions = 0,
                        lapses = 0,
                        dueAt = now,
                        lastReviewedAt = null
                    )
                )
            }
        }

        added to changed
    }

    // --- azioni dell'utente ---

    suspend fun setArchived(id: String, archived: Boolean) {
        wordDao.setArchived(id, archived)
    }

    suspend fun dueReviews(): List<ReviewEntity> =
        reviewDao.dueNow(System.currentTimeMillis())

    /** SM-2 semplificato. Intervalli in giorni:
     * 1 -> 3 -> 7 -> 16 -> 35 -> 75 -> 150 -> 300
     */
    suspend fun answer(wordId: String, knewIt: Boolean) = withContext(io) {
        val r = reviewDao.all().firstOrNull { it.wordId == wordId } ?: return@withContext

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
        reviewDao.update(updated)
    }

    suspend fun resetAll() = withContext(io) {
        db.clearAllTables()
        settings.setEtag("")
    }

    companion object {
        val INTERVALS = listOf(1, 3, 7, 16, 35, 75, 150, 300)
    }
}

private fun WordDto.toEntity(now: Long) = WordEntity(
    id = id,
    german = german,
    italian = italian,
    example = example,
    article = article,
    pronunciation = pronunciation,
    level = level,
    lesson = lesson,
    tags = tags.joinToString(","),
    archived = archived,
    createdAt = now
)