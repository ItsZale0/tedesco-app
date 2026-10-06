package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetenzeTest {

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

    // 1. Lista vuota -> tutti 0
    @Test
    fun `lista vuota da tutti zero`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = NOW
        )
        assertEquals(0f, risultato.lessico, 0.01f)
        assertEquals(0f, risultato.grammatica, 0.01f)
        assertEquals(0f, risultato.comprensione, 0.01f)
        assertEquals(0f, risultato.produzione, 0.01f)
    }

    // 2. Solo lessico -> solo lessico > 0
    @Test
    fun `solo lessico attivo`() {
        val parole = listOf(parola("1"), parola("2"), parola("3"))
        val reviews = mapOf(
            "1" to review("1", repetitions = 5),
            "2" to review("2", repetitions = 4),
            "3" to review("3", repetitions = 0)
        )
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertTrue("Lessico dovrebbe essere > 0", risultato.lessico > 0f)
        // Con review senza errori, grammatica = 100 (non 0)
        assertTrue("Grammatica dovrebbe essere > 0 con review senza errori", risultato.grammatica > 0f)
    }

    // 3. Accuratezza 100% -> grammatica 100
    @Test
    fun `accuratezza 100 percento da grammatica 100`() {
        val parole = listOf(parola("1"), parola("2"))
        val reviews = mapOf(
            "1" to review("1", repetitions = 5, lapses = 0),
            "2" to review("2", repetitions = 3, lapses = 0)
        )
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 1f),
            now = NOW
        )
        assertEquals(100f, risultato.grammatica, 0.01f)
    }

    // 4. Accuratezza 50% -> grammatica 50
    @Test
    fun `accuratezza 50 percento da grammatica 50`() {
        val parole = listOf(parola("1"), parola("2"))
        val reviews = mapOf(
            "1" to review("1", repetitions = 5, lapses = 5),
            "2" to review("2", repetitions = 3, lapses = 3)
        )
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0.5f),
            now = NOW
        )
        assertEquals(50f, risultato.grammatica, 0.01f)
    }

    // 5. Livello non scende sotto livelloCorrente
    @Test
    fun `livello non scende sotto livelloCorrente`() {
        val risultato = CalcoloCompetenze.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1),
            now = NOW
        )
        assertEquals(LivelloCEFR.B1, risultato.livelloComplessivo)
    }

    // 6. Punteggio complessivo calcolato correttamente
    @Test
    fun `punteggio complessivo calcolato correttamente`() {
        // 100 parole mature -> lessico = 5f
        // reviews: 10 ripassi, 0 errori -> grammatica = 100f
        // comprensione e produzione = 0f (accuratezzaMedia = 0)
        // complessivo = 5*0.3 + 100*0.3 + 0*0.2 + 0*0.2 = 1.5 + 30 = 31.5
        val parole = (1..100).map { parola(it.toString()) }
        val reviews = (1..100).associate { it.toString() to review(it.toString(), repetitions = 4) }
        val risultato = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = ProgressoUtente(accuratezzaMedia = 0f),
            now = NOW
        )
        assertEquals(5f, risultato.lessico, 0.01f)
        assertEquals(100f, risultato.grammatica, 0.01f)
        assertEquals(0f, risultato.comprensione, 0.01f)
        assertEquals(0f, risultato.produzione, 0.01f)
        // 31.5 -> A1 (soglia 40)
        assertEquals(LivelloCEFR.A1, risultato.livelloComplessivo)
    }
}
