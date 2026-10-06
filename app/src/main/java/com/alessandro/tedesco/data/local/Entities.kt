package com.alessandro.tedesco.data.local

import kotlinx.serialization.Serializable

@Serializable
data class WordEntity(
    val id: String,
    val german: String,
    val italian: String,
    val example: String,
    val article: String?,
    val pronunciation: String?,
    val level: String,
    val lesson: Int,
    val tags: String,
    val archived: Boolean,
    val createdAt: Long,
    val source: WordSource = WordSource.SYNCED
)

enum class WordSource {
    SYNCED,     // Da feed GitHub
    CUSTOM      // Aggiunta manualmente dall'utente
}

@Serializable
data class ReviewEntity(
    val wordId: String,
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val lapses: Int,
    val dueAt: Long,
    val lastReviewedAt: Long?
)

@Serializable
data class FeedLogEntity(
    val syncedAt: Long,
    val newWords: Int,
    val updatedWords: Int,
    val status: String,
    val message: String?
)

/** Sezione della guida di studio, sincronizzata dal Google Doc dell'utente. */
@Serializable
data class SezioneEntity(
    val titolo: String = "",
    val testo: String = ""
)

@Serializable
data class GuidaEntity(
    val titolo: String = "",
    val docUrl: String = "",
    val sezioni: List<SezioneEntity> = emptyList()
)