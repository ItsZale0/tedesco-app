package com.alessandro.tedesco.data.remote

import com.alessandro.tedesco.BuildConfig
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
    @SerialName("max_tokens") val maxTokens: Int = 2048,
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
        lezione: Int,
        modello: String? = null
    ): String = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically

        val systemPrompt = """
            Sei un tutor di tedesco per uno studente italiano di livello $livello.
            Regole:
            - Rispondi SEMPRE in italiano, con esempi in tedesco.
            - Rispondi in modo completo e dettagliato.
            - I sostantivi tedeschi vanno sempre con l'articolo (der/die/das).
            - Adatta la difficoltà al livello $livello: sii semplice e chiaro.
            - La lezione corrente è la numero $lezione.
            - Se lo studente sbaglia, correggi spiegando brevemente il perché.
            - Usa markdown semplice: **grassetto** per i termini chiave, - per gli elenchi, ## per i titoli.
            - Non usare tabelle né HTML.
        """.trimIndent()

        val messaggi = buildList {
            add(ChatMessage("system", systemPrompt))
            cronologia.takeLast(8).forEach { (ruolo, testo) ->
                add(ChatMessage(if (ruolo == "utente") "user" else "assistant", testo))
            }
        }

        // Se l'utente ha scelto un modello, usa quello; altrimenti prova i free in cascata
        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        for (m in modelli) {
            try {
                return@withContext chiamaModello(apiKey, m, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                // 401 = chiave non valida: inutile provare altri modelli
                if (e.message?.contains("non valida") == true) throw e
                // altrimenti prova il prossimo modello
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    /**
     * Corregge una risposta in tedesco di uno studente.
     *
     * @param apiKey chiave OpenRouter
     * @param testoStudente la risposta da correggere
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     * @param modello modello opzionale (null = automatico)
     * @return la correzione in italiano, una riga per errore
     */
    suspend fun correggiRisposta(
        apiKey: String,
        testoStudente: String,
        livello: String,
        lezione: Int,
        modello: String? = null
    ): String = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically

        val systemPrompt = """
            Sei un correttore di tedesco per uno studente italiano di livello $livello.
            Regole:
            - Analizza la risposta dello studente e individua TUTTI gli errori (grammatica, vocabolario, sintassi, articoli, preposizioni, coniugazioni).
            - Per ogni errore, scrivi una riga nel formato: "- **Errore**: [descrizione] → **Correzione**: [forma corretta]"
            - Se non ci sono errori, rispondi: "Corretto! Ottimo lavoro."
            - Spiega brevemente il perché di ogni correzione.
            - La lezione corrente è la numero $lezione.
            - Usa markdown semplice: **grassetto** per i termini chiave, - per gli elenchi.
            - Non usare tabelle né HTML.
        """.trimIndent()

        val messaggi = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", testoStudente)
        )

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        for (m in modelli) {
            try {
                return@withContext chiamaModello(apiKey, m, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                if (e.message?.contains("non valida") == true) throw e
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    /**
     * Scenario di gioco di ruolo preimpostato.
     */
    @Serializable
    data class RoleplayScenario(
        val id: String,
        val titolo: String,
        val descrizione: String,
        val ruoloAI: String,
        val ruoloUtente: String,
        val situazione: String
    )

    /**
     * Avvia un gioco di ruolo con uno scenario preimpostato.
     *
     * @param apiKey chiave OpenRouter
     * @param scenario lo scenario di gioco di ruolo
     * @param cronologia conversazione precedente
     * @param livello livello CEFR dell'utente (A0-B2)
     * @param lezione contesto della lezione corrente
     * @param modello modello opzionale (null = automatico)
     */
    suspend fun roleplay(
        apiKey: String,
        scenario: RoleplayScenario,
        cronologia: List<Pair<String, String>>,
        livello: String,
        lezione: Int,
        modello: String? = null
    ): String = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically

        val systemPrompt = """
            Sei un ${scenario.ruoloAI} in un gioco di ruolo per uno studente italiano di tedesco di livello $livello.

            SCENARIO: ${scenario.titolo}
            SITUAZIONE: ${scenario.situazione}
            TUO RUOLO: ${scenario.ruoloAI}
            RUOLO DELLO STUDENTE: ${scenario.ruoloUtente}

            Regole:
            - Interagisci SEMPRE in tedesco (con traduzione in italiano tra parentesi se necessario).
            - Resti nel personaggio per tutta la conversazione.
            - Adatta la difficoltà al livello $livello: usa frasi semplici e vocabolario di base.
            - Se lo studente sbaglia, correggi gentilmente e continua la conversazione.
            - La lezione corrente è la numero $lezione.
            - Mantieni la conversazione naturale e realistica.
            - Se lo studente usa l'italiano, rispondi in tedesco incoraggiandolo a continuare.
            - Usa markdown semplice: **grassetto** per i termini chiave.
            - Non usare tabelle né HTML.
        """.trimIndent()

        val messaggi = buildList {
            add(ChatMessage("system", systemPrompt))
            cronologia.takeLast(8).forEach { (ruolo, testo) ->
                add(ChatMessage(if (ruolo == "utente") "user" else "assistant", testo))
            }
        }

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) MODELLI_NO_KEY else MODELLI_FREE

        var ultimoErrore: String? = null
        for (m in modelli) {
            try {
                return@withContext chiamaModello(apiKey, m, messaggi)
            } catch (e: IOException) {
                ultimoErrore = e.message
                if (e.message?.contains("non valida") == true) throw e
            }
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    private fun chiamaModello(
        apiKey: String,
        modello: String,
        messaggi: List<ChatMessage>
    ): String {
        val isFreeMode = apiKey.isBlank()
        val url = if (isFreeMode) FREE_ENDPOINT else "https://openrouter.ai/api/v1/chat/completions"
        val effectiveKey = if (isFreeMode) FREE_API_KEY else apiKey

        val body = json.encodeToString(
            ChatRequest.serializer(),
            ChatRequest(model = modello, messages = messaggi)
        )

        val builder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $effectiveKey")
            .header("Content-Type", "application/json")

        if (!isFreeMode) {
            builder.header("HTTP-Referer", "https://github.com/ItsZale0/tedesco-app")
            builder.header("X-Title", "Tedesco App")
        }

        val request = builder.post(body.toRequestBody("application/json".toMediaType())).build()

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
         * Scenari di gioco di ruolo preimpostati.
         */
        val SCENARI = listOf(
            RoleplayScenario(
                id = "ristorante",
                titolo = "Al ristorante",
                descrizione = "Ordina un pasto in un ristorante tedesco",
                ruoloAI = "Cameriere",
                ruoloUtente = "Cliente",
                situazione = "Sei in un ristorante a Berlino. Il cameriere ti accoglie e ti porta al tavolo."
            ),
            RoleplayScenario(
                id = "stazione",
                titolo = "Alla stazione",
                descrizione = "Chiedi informazioni e biglietti alla stazione",
                ruoloAI = "Impiegato della stazione",
                ruoloUtente = "Viaggiatore",
                situazione = "Sei alla stazione centrale di Monaco e devi comprare un biglietto per Vienna."
            ),
            RoleplayScenario(
                id = "negozio",
                titolo = "Negozio",
                descrizione = "Fai shopping e chiedi informazioni sui prodotti",
                ruoloAI = "Commesso",
                ruoloUtente = "Cliente",
                situazione = "Sei in un negozio di abititi a Amburgo e cerchi un vestito per una festa."
            ),
            RoleplayScenario(
                id = "medico",
                titolo = "Al medico",
                descrizione = "Descrivi i tuoi sintomi al medico",
                ruoloAI = "Medico",
                ruoloUtente = "Paziente",
                situazione = "Sei dal medico perché hai mal di testa e febbre da due giorni."
            ),
            RoleplayScenario(
                id = "albergo",
                titolo = "In albergo",
                descrizione = "Fai il check-in e chiedi informazioni",
                ruoloAI = "Receptionist",
                ruoloUtente = "Ospite",
                situazione = "Arrivi in un albergo a Zurigo e fai il check-in. Hai una prenotazione."
            )
        )

        // Endpoint gratuito senza chiave API (Google Gemini free tier)
        const val FREE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
        val FREE_API_KEY = BuildConfig.FREE_API_KEY

        /**
         * Modelli gratuiti senza chiave API (Gemini), in ordine di preferenza.
         */
        val MODELLI_NO_KEY = listOf(
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.0-flash"
        )

        /**
         * Modelli OpenRouter, in ordine di preferenza.
         * Usati quando l'utente inserisce la propria chiave API.
         * Se uno è in rate limit si passa al successivo.
         */
        val MODELLI_FREE = listOf(
            "poolside/laguna-s-2.1:free",
            "poolside/laguna-xs-2.1:free",
            "thinkingmachines/inkling:free",
            "thinkingmachines/inkling-small:free",
            "stepfun/step-5-preview:free",
            "nvidia/nemotron-3.5-lightning:free",
            "google/gemma-4-31b-it:free",
            "google/gemma-4-26b-a4b-it:free"
        )
        const val MODELLO = "gemini-2.5-flash"
    }
}
