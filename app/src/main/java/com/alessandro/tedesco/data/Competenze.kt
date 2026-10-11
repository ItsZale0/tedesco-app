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

        // GRAMMATICA: accuratezza media delle review
        // Se non ci sono review, usa il progresso.accuratezzaMedia
        val tutteReviews = paroleAttive.mapNotNull { reviews[it.id] }
        val grammatica = if (tutteReviews.isEmpty()) {
            progresso.accuratezzaMedia * 100
        } else {
            val ripassi = tutteReviews.sumOf { it.repetitions }
            val errori = tutteReviews.sumOf { it.lapses }
            if (ripassi + errori == 0) 0f
            else (ripassi.toFloat() / (ripassi + errori) * 100)
        }

        // COMPRENSIONE: media dei punteggi dei quiz di comprensione (ultimi 5)
        val comprensione = if (progresso.testComprensione.isNotEmpty()) {
            progresso.testComprensione.takeLast(5).map { it.punteggio }.average().toFloat()
        } else {
            progresso.accuratezzaMedia * 100
        }

        // PRODUZIONE: media dei punteggi degli esercizi di produzione (ultimi 5)
        val produzione = if (progresso.testProduzione.isNotEmpty()) {
            progresso.testProduzione.takeLast(5).map { it.punteggio }.average().toFloat()
        } else {
            progresso.accuratezzaMedia * 100
        }

        // LIVELLO COMPLESSIVO: media ponderata
        // Lessico 30%, Grammatica 30%, Comprensione 20%, Produzione 20%
        val punteggioComplessivo = lessico * 0.3f + grammatica * 0.3f + comprensione * 0.2f + produzione * 0.2f

        val livelloComplessivo = when {
            punteggioComplessivo < 20 -> LivelloCEFR.A0
            punteggioComplessivo < 40 -> LivelloCEFR.A1
            punteggioComplessivo < 60 -> LivelloCEFR.A2
            punteggioComplessivo < 75 -> LivelloCEFR.B1
            punteggioComplessivo < 90 -> LivelloCEFR.B2
            else -> LivelloCEFR.C1
        }

        // Non far mai scendere sotto il livello corrente
        val livelloFinale = if (progresso.livelloCorrente.ordine > livelloComplessivo.ordine) {
            progresso.livelloCorrente
        } else {
            livelloComplessivo
        }

        return PunteggiCompetenze(
            lessico = lessico,
            grammatica = grammatica,
            comprensione = comprensione,
            produzione = produzione,
            livelloComplessivo = livelloFinale
        )
    }
}
