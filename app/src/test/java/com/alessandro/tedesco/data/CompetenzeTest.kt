package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.TestComprensione
import com.alessandro.tedesco.data.local.TestGrammatica
import com.alessandro.tedesco.data.local.TestProduzione
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetenzeTest {

    private val NOW = 1_700_000_000_000L

    private fun parola(id: String, archived: Boolean = false) = WordEntity(
        id = id, german = "w$id", italian = "p$id", archived = archived
    )

    private fun review(wordId: String, repetitions: Int, lapses: Int = 0) = ReviewEntity(
        wordId = wordId, repetitions = repetitions, lapses = lapses
    )

    private fun testGramm(p: Float) = TestGrammatica(data = NOW, punteggio = p, errori = 0, totale = 10)
    private fun testComp(p: Float) = TestComprensione(data = NOW, punteggio = p, errori = 0, totale = 10)
    private fun testProd(p: Float) = TestProduzione(data = NOW, punteggio = p, errori = 0, totale = 10)

    // 1. Lista vuota -> tutti 0, livello A0
    @Test
    fun `lista vuota da tutti zero e livello A0`() {
        val r = CalcoloCompetenze.calcola(emptyList(), emptyMap(), ProgressoUtente(), NOW)
        assertEquals(0f, r.lessico, 0.01f)
        assertEquals(0f, r.grammatica, 0.01f)
        assertEquals(0f, r.comprensione, 0.01f)
        assertEquals(0f, r.produzione, 0.01f)
        assertEquals(LivelloCEFR.A0, r.livelloComplessivo)
    }

    // 2. Accuratezza 100% dalle review
    @Test
    fun `accuratezza 100 percento da grammatica 100`() {
        val parole = listOf(parola("1"), parola("2"))
        val reviews = mapOf("1" to review("1", 5, 0), "2" to review("2", 3, 0))
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW)
        assertEquals(100f, r.grammatica, 0.01f)
    }

    // 3. Accuratezza 50%
    @Test
    fun `accuratezza 50 percento da grammatica 50`() {
        val parole = listOf(parola("1"), parola("2"))
        val reviews = mapOf("1" to review("1", 5, 5), "2" to review("2", 3, 3))
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW)
        assertEquals(50f, r.grammatica, 0.01f)
    }

    // 4. SENZA test non si sale di livello (no fallback gonfiato)
    @Test
    fun `senza test resta A0 anche con lessico alto`() {
        val parole = (1..200).map { parola(it.toString()) }
        val reviews = (1..200).associate { it.toString() to review(it.toString(), 5, 0) }
        val r = CalcoloCompetenze.calcola(parole, reviews, ProgressoUtente(), NOW)
        assertEquals(LivelloCEFR.A0, r.livelloComplessivo)
    }

    // 5. Con test scarsi NON si sale (il caso "pronto per A2" sbagliato)
    @Test
    fun `test insufficienti non promuovono`() {
        val parole = (1..50).map { parola(it.toString()) }
        val reviews = (1..50).associate { it.toString() to review(it.toString(), 5, 0) }
        val prog = ProgressoUtente(
            testGrammatica = List(3) { testGramm(80f) },
            testComprensione = List(3) { testComp(45f) },   // sotto soglia A1 (70)
            testProduzione = List(3) { testProd(80f) }
        )
        val r = CalcoloCompetenze.calcola(parole, reviews, prog, NOW)
        assertEquals(LivelloCEFR.A0, r.livelloComplessivo)
    }

    // 6. Tutte le dimensioni sopra soglia -> A1
    @Test
    fun `tutte le dimensioni sopra soglia promuovono ad A1`() {
        val parole = (1..40).map { parola(it.toString()) }
        val reviews = (1..40).associate { it.toString() to review(it.toString(), 5, 0) }
        val prog = ProgressoUtente(
            testGrammatica = List(3) { testGramm(75f) },
            testComprensione = List(3) { testComp(75f) },
            testProduzione = List(3) { testProd(75f) }
        )
        val r = CalcoloCompetenze.calcola(parole, reviews, prog, NOW)
        assertEquals(LivelloCEFR.A1, r.livelloComplessivo)
    }

    // 7. Maglia piu debole: una dimensione sotto soglia blocca il livello
    @Test
    fun `una dimensione debole blocca la promozione`() {
        val parole = (1..40).map { parola(it.toString()) }
        val reviews = (1..40).associate { it.toString() to review(it.toString(), 5, 0) }
        val prog = ProgressoUtente(
            testGrammatica = List(3) { testGramm(95f) },
            testComprensione = List(3) { testComp(95f) },
            testProduzione = List(3) { testProd(60f) }   // sotto 70
        )
        val r = CalcoloCompetenze.calcola(parole, reviews, prog, NOW)
        assertEquals(LivelloCEFR.A0, r.livelloComplessivo)
    }
}
