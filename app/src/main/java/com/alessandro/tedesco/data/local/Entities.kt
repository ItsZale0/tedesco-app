package com.alessandro.tedesco.data.local

import com.alessandro.tedesco.data.TestB1
import kotlinx.serialization.Serializable

@Serializable
data class WordEntity(
    val id: String,
    val german: String,
    val italian: String,
    val example: String = "",
    val article: String? = null,
    val pronunciation: String? = null,
    val level: String = "A1",
    val lesson: Int = 0,
    val tags: String = "",
    val archived: Boolean = false,
    val createdAt: Long = 0L,
    val source: WordSource = WordSource.SYNCED
)

enum class WordSource {
    SYNCED,     // Dal feed GitHub del corso
    CUSTOM      // Aggiunta manualmente dall'utente
}

@Serializable
data class ReviewEntity(
    val wordId: String,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val dueAt: Long = 0L,
    val lastReviewedAt: Long? = null
)

@Serializable
data class FeedLogEntity(
    val syncedAt: Long,
    val newWords: Int,
    val updatedWords: Int,
    val status: String,
    val message: String? = null
)

/** Sezione della guida di studio, sincronizzata dal Google Doc dell'utente. */
@Serializable
data class SezioneEntity(
    val titolo: String = "",
    val testo: String = ""
)

@Serializable
data class LessonEntity(
    val numero: Int,
    val titolo: String,
    val contenuto: String,
    val audioUrl: String = ""
)

@Serializable
data class GuidaEntity(
    val titolo: String = "",
    val docUrl: String = "",
    val sezioni: List<SezioneEntity> = emptyList()
)

/** Livelli CEFR supportati */
enum class LivelloCEFR(val ordine: Int, val label: String, val descrizione: String) {
    A0(0, "A0 — Principiante assoluto", "Nessuna conoscenza, si inizia da zero"),
    A1(1, "A1 — Elementare", "Frasi semplici, presente, articoli, ~500 parole"),
    A2(2, "A2 — Base", "Passato (Perfetto), dativo, separabili, modali, ~1000 parole"),
    B1(3, "B1 — Intermedio", "Congiuntivo, passivo, frasi complesse, vita quotidiana, ~2000 parole"),
    B2(4, "B2 — Avanzato", "Lavorativo, testi complessi, sfumature, ~4000 parole"),
    C1(5, "C1 — Autonomo", "Fluente, accademico/professionale, ~8000 parole"),
    C2(6, "C2 — Madrelingua", "Nativo, tutte le sfumature")
}

/** Risultato di un test di livello */
@Serializable
data class TestLivello(
    val id: String,
    val timestamp: Long,
    val livelloPrecedente: LivelloCEFR,
    val livelloNuovo: LivelloCEFR,
    val punteggioVocab: Int,
    val punteggioGrammatica: Int,
    val punteggioTotale: Int,
    val paroleTestate: Int,
    val corrette: Int,
    val durataSecondi: Long
)

/** Progresso complessivo dell'utente */
@Serializable
data class ProgressoUtente(
    val livelloCorrente: LivelloCEFR = LivelloCEFR.A0,
    val xpTotale: Int = 0,
    val paroleApprese: Int = 0,
    val paroleInCorso: Int = 0,
    val lezioniCompletate: Int = 0,
    val accuratezzaMedia: Float = 0f,
    val streakGiorni: Int = 0,
    val ultimoTest: TestLivello? = null,
    val storicoTest: List<TestLivello> = emptyList(),
    val ultimaAttivita: Long = 0,
    val obiettivoLivello: LivelloCEFR = LivelloCEFR.B1,
    val lezioneCorrente: Int = 1,
    val testGrammatica: List<TestGrammatica> = emptyList(),
    val testComprensione: List<TestComprensione> = emptyList(),
    val testProduzione: List<TestProduzione> = emptyList(),
    val testB1: List<com.alessandro.tedesco.data.TestB1> = emptyList(),
    val testAdattivi: List<TestAdattivo> = emptyList(),
    val testAscolto: List<TestAscolto> = emptyList(),
    val erroriGrammatica: List<String> = emptyList()
)

/** Risultato di un test di grammatica */
@Serializable
data class TestGrammatica(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int
)

/** Risultato di un test di comprensione */
@Serializable
data class TestComprensione(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int
)

/** Risultato di un test di produzione */
@Serializable
data class TestProduzione(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int
)

/** Risultato di un test adattivo (domande generate dinamicamente) */
@Serializable
data class TestAdattivo(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int,
    val livello: String
)

/** Risultato di un test di ascolto (Hörverstehen) */
@Serializable
data class TestAscolto(
    val data: Long,
    val punteggio: Float,  // 0-100
    val errori: Int,
    val totale: Int,
    val livello: String
)

/** Domanda per test di livello */
@Serializable
data class DomandaTest(
    val id: String,
    val tipo: TipoDomanda,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    val livelloRichiesto: LivelloCEFR,
    val tagGrammatica: String? = null
)

enum class TipoDomanda {
    VOCAB_TED_ITA,
    VOCAB_ITA_TED,
    GRAMMATICA,
    COMPLETAMENTO,
    TRADUZIONE
}

