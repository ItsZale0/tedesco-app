package com.alessandro.tedesco.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alessandro.tedesco.data.SyncResult
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.TedescoApp
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repo: WordRepository by lazy {
        val app = applicationContext as TedescoApp
        app.wordRepositoryInstance
    }

    override suspend fun doWork(): Result {
        val result = repo.sync()
        return when (result) {
            is SyncResult.NotModified -> Result.success()
            is SyncResult.Updated -> Result.success()
            is SyncResult.Failed -> {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "sync_vocabolario"

        fun schedule(context: Context, minutes: Int = 15) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                minutes.coerceAtLeast(15).toLong(), TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}