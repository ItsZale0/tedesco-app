package com.alessandro.tedesco.di

import android.content.Context
import com.alessandro.tedesco.data.local.AppDatabase
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.settings.SettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.get(ctx)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true      // campi extra non devono rompere nulla
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideSettings(@ApplicationContext ctx: Context): SettingsStore =
        SettingsStore(ctx)

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Fornisce un FeedService che usa OkHttp puro per scaricare il JSON,
     * lasciando all'app (WordRepository) la responsabilità di parserlo con
     * kotlinx.serialization, come gia' fatto nel metodo merge().
     * Usa l'URL passato via @Url al metodo, non un baseUrl fisso.
     */
    @Provides
    @Singleton
    fun provideFeedService(okHttp: OkHttpClient): FeedService {
        return object : FeedService {
            override suspend fun getRaw(@Url url: String): okhttp3.Response {
                val request = okhttp3.Request(url)
                okHttp.newCall(request).execute()
            }

            override suspend fun head(@Url url: String): okhttp3.Response {
                val request = okhttp3.Request(url, okhttp3.Request.Method.HEAD)
                okHttp.newCall(request).execute()
            }
        }
    }
}