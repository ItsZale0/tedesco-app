package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test del motore di progresso unificato (dati reali, niente valori inventati). */
class CalcoloProgressoTest {

    private val NOW = 1_700_000_000_000L
    private val GIORNO = 86_400_000L

    private fun parola(id: String) = WordEntity(id = id, german = "w$id", italian = "i$id")
    private fun review(id: String, rep: Int) = ReviewEntity(wordId = id, repetitions = rep)
    private fun log(giorniFa: Long, corretta: Boolean = true) =
        RipassoLogEntity(wordId = "w", timestamp = NOW - giorniFa * GIORNO, corretta = corretta)

    @Test
    fun `progresso vuoto non inventa numeri`() {
        val p = CalcoloProgresso.calcola(emptyList(), emptyMap(), ProgressoUtente(), emptyList(), LivelloCEFR.B1, NOW)
        assertEquals(0, p.paroleImparate)
        assertEquals(0, p.ripassiTotali)
        assertEquals(0f, p.accuratezza, 0.001f)
        assertEquals(0, p.streakGiorni)
        assertEquals(LivelloCEFR.A0, p.livello)
        assertEquals(0, p.traguardiRaggiunti)
    }

    @Test
    fun `streak conta i giorni consecutivi dal log`() {
        val log = listOf(log(0), log(1), log(2), log(5))  // 3 giorni di fila, poi buco
        val p = CalcoloProgresso.calcola(emptyList(), emptyMap(), ProgressoUtente(), log, LivelloCEFR.B1, NOW)
        assertEquals(3, p.streakGiorni)
        assertEquals(4, p.giorniStudioTotali)
        assertEquals(3, p.streakRecord)
    }

    @Test
    fun `streak e zero se l ultima attivita e vecchia`() {
        val log = listOf(log(10), log(11))  // nessuna attivita' oggi o ieri
        val p = CalcoloProgresso.calcola(emptyList(), emptyMap(), ProgressoUtente(), log, LivelloCEFR.B1, NOW)
        assertEquals(0, p.streakGiorni)
        assertEquals(2, p.streakRecord)  // il record resta
    }

    @Test
    fun `accuratezza viene dal log`() {
        val log = listOf(log(0, true), log(0, true), log(0, false), log(0, true))
        val p = CalcoloProgresso.calcola(emptyList(), emptyMap(), ProgressoUtente(), log, LivelloCEFR.B1, NOW)
        assertEquals(4, p.ripassiTotali)
        assertEquals(0.75f, p.accuratezza, 0.001f)
    }

    @Test
    fun `dimensione debole indica cosa fare`() {
        // Solo lessico (parole mature), nessun test -> grammatica/comprensione/produzione = 0
        val parole = (1..40).map { parola("p$it") }
        val reviews = (1..40).associate { "p$it" to review("p$it", 5) }
        val p = CalcoloProgresso.calcola(parole, reviews, ProgressoUtente(), emptyList(), LivelloCEFR.B1, NOW)
        assertTrue(p.dimensioneDeboleValore <= 100f)
        assertTrue(p.azioneConsigliata.isNotBlank())
    }

    @Test
    fun `traguardi si sbloccano con i dati reali`() {
        val parole = (1..10).map { parola("p$it") }
        val reviews = (1..10).associate { "p$it" to review("p$it", 5) }
        val p = CalcoloProgresso.calcola(parole, reviews, ProgressoUtente(), emptyList(), LivelloCEFR.B1, NOW)
        assertEquals(10, p.paroleImparate)
        assertTrue(p.traguardiRaggiunti >= 2)  // "Prima parola" + "10 parole"
    }

    @Test
    fun `minuti studiati stimati dai ripassi`() {
        val log = List(30) { log(0) }  // 30 ripassi * 20s = 600s = 10 min
        val p = CalcoloProgresso.calcola(emptyList(), emptyMap(), ProgressoUtente(), log, LivelloCEFR.B1, NOW)
        assertEquals(10, p.minutiStudiati)
    }
}
