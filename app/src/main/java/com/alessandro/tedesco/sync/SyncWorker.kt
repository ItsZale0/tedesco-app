package com.alessandro.tedesco.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alessandro.tedesco.data.SyncResult
import com.alessandro.tedesco.data.WordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Sincronizzazione periodica in background.
 * Usa WorkManager e non un timer: così Android decide quando svegliare
 * l'app rispettando la batteria. Non blocca mai l'avvio dell'app.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: WordRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = repo.sync()
        return when (result) {
            // niente modifiche: non e' un errore, non ritentare subito
            is SyncResult.NotModified -> Result.success()
            is SyncResult.Updated -> Result.success()
            is SyncResult.Failed -> {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "sync_vocabolario"

        /** default: ogni 15 minuti, il minimo che WorkManager accetta. */
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
