package com.alessandro.tedesco.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Url

interface FeedService {

    /**
     * Restituisce la Response completa invece del solo body: cosi' posso
     * leggere l'ETag, che il converter JSON consumerebbe senza mostrarlo.
     */
    @GET
    suspend fun getRaw(@Url url: String): Response<ResponseBody>

    /** HEAD leggero: serve solo a sapere se l'ETag e' cambiato. */
    @HEAD
    suspend fun head(@Url url: String): Response<Unit>
}
