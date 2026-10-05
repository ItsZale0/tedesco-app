package com.alessandro.tedesco.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: String,
    val german: String,
    val italian: String,
    val example: String,
    /** der / die / das, solo per i sostantivi */
    val article: String?,
    /** trascrizione in italiano, es. "tish" */
    val pronunciation: String?,
    val level: String,
    val lesson: Int,
    /** separata da virgola nel JSON */
    val tags: String,
    val archived: Boolean,
    val createdAt: Long
)

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId"), Index("dueAt")]
)
data class ReviewEntity(
    @PrimaryKey val wordId: String,
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val lapses: Int,
    val dueAt: Long,
    val lastReviewedAt: Long?
)

@Entity(tableName = "feed_log", indices = [Index("syncedAt")])
data class FeedLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncedAt: Long,
    val newWords: Int,
    val updatedWords: Int,
    val status: String,
    val message: String?
)
