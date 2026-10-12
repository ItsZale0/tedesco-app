package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity

/**
 * Calcola i 4 punteggi di competenza (0-100) per il livello B1.
 *
 * - Lessico: parole mature / 2000 * 100 (cap 100)
 * - Grammatica: basata su errori nelle review (accuratezza) e test di grammatica
 * - Comprensione: basata su quiz di comprensione (da implementare)
 * - Produzione: basata su frasi complete (da implementare)
 */
object CalcoloCompetenze {

    const val OBIETTIVO_PAROLE_B1 = 2000

    /** Minimo di test per dimensione perché il livello possa avanzare. */
    /** Minimo di test per dimensione perché il livello possa avanzare. */
    private fun minTest(livello: LivelloCEFR): Int = when (livello) {
        LivelloCEFR.A0, LivelloCEFR.A1 -> 3
        LivelloCEFR.A2 -> 4
        LivelloCEFR.B1 -> 6
        else -> 8
    }

    /** Requisiti per raggiungere un livello (criteri CEFR/Goethe, conservativi). */
    private data class Requisito(
        val paroleMature: Int,
        val grammatica: Float,
        val comprensione: Float,
        val produzione: Float
    )

    // Parole "mature" = ripassate con successo almeno 4 volte.
    // Soglie volutamente più alte del minimo CEFR per evitare promozioni facili.
    private val REQ_A1 = Requisito(30, 70f, 70f, 70f)
    private val REQ_A2 = Requisito(60, 78f, 78f, 78f)
    private val REQ_B1 = Requisito(110, 82f, 82f, 82f)
    private val REQ_B2 = Requisito(300, 85f, 85f, 85f)

    private fun mediaUltimi(punteggi: List<Float>): Float =
        if (punteggi.isEmpty()) 0f else punteggi.takeLast(5).average().toFloat()

    data class PunteggiCompetenze(
        val lessico: Float,      // 0-100
        val grammatica: Float,   // 0-100
        val comprensione: Float, // 0-100
        val produzione: Float,   // 0-100
        val livelloComplessivo: LivelloCEFR
    )

    /**
     * Calcola i 4 punteggi di competenza.
     *
     * @param parole tutte le parole (incluse archiviate)
     * @param reviews mappa wordId -> ReviewEntity
     * @param progresso progresso utente (per test di grammatica/comprensione/produzione)
     * @param now timestamp corrente
     */
    fun calcola(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        now: Long
    ): PunteggiCompetenze {
        val paroleAttive = parole.filter { !it.archived }

        // LESSICO: parole mature / 2000 * 100
        val paroleMature = paroleAttive.count { w ->
            val r = reviews[w.id]
            r != null && r.repetitions >= 4
        }
        val lessico = if (paroleAttive.isEmpty()) 0f
            else (paroleMature.toFloat() / paroleAttive.size * 100).coerceAtMost(100f)

        // GRAMMATICA: accuratezza delle review (SM-2). Nessun fallback gonfiato.
        val tutteReviews = paroleAttive.mapNotNull { reviews[it.id] }
        val grammaticaReview = if (tutteReviews.isEmpty()) 0f else {
            val ripassi = tutteReviews.sumOf { it.repetitions }
            val errori = tutteReviews.sumOf { it.lapses }
            if (ripassi + errori == 0) 0f
            else (ripassi.toFloat() / (ripassi + errori) * 100)
        }

        // COMPRENSIONE / PRODUZIONE / GRAMMATICA-TEST: SOLO da test reali.
        // Se mancano i test il punteggio è 0: niente fallback che gonfia il livello.
        val comprensione = mediaUltimi(progresso.testComprensione.map { it.punteggio })
        val produzione = mediaUltimi(progresso.testProduzione.map { it.punteggio })
        val grammatica = if (progresso.testGrammatica.isNotEmpty())
            mediaUltimi(progresso.testGrammatica.map { it.punteggio })
        else grammaticaReview

        // CRITERI CEFR (Goethe/telc/ÖSD): il livello è il massimo in cui TUTTE le
        // dimensioni raggiungono la soglia ("maglia più debole"). Servono almeno
        // MIN_TEST test per dimensione + un numero sufficiente di parole mature.
        val nGramm = progresso.testGrammatica.size
        val nComp = progresso.testComprensione.size
        val nProd = progresso.testProduzione.size

        fun raggiunge(r: Requisito, livello: LivelloCEFR): Boolean {
            val min = minTest(livello)
            return paroleMature >= r.paroleMature &&
                nGramm >= min && grammatica >= r.grammatica &&
                nComp >= min && comprensione >= r.comprensione &&
                nProd >= min && produzione >= r.produzione
        }

        val livelloComplessivo = when {
            raggiunge(REQ_B2, LivelloCEFR.B2) -> LivelloCEFR.B2
            raggiunge(REQ_B1, LivelloCEFR.B1) -> LivelloCEFR.B1
            raggiunge(REQ_A2, LivelloCEFR.A2) -> LivelloCEFR.A2
            raggiunge(REQ_A1, LivelloCEFR.A1) -> LivelloCEFR.A1
            else -> LivelloCEFR.A0
        }

        return PunteggiCompetenze(
            lessico = lessico,
            grammatica = grammatica,
            comprensione = comprensione,
            produzione = produzione,
            livelloComplessivo = livelloComplessivo
        )
    }
}
