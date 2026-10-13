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

    companion object {
        fun get(context: Context): TedescoApp = context.applicationContext as TedescoApp
    }
}
