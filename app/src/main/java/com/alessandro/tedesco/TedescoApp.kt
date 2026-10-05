package com.alessandro.tedesco

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.di.AppModule
import com.alessandro.tedesco.settings.SettingsStore
import com.alessandro.tedesco.sync.SyncWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class TedescoApp : Application(), Configuration.Provider {

    // Manual DI - singletons
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val json: Json = AppModule.provideJson()
    private val okHttp: OkHttpClient = AppModule.provideOkHttp()
    private val feedService: FeedService = AppModule.provideFeedService(okHttp)
    private val settings: SettingsStore by lazy { AppModule.provideSettings(this) }
    private val wordRepository: WordRepository by lazy {
        AppModule.provideWordRepository(this, feedService, settings, json, ioDispatcher)
    }

    // Expose repository for SyncWorker
    val wordRepositoryInstance: WordRepository get() = wordRepository
    val settingsInstance: SettingsStore get() = settings

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        SyncWorker.schedule(this)
    }

    companion object {
        fun get(context: Context): TedescoApp = context.applicationContext as TedescoApp
    }
}