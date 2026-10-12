package com.alessandro.tedesco.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

/**
 * Risposta "grezza" di un singolo esercizio generato dall'AI.
 * I campi sono tutti opzionali perché il modello può sbagliare il JSON:
 * il parser scarta gli elementi incompleti invece di far crashare.
 */
@Serializable
private data class EsercizioAi(
    val id: String? = null,
    val tipo: String? = null,
    val domanda: String? = null,
    val opzioni: List<String>? = null,
    @SerialName("rispostaCorretta") val rispostaCorretta: Int? = null,
    val spiegazione: String? = null,
    val livello: String? = null,
    val fraseTedesca: String? = null,
    val traduzioneItaliana: String? = null
)

@Serializable
private data class RispostaEserciziAi(
    val esercizi: List<EsercizioAi> = emptyList()
)

/** Tipo di esercizio richiesto al generatore. */
enum class TipoEsercizio(val etichetta: String, val jsonTipo: String) {
    COMPRENSIONE("Comprensione", "comprensione"),
    PRODUZIONE("Produzione", "produzione"),
    ASCOLTO("Ascolto", "ascolto"),
    GRAMMATICA("Grammatica", "grammatica")
}

/** Un esercizio generato dall'AI, già normalizzato e validato. */
data class EsercizioGenerato(
    val id: String,
    val tipo: TipoEsercizio,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    val livello: String,
    /** Solo per ASCOLTO: la frase da far ascoltare via TTS. */
    val fraseTedesca: String? = null,
    val traduzioneItaliana: String? = null
)

/**
 * Generatore di esercizi infiniti via Groq (solo modelli free).
 *
 * Gli esercizi statici delle schermate (QuizData, AscoltoData, TestB1) sono un
 * pool iniziale: quando lo studente li esaurisce, l'app ne genera di nuovi con
 * l'AI. La generazione avviene SOLO su richiesta esplicita dello schermo
 * (bottone "Genera altri") e mai in background, per non sprecare rate limit.
 *
 * Usa la stessa cascata di modelli free di TutorService: se uno è in rate
 * limit si passa al successivo.
 */
