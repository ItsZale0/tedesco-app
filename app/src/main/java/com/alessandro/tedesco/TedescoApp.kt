package com.alessandro.tedesco

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.alessandro.tedesco.data.ProfileManager
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.data.remote.GeneratoreEsercizi
import com.alessandro.tedesco.data.remote.TutorService
import com.alessandro.tedesco.data.remote.VocabPublishService
import com.alessandro.tedesco.di.AppModule
import com.alessandro.tedesco.sync.ReminderWorker
import com.alessandro.tedesco.data.BackupSettings
import kotlinx.coroutines.launch
import com.alessandro.tedesco.sync.BackupWorker
import com.alessandro.tedesco.sync.SyncWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class TedescoApp : Application(), Configuration.Provider {

    /** Handler originale: il crash resta visibile all'utente ("l'app si e' interrotta"). */
    private val handlerOriginale: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    /** Scope dell'app: per lavoro che deve sopravvivere all'Activity ma non e' un worker. */
    private val appScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )

    // Manual DI - singletons
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val json: Json = AppModule.provideJson()
    private val okHttp: OkHttpClient = AppModule.provideOkHttp()
    private val feedService: FeedService = AppModule.provideFeedService(okHttp)
    private val tutorService: TutorService = AppModule.provideTutorService()
    private val vocabPublishService: VocabPublishService = AppModule.provideVocabPublishService()
    private val generatoreEsercizi: GeneratoreEsercizi by lazy { AppModule.provideGeneratoreEsercizi() }

    // Gestione profili utente
    private val profileManager: ProfileManager by lazy {
        AppModule.provideProfileManager(this, json, ioDispatcher)
    }

    private val wordRepository: WordRepository by lazy {
        AppModule.provideWordRepository(this, feedService, json, ioDispatcher, profileManager)
    }

    val wordRepositoryInstance: WordRepository get() = wordRepository
    val profileManagerInstance: ProfileManager get() = profileManager
    val tutorServiceInstance: TutorService get() = tutorService
    val vocabPublishServiceInstance: VocabPublishService get() = vocabPublishService
    val generatoreEserciziInstance: GeneratoreEsercizi get() = generatoreEsercizi

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        installaCrashReporter()
        SyncWorker.schedule(this)
        ReminderWorker.schedule(this)
        // Riprogramma il backup automatico se l'utente lo aveva attivato
        // (WorkManager perde il job se l'app viene reinstallata/aggiornata).
        appScope.launch {
            if (BackupSettings(this@TedescoApp).autoAbilitato()) {
                BackupWorker.schedule(this@TedescoApp)
            }
        }
    }

    /**
     * Salva lo stack trace dell'ultimo crash in filesDir/ultimo_crash.txt, cosi'
     * Impostazioni lo mostra: un crash senza log e' un bug irripetibile.
     * Il handler originale viene chiamato dopo, cosi' l'app si chiude come sempre.
     */
    private fun installaCrashReporter() {
        Thread.setDefaultUncaughtExceptionHandler { thread, errore ->
            runCatching {
                val trace = buildString {
                    appendLine("Data: ${java.util.Date()}")
                    appendLine("Thread: ${thread.name}")
                    append(errore.stackTraceToString())
                    var causa: Throwable? = errore.cause
                    while (causa != null) {
                        appendLine("Causa: $causa")
                        append(causa.stackTraceToString())
                        causa = causa.cause
                    }
                }
                java.io.File(filesDir, "ultimo_crash.txt").writeText(trace)
                android.util.Log.e("CrashReporter", "Crash salvato in filesDir/ultimo_crash.txt", errore)
            }
            handlerOriginale?.uncaughtException(thread, errore)
        }
    }

    companion object {
        fun get(context: Context): TedescoApp = context.applicationContext as TedescoApp

        /** Ultimo crash salvato (null se l'app non e' mai crashata). */
        fun ultimoCrash(context: Context): String? = runCatching {
            val f = java.io.File(context.filesDir, "ultimo_crash.txt")
            if (f.exists()) f.readText() else null
        }.getOrNull()

        fun cancellaUltimoCrash(context: Context) {
            runCatching { java.io.File(context.filesDir, "ultimo_crash.txt").delete() }
        }
    }
}
