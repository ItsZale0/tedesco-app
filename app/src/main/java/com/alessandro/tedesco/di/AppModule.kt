package com.alessandro.tedesco.di
import android.content.Context
import com.alessandro.tedesco.data.ProfileManager
import com.alessandro.tedesco.data.WordRepository
import com.alessandro.tedesco.data.remote.FeedService
import com.alessandro.tedesco.data.remote.TutorService
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
    fun provideProfileManager(
        context: Context,
        json: Json,
        io: CoroutineDispatcher
    ): ProfileManager = ProfileManager(context, json, io)
    fun provideWordRepository(
        context: Context,
        service: FeedService,
        json: Json,
        io: CoroutineDispatcher,
        profileManager: ProfileManager
    ): WordRepository = WordRepository(context, service, json, io, profileManager)
    fun provideTutorService(): TutorService = TutorService()
}