class GeneratoreEsercizi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Genera [quanti] esercizi nuovi del tipo richiesto.
     *
     * @param apiKey chiave Groq dell'utente
     * @param tipo quale sezione deve generare
     * @param livello livello CEFR (A0-B2)
     * @param quanti quanti esercizi (consigliato 5-10)
     * @param contestoMedico true = usa vocabolario medico/ospedaliero
     * @param evita lista di testi già mostrati, per non ripetere le domande
     * @param modello modello opzionale (null = cascata dei free)
     * @return lista di esercizi validati (può essere più corta di [quanti]
     *         se il modello ne produce di scadenti: quelli invalidi sono scartati)
     */
    suspend fun genera(
        apiKey: String,
        tipo: TipoEsercizio,
        livello: String,
        quanti: Int = 5,
        contestoMedico: Boolean = false,
        evita: List<String> = emptyList(),
        modello: String? = null
    ): List<EsercizioGenerato> = withContext(Dispatchers.IO) {
        // Free mode (no API key): uses Gemini endpoint automatically
        if (quanti <= 0) return@withContext emptyList()

        val systemPrompt = costruisciPrompt(tipo, livello, quanti, contestoMedico, evita)
        val messaggi = listOf(
            ChatMessageCompat("system", systemPrompt),
            ChatMessageCompat("user", "Genera gli esercizi in JSON.")
        )

        val modelli = if (!modello.isNullOrBlank()) listOf(modello)
            else if (apiKey.isBlank()) TutorService.MODELLI_NO_KEY else TutorService.MODELLI_FREE

        var ultimoErrore: String? = null
        var rateLimited = false
        // 2 tentativi: se tutti i modelli danno rate limit (429), aspetta e riprova
        for (tentativo in 1..2) {
            for (m in modelli) {
                try {
                    val risposta = chiamaModello(apiKey, m, messaggi)
                    val esercizi = parseEsercizi(risposta, tipo, livello)
                    if (esercizi.isNotEmpty()) return@withContext esercizi
                    ultimoErrore = "Risposta non valida dal modello $m"
                } catch (e: IOException) {
                    ultimoErrore = e.message
                    // 401 = chiave non valida: inutile provare altri modelli
                    if (e.message?.contains("non valida") == true) throw e
                    if (e.message?.contains("Troppe richieste") == true) rateLimited = true
                }
            }
            if (!rateLimited) break
            if (tentativo < 2) delay(4000)
        }
        throw IOException(ultimoErrore ?: "Nessun modello disponibile al momento")
    }

    private fun costruisciPrompt(
        tipo: TipoEsercizio,
        livello: String,
        quanti: Int,
        contestoMedico: Boolean,
        evita: List<String>
    ): String {
        val contesto = if (contestoMedico) {
            "\n- Contesto professionale: lo studente è un fisioterapista che lavorerà a Bolzano. " +
                "Dove ha senso, usa vocabolario medico/ospedaliero (Krankenhaus, Schmerzen, Behandlung, Patient…)."
        } else ""

        val istruzioniTipo = when (tipo) {
            TipoEsercizio.ASCOLTO -> """
                Ogni esercizio è una frase tedesca da ascoltare + una domanda di comprensione a 4 opzioni.
                Compila anche i campi "fraseTedesca" (la frase da ascoltare) e "traduzioneItaliana".
            """.trimIndent()
            TipoEsercizio.COMPRENSIONE -> """
                Ogni esercizio è una domanda di comprensione della lettura (Lesen) a 4 opzioni:
                traduzioni, significato di parole, comprensione di brevi frasi.
            """.trimIndent()
            TipoEsercizio.PRODUZIONE -> """
                Ogni esercizio è una domanda di produzione/grammatica (Schreiben) a 4 opzioni:
                completare una frase, scegliere l'articolo, coniugare un verbo, ordinare parole.
            """.trimIndent()
            TipoEsercizio.GRAMMATICA -> """
                Ogni esercizio è un punto di grammatica a 4 opzioni (articoli, casi, verbi, plurali).
            """.trimIndent()
        }

        val evitaTesto = if (evita.isNotEmpty()) {
            "\n\nNON ripetere domande simili a queste già viste:\n" + evita.take(20).joinToString("\n") { "- $it" }
        } else ""

        return """
            Sei un generatore di esercizi di tedesco per uno studente italiano di livello $livello.

            Genera ESATTAMENTE $quanti esercizi di tipo "${tipo.etichetta}" in formato JSON.
            $istruzioniTipo

            Regole OBBLIGATORIE:
            - Rispondi SOLO con un JSON valido, senza testo prima o dopo, senza ```json.
            - Struttura: {"esercizi": [ ... ]}
            - Ogni esercizio ha: "tipo" ("${tipo.jsonTipo}"), "domanda", "opzioni" (array di esattamente 4 stringhe),
              "rispostaCorretta" (indice 0-3 della risposta giusta), "spiegazione" (in italiano, breve),
              "livello" ("$livello").$contesto
            - LINGUA (REGOLA FONDAMENTALE): domande, opzioni e spiegazioni sono ESCLUSIVAMENTE in italiano.
            - NON usare MAI spagnolo (vietate parole come amigo, hola, gracias, senor, por favor, muy, bueno, manana), ne inglese, ne altre lingue.
            - L'unico tedesco ammesso e la frase/esempio tedesco quando il tipo lo richiede.
            - Esempio corretto di opzioni in italiano: ["Io leggo un libro", "Tu leggi un libro", "Lui legge un libro", "Noi leggiamo un libro"]
            - Esempio SBAGLIATO da NON usare (spagnolo): ["Ich a un amigo", "Tu a un amigo", "El a mi"]
            - Le 4 opzioni devono essere plausibili ma con UNA sola risposta corretta.
            - DIFFICOLTÀ: genera esercizi SFIDANTI e UTILI per il livello $livello. Evita domande banali su parole singole elementari (es. il significato di "Hallo", "ciao", "grazie"). Privilegia frasi complete, vocabulario pratico e contesto realistico.
            - VARIETÀ: alterna schemi diversi (completare una frase, scegliere l'articolo/caso, coniugare un verbo, tradurre una frase utile, comprensione di una breve frase). Non proporre sempre lo stesso tipo.
            - I sostantivi tedeschi vanno sempre con l'articolo (der/die/das).
            - Non inventare esercizi con meno di 4 opzioni o senza rispostaCorretta.$evitaTesto
        """.trimIndent()
    }

    private fun chiamaModello(
        apiKey: String,
        modello: String,
        messaggi: List<ChatMessageCompat>
    ): String {
        val isFreeMode = apiKey.isBlank()
        val url = if (isFreeMode) TutorService.FREE_ENDPOINT else "https://api.groq.com/openai/v1/chat/completions"
        val effectiveKey = apiKey.trim().ifBlank { TutorService.FREE_API_KEY }

        val body = json.encodeToString(
            ChatRequestCompat.serializer(),
            ChatRequestCompat(model = modello, messages = messaggi, reasoningEffort = if (modello.contains("oss", ignoreCase = true)) "low" else null)
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
                throw IOException(
                    when (response.code) {
                        401 -> "Chiave API non valida."
                        429, 403 -> "Troppe richieste. Riprova tra poco."
                        else -> "Errore del server (${response.code})"
                    }
                )
            }
            val parsed = json.decodeFromString(ChatResponseCompat.serializer(), testo)
            return parsed.choices.firstOrNull()?.messageCompat?.content
                ?: throw IOException("Risposta vuota dal generatore")
        }
    }

    /**
     * Estrae il JSON dalla risposta (il modello a volte aggiunge ```json o testo),
     * poi converte in [EsercizioGenerato] scartando gli elementi invalidi.
     */
    // Solo parole inequivocabilmente spagnole, a parole intere (\b) per non matchare sottostringhe italiane
    // (es. "questa" NON deve matchare "esta"). Vetate solo parole che in italiano non esistono.
    private val regexSpagnolo = Regex(
        "\\b(amigo|amiga|hola|senor|senora|gracias|manana|perro|gato|muy|bueno|buena|por favor|de nada|que tal|como estas)\\b",
        RegexOption.IGNORE_CASE
    )

    /** true se il testo contiene parole spagnole (euristica anti-lingua-sbagliata). */
    private fun contieneSpagnolo(testo: String): Boolean = regexSpagnolo.containsMatchIn(testo)

    // Alfabeti non latini: arabo, cirillico, CJK, ecc. Le domande/opzioni devono essere in italiano (latino).
    private val regexNonLatino = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u0400-\\u04FF\\u4E00-\\u9FFF\\u3040-\\u30FF]")

    /** true se il testo contiene caratteri di alfabeti non latini (arabo, cirillico, CJK...). */
    private fun contieneNonLatino(testo: String): Boolean = regexNonLatino.containsMatchIn(testo)

    private fun parseEsercizi(
        risposta: String,
        tipo: TipoEsercizio,
        livelloDefault: String
    ): List<EsercizioGenerato> {
        val jsonPulito = estraiJson(risposta) ?: return emptyList()
        val parsed = runCatching {
            json.decodeFromString(RispostaEserciziAi.serializer(), jsonPulito)
        }.getOrNull() ?: return emptyList()

        return parsed.esercizi.mapIndexedNotNull { index, e ->
            val domanda = e.domanda?.trim().orEmpty()
            val opzioni = e.opzioni?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
            val rispostaCorretta = e.rispostaCorretta ?: return@mapIndexedNotNull null
            // Validazione: scarta esercizi incompleti o con risposta fuori range
            if (domanda.isEmpty() || opzioni.size != 4) return@mapIndexedNotNull null
            if (rispostaCorretta !in 0..3) return@mapIndexedNotNull null
            if (opzioni.toSet().size != opzioni.size) return@mapIndexedNotNull null // opzioni duplicate
            // Scarta esercizi in lingua sbagliata (spagnolo/inglese) nelle domande o opzioni
            if (contieneSpagnolo(domanda) || opzioni.any { contieneSpagnolo(it) }) return@mapIndexedNotNull null
            if (contieneNonLatino(domanda) || opzioni.any { contieneNonLatino(it) }) return@mapIndexedNotNull null
            // Scarta se la spiegazione è in tedesco invece che in italiano (modello che ignora la lingua)
            if (!e.spiegazione.isNullOrBlank() && contieneNonLatino(e.spiegazione)) return@mapIndexedNotNull null
            // ASCOLTO richiede la frase da ascoltare e la traduzione
            if (tipo == TipoEsercizio.ASCOLTO && (e.fraseTedesca.isNullOrBlank() || e.traduzioneItaliana.isNullOrBlank())) return@mapIndexedNotNull null

            EsercizioGenerato(
                id = "${tipo.jsonTipo}_gen_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}",
                tipo = tipo,
                domanda = domanda,
                opzioni = opzioni,
                rispostaCorretta = rispostaCorretta,
                spiegazione = e.spiegazione?.trim().orEmpty().ifEmpty { "Nessuna spiegazione disponibile." },
                livello = e.livello?.trim()?.takeIf { it.isNotEmpty() } ?: livelloDefault,
                fraseTedesca = e.fraseTedesca?.trim()?.takeIf { it.isNotEmpty() },
                traduzioneItaliana = e.traduzioneItaliana?.trim()?.takeIf { it.isNotEmpty() }
            )
        }
    }

    /** Trova il primo blocco JSON bilanciato nella risposta. */
    private fun estraiJson(testo: String): String? {
        val start = testo.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escape = false
        for (i in start until testo.length) {
            val c = testo[i]
            when {
                escape -> escape = false
                c == '\\' && inString -> escape = true
                c == '"' -> inString = !inString
                !inString && c == '{' -> depth++
                !inString && c == '}' -> {
                    depth--
                    if (depth == 0) return testo.substring(start, i + 1)
                }
            }
        }
        return null
    }
}

// ── Modelli di compatibilità per il generatore (evitano di toccare TutorService) ──

@Serializable
private data class ChatMessageCompat(val role: String, val content: String)

@Serializable
private data class ChatRequestCompat(
    val model: String,
    val messages: List<ChatMessageCompat>,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val temperature: Double = 0.25,
    @SerialName("reasoning_effort") val reasoningEffort: String? = null
)

@Serializable
private data class ChatMessageRespCompat(val content: String)

@Serializable
private data class ChatChoiceCompat(@SerialName("message") val messageCompat: ChatMessageRespCompat? = null)

@Serializable
private data class ChatResponseCompat(val choices: List<ChatChoiceCompat> = emptyList())
