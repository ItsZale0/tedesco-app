package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD tests for CompetenzeScreen display logic.
 *
 * CompetenzeScreen should display 4 competence scores (lessico, grammatica,
 * comprensione, produzione) plus livelloComplessivo. These tests define
 * the contract for the display layer.
 */
class CompetenzeScreenTest {

    private val NOW = 1_700_000_000_000L

    private fun parola(id: String, archived: Boolean = false) = WordEntity(
        id = id,
        german = "w$id",
        italian = "p$id",
        archived = archived
    )

    private fun review(wordId: String, repetitions: Int, lapses: Int = 0) = ReviewEntity(
        wordId = wordId,
        repetitions = repetitions,
        lapses = lapses
    )

    // --- Display data model tests ---

    @Test
    fun `PunteggiCompetenze has all 4 scores plus livello`() {
        val punteggi = CalcoloCompetenze.PunteggiCompetenze(
            lessico = 50f,
            grammatica = 60f,
            comprensione = 70f,
            produzione = 80f,
            livelloComplessivo = LivelloCEFR.B1
        )
        assertEquals(50f, punteggi.lessico, 0.01f)
        assertEquals(60f, punteggi.grammatica, 0.01f)
        assertEquals(70f, punteggi.comprensione, 0.01f)
        assertEquals(80f, punteggi.produzione, 0.01f)
        assertEquals(LivelloCEFR.B1, punteggi.livelloComplessivo)
    }

    // --- Weighted average calculation tests ---

    @Test
    fun `livelloComplessivo uses weighted average 30-30-20-20`() {
        val parole = (1..1000).map { parola("p$it") }
        val reviews = (1..1000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertEquals(50f, risultato.lessico, 0.01f)
        assertEquals(100f, risultato.grammatica, 0.01f)
        assertEquals(0f, risultato.comprensione, 0.01f)
        assertEquals(0f, risultato.produzione, 0.01f)
        // 50*0.3 + 100*0.3 + 0*0.2 + 0*0.2 = 45 -> A2
        assertEquals(LivelloCEFR.A2, risultato.livelloComplessivo)
    }

    @Test
    fun `livelloComplessivo with all 100 gives C1`() {
        val parole = (1..2000).map { parola("p$it") }
        val reviews = (1..2000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 1f),
            now = NOW
        )
        assertEquals(100f, risultato.lessico, 0.01f)
        assertEquals(100f, risultato.grammatica, 0.01f)
        assertEquals(100f, risultato.comprensione, 0.01f)
        assertEquals(100f, risultato.produzione, 0.01f)
        assertEquals(LivelloCEFR.C1, risultato.livelloComplessivo)
    }

    // --- Boundary tests ---

