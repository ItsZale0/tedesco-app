package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test della verifica OGGETTIVA del lessico (scelta multipla, non self-report). */
class VerificaLessicoTest {

    private fun parola(id: String, de: String, it: String) =
        WordEntity(id = id, german = de, italian = it)

    private fun log(id: String, corretta: Boolean, ts: Long = 1000L) =
        RipassoLogEntity(wordId = id, timestamp = ts, corretta = corretta)

    @Test
    fun `le domande hanno la risposta corretta tra le opzioni`() {
        val parole = listOf(
            parola("1", "das Haus", "la casa"),
            parola("2", "der Hund", "il cane"),
            parola("3", "die Katze", "il gatto"),
            parola("4", "das Buch", "il libro")
        )
        val domande = VerificaLessico.costruisciDomande(parole)
        assertEquals(4, domande.size)
        domande.forEach { d ->
            assertTrue("opzioni non vuote", d.opzioni.isNotEmpty())
            assertTrue("corretta presente", d.opzioni.contains(d.corretta))
        }
    }

    @Test
    fun `i distrattori non includono lo stesso significato della risposta`() {
        val parole = listOf(
            parola("1", "das Haus", "la casa"),
            parola("2", "die Wohnung", "la casa"),  // stesso significato!
            parola("3", "der Hund", "il cane"),
            parola("4", "das Buch", "il libro")
        )
        val domande = VerificaLessico.costruisciDomande(parole)
        val d = domande.first { it.parola.id == "1" }
        // "la casa" non deve apparire due volte
        assertEquals(1, d.opzioni.count { it == "la casa" })
    }

    @Test
    fun `punteggio lessico e percentuale di verificate`() {
        val verifiche = mapOf("1" to true, "2" to true, "3" to false, "4" to false)
        assertEquals(50f, VerificaLessico.punteggioLessico(4, verifiche), 0.01f)
        assertEquals(0f, VerificaLessico.punteggioLessico(0, emptyMap()), 0.01f)
    }

    @Test
    fun `una parola e verificata solo con almeno 2 corrette e ultima corretta`() {
        // 1 sola corretta -> NON verificata (colpo di fortuna)
        val v1 = VerificaLessico.verificheDaLog(listOf(log("a", true, 1)))
        assertFalse(v1["a"] ?: false)

        // 2 corrette, ultima corretta -> verificata
        val v2 = VerificaLessico.verificheDaLog(listOf(log("a", true, 1), log("a", true, 2)))
        assertTrue(v2["a"] ?: false)

        // 2 corrette ma ULTIMA sbagliata -> non verificata
        val v3 = VerificaLessico.verificheDaLog(listOf(log("a", true, 1), log("a", false, 2)))
        assertFalse(v3["a"] ?: false)
    }

    @Test
    fun `le parole fragili sono le sbagliate non verificate`() {
        val parole = listOf(parola("1", "a", "A"), parola("2", "b", "B"), parola("3", "c", "C"))
        val log = listOf(log("1", false), log("2", true, 1), log("2", true, 2))
        val verifiche = VerificaLessico.verificheDaLog(log)
        val fragili = VerificaLessico.paroleFragili(parole, log, verifiche)
        assertEquals(listOf("1"), fragili.map { it.id })
    }

    @Test
    fun `le parole da verificare escludono quelle gia verificate`() {
        val parole = (1..5).map { parola("$it", "w$it", "i$it") }
        val log = listOf(log("1", true, 1), log("1", true, 2))
        val verifiche = VerificaLessico.verificheDaLog(log)
        val daVerificare = VerificaLessico.paroleDaVerificare(parole, log, verifiche, 10)
        assertFalse(daVerificare.any { it.id == "1" })
        assertEquals(4, daVerificare.size)
    }

    @Test
    fun `esito calcola punteggio e parole verificate`() {
        val e = VerificaLessico.Esito(
            totale = 10, corrette = 7,
            paroleVerificate = listOf("a","b","c","d","e","f","g"),
            paroleDaRivedere = listOf("h","i","j")
        )
        assertEquals(70f, e.punteggio, 0.01f)
        assertEquals(7, e.paroleVerificate.size)
    }

    @Test
    fun `con meno di 2 parole non si generano domande`() {
        assertEquals(0, VerificaLessico.costruisciDomande(listOf(parola("1","a","A"))).size)
        assertEquals(0, VerificaLessico.costruisciDomande(emptyList()).size)
    }
}