/* ==================== PROFILI UTENTE ==================== */

/** Tipologia profilo: definisce il preset di funzionalità */
enum class TipoProfilo(
    val id: String,
    val nome: String,
    val descrizione: String,
    val enableCustomWords: Boolean,
    val enableGoogleSheets: Boolean
) {
    ALESSANDRO(
        id = "alessandro",
        nome = "Alessandro",
        descrizione = "Percorso standard: lezioni, ripasso, guida e test di livello",
        enableCustomWords = false,
        enableGoogleSheets = false
    ),
    ALESSANDRO_CUSTOM(
        id = "alessandro_custom",
        nome = "Alessandro Personalizzato",
        descrizione = "Standard + parole personalizzate e sincronizzazione Google Sheets",
        enableCustomWords = true,
        enableGoogleSheets = true
    ),
    PERSONALIZZATO(
        id = "personalizzato",
        nome = "Personalizzato",
        descrizione = "Profilo personalizzato creato dall'utente",
        enableCustomWords = true,
        enableGoogleSheets = false
    ),
    EMMA(
        id = "emma",
        nome = "Emma",
        descrizione = "Percorso personalizzato: parole custom e Google Sheets",
        enableCustomWords = true,
        enableGoogleSheets = true
    )
}

/** Configurazione profilo */
@Serializable
data class ProfiloConfig(
    val tipo: TipoProfilo,
    val nomeVisualizzato: String,
    val enableCustomWords: Boolean = false,
    val enableGoogleSheets: Boolean = false,
    val googleSheetId: String? = null,
    val feedUrl: String = "",
    val guidaDocId: String? = null,
    val tutorApiKey: String = "",
    val palette: String = "uber",
    val stileDesign: String = "pixel"
)

/** Stato persistente per profilo — dati completamente separati per utente */
@Serializable
data class ProfiloStato(
    val parole: List<WordEntity> = emptyList(),
    val reviews: Map<String, ReviewEntity> = emptyMap(),
    val feedLog: List<FeedLogEntity> = emptyList(),
    val guida: GuidaEntity? = null,
    val progresso: ProgressoUtente = ProgressoUtente(),
    val lezioneContenuto: String = "",
    val etag: String = "",
    val lastSync: Long = 0L,
    val risposte: List<FeedbackEntry> = emptyList(),
    val piano: PianoEntity? = null,
    val progressoFeed: ProgressoFeedEntity? = null,
    val sessioni: List<SessioneEntity> = emptyList(),
    val streakGiorni: Int = 0,
    val streakRecord: Int = 0,
    val ultimoGiornoStudio: String? = null,
    val badgeSbloccati: List<String> = emptyList(),
    val puntiTotali: Int = 0

)

/** Piano di studio verso il B2. */
@Serializable
data class PianoEntity(
    val obiettivo: String = "",
    val orizzonte: String = "",
    val tappe: List<TappaEntity> = emptyList(),
    val certificazioni: List<CertificazioneEntity> = emptyList(),
    val risorse: List<RisorsaEntity> = emptyList()
)

@Serializable
data class TappaEntity(
    val nome: String = "",
    val descrizione: String = "",
    val lezioni: String = "",
    val stato: String = ""
)

@Serializable
data class CertificazioneEntity(
    val nome: String = "",
    val ente: String = "",
    val livello: String = "",
    val note: String = "",
    val url: String = ""
)

@Serializable
data class RisorsaEntity(
    val nome: String = "",
    val tipo: String = "",
    val nota: String = ""
)

/** Sessione strutturata disponibile nell'app. */
@Serializable
data class SessioneEntity(
    val tipo: String = "",
    val titolo: String = "",
    val descrizione: String = "",
    val durata: Int = 0
)

/** Risposta dell'utente da correggere. */
@Serializable
data class FeedbackEntry(
    val id: String,
    val testo: String,
    val timestamp: Long,
    val corretto: Boolean = false,
    val correzione: String? = null
)

/** Progresso corrente pubblicato dal tutor nel feed. */
@Serializable
data class ProgressoFeedEntity(
    val lezioneCorrente: Int = 1,
    val streakCorrente: Int = 0,
    val streakRecord: Int = 0,
    val totaleFatte: Int = 0,
    val paroleTotali: Int = 0,
    val paroleMature: Int = 0
)

/** Profilo utente completo con stato */
@Serializable
data class ProfiloUtente(
    val id: String,
    val config: ProfiloConfig,
    val stato: ProfiloStato = ProfiloStato(),
    val creatoIl: Long = System.currentTimeMillis(),
    val ultimoAccesso: Long = System.currentTimeMillis()
)

/** Repository per gestione profili */
@Serializable
data class ProfiliRepository(
    val profili: Map<String, ProfiloUtente> = emptyMap(),
    val profiloAttivoId: String? = null
)

// Stile design dell'app
enum class StileDesign {
    PIXEL,      // Google Pixel: colori vivaci, animazioni fluide
    IOS,        // iOS: glassmorphism, ombre profonde
    MATERIAL,   // Material You: tonalità dinamiche
    UBER        // Uber: dark minimal, alto contrasto
}
