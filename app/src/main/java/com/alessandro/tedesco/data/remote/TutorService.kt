package com.alessandro.tedesco.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
private data class ChatMessage(val role: String, val content: String)

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 400,
    val temperature: Double = 0.7
)

@Serializable
private data class ChatChoice(val message: ChatMessage? = null)

@Serializable
private data class ChatResponse(val choices: List<ChatChoice> = emptyList())

/**
 * Client per il tutor AI via OpenRouter.
 *
 * Usa un modello veloce ed economico (Llama 3.1 8B) per risposte
 * in tempo reale. La chiave API è configurata dall'utente nelle impostazioni.
 */
class TutorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Invia un messaggio al tutor e ottiene la risposta.
     *
     * @param apiKey chiave OpenRouter
     * @param cronologia conversazione precedente
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     */
    suspend fun rispondi(
        apiKey: String,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IOException("Chiave API non configurata. Vai in Profilo → Tutor AI per inserirla.")
        }

        val systemPrompt = """
            Sei un tutor di tedesco per uno studente italiano di livello $livello.
            Regole:
            - Rispondi SEMPRE in italiano, con esempi in tedesco.
            - Sii conciso: massimo 6-8 righe.
            - I sostantivi tedeschi vanno sempre con l'articolo (der/die/das).
            - Adatta la difficoltà al livello $livello: sii semplice e chiaro.
            - La lezione corrente è la numero $lezione.
            - Se lo studente sbaglia, correggi spiegando brevemente il perché.
            - Non usare markdown complesso: solo testo semplice con trattini per gli elenchi.
        """.trimIndent()

        val messaggi = buildList {
            add(ChatMessage("system", systemPrompt))
            cronologia.takeLast(8).forEach { (ruolo, testo) ->
                add(ChatMessage(if (ruolo == "utente") "user" else "assistant", testo))
            }
        }

        var ultimoErrore: String? = null
        for (modello in MODELLI_FREE) {
            try {
                return@withContext chiamaModello(apiKey, modello, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                // 401 = chiave non valida: inutile provare altri modelli
                if (e.message?.contains("non valida") == true) throw e
                // altrimenti prova il prossimo modello free
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    private fun chiamaModello(
        apiKey: String,
        modello: String,
        messaggi: List<ChatMessage>
    ): String {
        val body = json.encodeToString(
            ChatRequest.serializer(),
            ChatRequest(model = modello, messages = messaggi)
        )

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("HTTP-Referer", "https://github.com/ItsZale0/tedesco-app")
            .header("X-Title", "Tedesco App")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val testo = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errore = runCatching {
                    json.parseToJsonElement(testo).toString()
                        .substringAfter("\"message\":\"", "")
                        .substringBefore("\"")
                }.getOrNull()
                throw IOException(
                    when {
                        response.code == 401 -> "Chiave API non valida."
                        response.code == 429 -> "Troppe richieste. Riprova tra poco."
                        errore.isNullOrBlank() -> "Errore del server (${response.code})"
                        else -> errore
                    }
                )
            }
            val parsed = json.decodeFromString(ChatResponse.serializer(), testo)
            return parsed.choices.firstOrNull()?.message?.content
                ?: throw IOException("Risposta vuota dal tutor")
        }
    }

    companion object {
        /**
         * Modelli free, in ordine di preferenza.
         * Se uno è in rate limit si passa al successivo: così il tutor
         * resta utilizzabile senza spendere credito.
         */
        val MODELLI_FREE = listOf(
            "nvidia/nemotron-3.5-lightning:free",
            "liquid/lfm-2.5-2.6b:free",
            "inclusionai/ling-3.0-flash-sante:free",
            "google/gemma-4-31b-it:free",
            "google/gemma-4-26b-a4b-it:free",
            "nvidia/nemotron-3-super-120b-a12b:free"
        )
        const val MODELLO = "nvidia/nemotron-3.5-lightning:free"
    }
}