    @Test
    fun `lessico capped at 100 when more than 2000 mature words`() {
        val parole = (1..3000).map { parola("p$it") }
        val reviews = (1..3000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertEquals(100f, risultato.lessico, 0.01f)
    }

    @Test
    fun `lessico is 0 when no mature words`() {
        val parole = listOf(parola("1"), parola("2"))
        val reviews = mapOf(
            "1" to review("1", repetitions = 2),
            "2" to review("2", repetitions = 1)
        )
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertEquals(0f, risultato.lessico, 0.01f)
    }

    @Test
    fun `grammatica uses accuratezzaMedia when no reviews`() {
        val parole = listOf(parola("1"), parola("2"))
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.75f),
            now = NOW
        )
        assertEquals(75f, risultato.grammatica, 0.01f)
    }

    @Test
    fun `grammatica is 0 when reviews exist but no repetitions or lapses`() {
        val parole = listOf(parola("1"))
        val reviews = mapOf("1" to review("1", repetitions = 0, lapses = 0))
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 1f),
            now = NOW
        )
        assertEquals(0f, risultato.grammatica, 0.01f)
    }

    @Test
    fun `comprensione and produzione use accuratezzaMedia as proxy`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.6f),
            now = NOW
        )
        assertEquals(60f, risultato.comprensione, 0.01f)
        assertEquals(60f, risultato.produzione, 0.01f)
    }

    @Test
    fun `comprensione and produzione are 0 when accuratezzaMedia is 0`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertEquals(0f, risultato.comprensione, 0.01f)
        assertEquals(0f, risultato.produzione, 0.01f)
    }

    // --- Livello non scende tests ---

    @Test
    fun `livello non scende sotto livelloCorrente when calculated is lower`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B2),
            now = NOW
        )
        assertEquals(LivelloCEFR.B2, risultato.livelloComplessivo)
    }

    @Test
    fun `livello non scende sotto livelloCorrente when calculated is A0`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.A2),
            now = NOW
        )
        assertEquals(LivelloCEFR.A2, risultato.livelloComplessivo)
    }

    @Test
    fun `livello can go up when calculated is higher than livelloCorrente`() {
        val parole = (1..2000).map { parola("p$it") }
        val reviews = (1..2000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(
                livelloCorrente = LivelloCEFR.A0,
                accuratezzaMedia = 1f
            ),
            now = NOW
        )
        // All 100 -> C1, which is higher than A0
        assertEquals(LivelloCEFR.C1, risultato.livelloComplessivo)
    }

    // --- Archived words excluded ---

    @Test
    fun `archived words excluded from lessico calculation`() {
        val parole = listOf(
            parola("1", archived = false),
            parola("2", archived = true),
            parola("3", archived = true)
        )
        val reviews = mapOf(
            "1" to review("1", repetitions = 5),
            "2" to review("2", repetitions = 5),
            "3" to review("3", repetitions = 5)
        )
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        // Only 1 active word, mature -> lessico = 1/2000*100 = 0.05
        assertTrue("Lessico should be > 0", risultato.lessico > 0f)
        assertTrue("Lessico should be < 1", risultato.lessico < 1f)
    }

    // --- Integration with CalcoloStatistiche ---

    @Test
    fun `Statistiche includes punteggiCompetenze`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = NOW
        )
        assertTrue("Statistiche should include punteggiCompetenze", stats.punteggiCompetenze != null)
    }

    @Test
    fun `Statistiche punteggiCompetenze has correct values`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.5f),
            now = NOW
        )
        val punteggi = stats.punteggiCompetenze!!
        assertEquals(0f, punteggi.lessico, 0.01f)
        assertEquals(50f, punteggi.grammatica, 0.01f)
        assertEquals(50f, punteggi.comprensione, 0.01f)
        assertEquals(50f, punteggi.produzione, 0.01f)
    }

    // --- CEFR level boundary tests ---

    @Test
    fun `livelloComplessivo A0 when score below 20`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertEquals(LivelloCEFR.A0, risultato.livelloComplessivo)
    }

    @Test
    fun `livelloComplessivo A1 when score between 20 and 40`() {
        // No mature words, no reviews. accuratezzaMedia drives all three equally.
        // weighted = 0 + 30*0.3 + 30*0.2 + 30*0.2 = 21 -> A1
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(accuratezzaMedia = 0.3f),
            now = NOW
        )
        assertEquals(LivelloCEFR.A1, risultato.livelloComplessivo)
    }

    @Test
    fun `livelloComplessivo B1 when score between 60 and 75`() {
        // lessico=100, grammatica=100, comprensione=0, produzione=0
        // weighted = 100*0.3 + 100*0.3 + 0 + 0 = 60 -> A2 (soglia 60)
        // Need score >= 60 and < 75 for B1
        // lessico=100, grammatica=100, comprensione=50, produzione=50
        // weighted = 30 + 30 + 10 + 10 = 80 -> B2
        // Let's try: lessico=100, grammatica=100, comprensione=0, produzione=0 = 60 -> A2
        // lessico=100, grammatica=100, comprensione=25, produzione=25 = 30+30+5+5=70 -> B1
        val parole = (1..2000).map { parola("p$it") }
        val reviews = (1..2000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0.25f),
            now = NOW
        )
        assertEquals(LivelloCEFR.B1, risultato.livelloComplessivo)
    }

    @Test
    fun `livelloComplessivo B2 when score between 75 and 90`() {
        // lessico=100, grammatica=100, comprensione=50, produzione=50
        // weighted = 30 + 30 + 10 + 10 = 80 -> B2
        val parole = (1..2000).map { parola("p$it") }
        val reviews = (1..2000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0.5f),
            now = NOW
        )
        assertEquals(LivelloCEFR.B2, risultato.livelloComplessivo)
    }

    @Test
    fun `livelloComplessivo C1 when score 90 or above`() {
        // lessico=100, grammatica=100, comprensione=100, produzione=100
        // weighted = 30 + 30 + 20 + 20 = 100 -> C1
        val parole = (1..2000).map { parola("p$it") }
        val reviews = (1..2000).associate { "p$it" to review("p$it", repetitions = 5) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 1f),
            now = NOW
        )
        assertEquals(LivelloCEFR.C1, risultato.livelloComplessivo)
    }
}
