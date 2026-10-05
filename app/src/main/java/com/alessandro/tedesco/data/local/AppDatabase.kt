package com.alessandro.tedesco.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class, ReviewEntity::class, FeedLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun reviewDao(): ReviewDao
    abstract fun feedLogDao(): FeedLogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tedesco.db"
                ).build().also { instance = it }
            }
    }
}
