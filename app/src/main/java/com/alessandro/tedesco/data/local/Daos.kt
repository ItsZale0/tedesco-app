package com.alessandro.tedesco.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(words: List<WordEntity>)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: String): WordEntity?

    @Query("SELECT * FROM words WHERE archived = 0 ORDER BY lesson, german")
    fun observeAll(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE archived = 0 AND lesson = :lesson ORDER BY german")
    fun observeByLesson(lesson: Int): Flow<List<WordEntity>>

    @Query("SELECT DISTINCT lesson FROM words WHERE archived = 0 ORDER BY lesson")
    fun observeLessons(): Flow<List<Int>>

    @Query("UPDATE words SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("SELECT COUNT(*) FROM words WHERE archived = 0")
    suspend fun count(): Int
}

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE dueAt <= :now ORDER BY dueAt ASC")
    suspend fun dueNow(now: Long): List<ReviewEntity>

    @Query("SELECT COUNT(*) FROM reviews WHERE dueAt <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun count(): Int

    @Query("SELECT * FROM reviews")
    suspend fun all(): List<ReviewEntity>

    @Update
    suspend fun update(review: ReviewEntity)

    @Query("DELETE FROM reviews")
    suspend fun clear()
}

@Dao
interface FeedLogDao {

    @Insert
    suspend fun insert(entry: FeedLogEntity)

    @Query("SELECT * FROM feed_log ORDER BY syncedAt DESC LIMIT 1")
    fun observeLast(): Flow<FeedLogEntity?>

    @Query("SELECT * FROM feed_log ORDER BY syncedAt DESC LIMIT 30")
    suspend fun recent(): List<FeedLogEntity>
}
