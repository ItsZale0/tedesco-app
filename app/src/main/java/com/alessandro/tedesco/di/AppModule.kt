package com.alessandro.tedesco.di

import android.content.Context
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.settings.SettingsStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppModule {

    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun provideSettings(context: Context): SettingsStore = SettingsStore(context)

    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun provideFeedService(okHttp: OkHttpClient): FeedService {
        return object : FeedService {
            override suspend fun getRaw(url: String): okhttp3.Response {
                val request = okhttp3.Request.Builder().url(url).build()
                return okHttp.newCall(request).execute()
            }

            override suspend fun head(url: String): okhttp3.Response {
                val request = okhttp3.Request.Builder().url(url).head().build()
                return okHttp.newCall(request).execute()
            }
        }
    }

    fun provideWordRepository(
        context: Context,
        service: FeedService,
        settings: SettingsStore,
        json: Json,
        io: CoroutineDispatcher
    ): WordRepository = WordRepository(context, service, settings, json, io)
}