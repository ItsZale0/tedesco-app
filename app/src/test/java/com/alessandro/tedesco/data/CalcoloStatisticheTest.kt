package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcoloStatisticheTest {

    private val now = 1_700_000_000_000L // timestamp arbitrario

    private fun parola(
        id: String,
        lesson: Int = 1,
        archived: Boolean = false,
        source: WordSource = WordSource.SYNCED
    ) = WordEntity(
        id = id,
        german = "w$id",
        italian = "p$id",
        lesson = lesson,
        archived = archived,
        source = source
    )

    private fun review(
        wordId: String,
        repetitions: Int = 0,
        lapses: Int = 0,
        dueAt: Long = 0L,
        lastReviewedAt: Long? = null
    ) = ReviewEntity(
        wordId = wordId,
        repetitions = repetitions,
        lapses = lapses,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt
    )

    // --- Test 1: lista vuota ---
    @Test
    fun `lista vuota restituisce zeri`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = now
        )
        assertEquals(0, stats.paroleTotali)
        assertEquals(0, stats.paroleSynced)
        assertEquals(0, stats.paroleCustom)
        assertEquals(0, stats.daRipassare)
        assertEquals(0, stats.nuove)
        assertEquals(0, stats.inApprendimento)
        assertEquals(0, stats.mature)
        assertEquals(0, stats.lezioniTotali)
        assertEquals(0f, stats.accuratezza, 0.001f)
        assertEquals(0, stats.ripassiTotali)
        assertEquals(0, stats.erroriTotali)
        assertEquals(0, stats.paroleApprese)
        assertEquals(0f, stats.progressoB1, 0.001f)
        assertEquals(LivelloCEFR.A0, stats.livelloStimato)
        assertTrue(stats.perLezione.isEmpty())
        assertEquals(7, stats.ultimi7giorni.size)
    }

    // --- Test 2: conteggio nuove / mature ---
    @Test
    fun `conteggio nuove inApprendimento mature`() {
        val parole = listOf(
            parola("a"), // nuova (senza review)
            parola("b"), // in apprendimento (repetitions 2)
            parola("c"), // mature (repetitions 5)
            parola("d")  // mature (repetitions 4)
        )
        val reviews = mapOf(
            "b" to review("b", repetitions = 2),
            "c" to review("c", repetitions = 5),
            "d" to review("d", repetitions = 4)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(1, stats.nuove)
        assertEquals(1, stats.inApprendimento)
        assertEquals(2, stats.mature)
        assertEquals(2, stats.paroleApprese)
    }

    // --- Test 3: calcolo accuratezza ---
    @Test
    fun `accuratezza calcolata correttamente`() {
        val parole = listOf(parola("a"), parola("b"))
        val reviews = mapOf(
            "a" to review("a", repetitions = 8, lapses = 2),
            "b" to review("b", repetitions = 4, lapses = 2)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        // ripassiTotali = 12, erroriTotali = 4, accuratezza = 12/16 = 0.75
        assertEquals(12, stats.ripassiTotali)
        assertEquals(4, stats.erroriTotali)
        assertEquals(0.75f, stats.accuratezza, 0.001f)
    }

    // --- Test 4: parole archiviate ignorate ---
    @Test
    fun `parole archiviate ignorate in tutti i conteggi`() {
        val parole = listOf(
            parola("a", archived = false),
            parola("b", archived = true),
            parola("c", archived = true)
        )
        val reviews = mapOf(
            "a" to review("a", repetitions = 5),
            "b" to review("b", repetitions = 5),
            "c" to review("c", repetitions = 5)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(1, stats.paroleTotali)
        // parola "a" non è archiviata e ha source=SYNCED
        assertEquals(1, stats.paroleSynced)
        assertEquals(1, stats.mature)
        assertEquals(1, stats.paroleApprese)
        assertEquals(1, stats.lezioniTotali)
    }

    // --- Test 5: progressoB1 non oltre 1f ---
    @Test
    fun `progressoB1 non supera 1f`() {
        // Crea 2500 parole mature (oltre l'obiettivo di 2000)
        val parole = (1..2500).map { parola("p$it") }
        val reviews = (1..2500).associate { "p$it" to review("p$it", repetitions = 5) }
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(2500, stats.paroleApprese)
        assertEquals(1f, stats.progressoB1, 0.001f)
    }

    // --- Test 6: livelloStimato non scende sotto progresso.livelloCorrente ---
    @Test
    fun `livelloStimato non scende sotto livelloCorrente`() {
        // Solo 10 parole mature -> livello calcolato A0
        val parole = (1..10).map { parola("p$it") }
        val reviews = (1..10).associate { "p$it" to review("p$it", repetitions = 5) }
        val progresso = ProgressoUtente(livelloCorrente = LivelloCEFR.B1)
        val stats = CalcoloStatistiche.calcola(parole, reviews, progresso, now)
        // Livello calcolato sarebbe A0, ma livelloCorrente è B1 -> deve restare B1
        assertEquals(LivelloCEFR.B1, stats.livelloStimato)
    }

    // --- Test 7: 7 giorni di attivita' ---
    @Test
    fun `ultimi 7 giorni sempre 7 elementi`() {
        val stats = CalcoloStatistiche.calcola(
            parole = emptyList(),
            reviews = emptyMap(),
            progresso = ProgressoUtente(),
            now = now
        )
        assertEquals(7, stats.ultimi7giorni.size)
        // Verifica che l'ultimo elemento sia oggi
        val oggi = java.time.LocalDate.ofEpochDay(now / (1000 * 60 * 60 * 24))
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.ITALIAN)
        val etichettaOggi = oggi.format(formatter)
        assertEquals(etichettaOggi, stats.ultimi7giorni.last().etichetta)
    }

    // --- Test 8: livelloStimato cresce con paroleApprese ---
    @Test
    fun `livelloStimato cresce con paroleApprese`() {
        // 100 parole mature -> A1
        val parole100 = (1..100).map { parola("p$it") }
        val reviews100 = (1..100).associate { "p$it" to review("p$it", repetitions = 5) }
        val stats100 = CalcoloStatistiche.calcola(parole100, reviews100, ProgressoUtente(), now)
        assertEquals(LivelloCEFR.A1, stats100.livelloStimato)

        // 500 parole mature -> B1 (soglia: <400=A2, <900=B1)
        val parole500 = (1..500).map { parola("p$it") }
        val reviews500 = (1..500).associate { "p$it" to review("p$it", repetitions = 5) }
        val stats500 = CalcoloStatistiche.calcola(parole500, reviews500, ProgressoUtente(), now)
        assertEquals(LivelloCEFR.B1, stats500.livelloStimato)

        // 1000 parole mature -> B2 (soglia: <900=B1, <1800=B2)
        val parole1000 = (1..1000).map { parola("p$it") }
        val reviews1000 = (1..1000).associate { "p$it" to review("p$it", repetitions = 5) }
        val stats1000 = CalcoloStatistiche.calcola(parole1000, reviews1000, ProgressoUtente(), now)
        assertEquals(LivelloCEFR.B2, stats1000.livelloStimato)
    }

    // --- Test 9: daRipassare ---
    @Test
    fun `daRipassare conta solo review scadute`() {
        val parole = listOf(parola("a"), parola("b"), parola("c"))
        val reviews = mapOf(
            "a" to review("a", dueAt = now - 1000), // scaduta
            "b" to review("b", dueAt = now + 1000), // non scaduta
            "c" to review("c", dueAt = now)          // scaduta (dueAt <= now)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(2, stats.daRipassare)
    }

    // --- Test 10: perLezione ordinata e corretta ---
    @Test
    fun `perLezione ordinata per lezione crescente`() {
        val parole = listOf(
            parola("a", lesson = 3),
            parola("b", lesson = 1),
            parola("c", lesson = 2),
            parola("d", lesson = 1)
        )
        val reviews = mapOf(
            "a" to review("a", repetitions = 5),
            "b" to review("b", repetitions = 2),
            "c" to review("c", repetitions = 5),
            "d" to review("d", repetitions = 0)
        )
        val stats = CalcoloStatistiche.calcola(parole, reviews, ProgressoUtente(), now)
        assertEquals(3, stats.perLezione.size)
        assertEquals(1, stats.perLezione[0].lezione)
        assertEquals(2, stats.perLezione[0].totale)
        assertEquals(0, stats.perLezione[0].mature)
        assertEquals(2, stats.perLezione[1].lezione)
        assertEquals(1, stats.perLezione[1].totale)
        assertEquals(1, stats.perLezione[1].mature)
        assertEquals(3, stats.perLezione[2].lezione)
        assertEquals(1, stats.perLezione[2].totale)
        assertEquals(1, stats.perLezione[2].mature)
    }
}
