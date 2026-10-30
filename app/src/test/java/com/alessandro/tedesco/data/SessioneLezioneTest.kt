package com.alessandro.tedesco.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La sessione di una lezione deve cambiare da un tentativo all'altro, ma restare
 * stabile mentre lo studente risponde (altrimenti la domanda cambierebbe sotto
 * le mani). Questi test bloccano le due regressioni opposte.
 */
class SessioneLezioneTest {

    private fun unitaRicca(): UnitaGrammatica =
        GrammaticaSyllabus.unita.first { it.esercizi.size >= 12 }

    @Test
    fun `la stessa semina produce la stessa sessione`() {
        val unita = unitaRicca()
        val a = SessioneLezione.esercizi(unita, seme = 123456)
        val b = SessioneLezione.esercizi(unita, seme = 123456)
        assertEquals(a.map { it.id }, b.map { it.id })
    }

    @Test
    fun `semi diversi producono sessioni diverse`() {
        val unita = unitaRicca()
        val a = SessioneLezione.esercizi(unita, seme = 1).map { it.id }.toSet()
        val b = SessioneLezione.esercizi(unita, seme = 999).map { it.id }.toSet()
        assertNotEquals("La sessione non cambia al cambio di tentativo", a, b)
    }

    @Test
    fun `la sessione rispetta la dimensione richiesta`() {
        val unita = unitaRicca()
        assertEquals(8, SessioneLezione.esercizi(unita, seme = 7).size)
        assertEquals(5, SessioneLezione.esercizi(unita, seme = 7, quanti = 5).size)
    }

    @Test
    fun `con un banco piccolo si mostrano tutti gli esercizi`() {
        val piccola = UnitaGrammatica(
            id = "test-piccola",
            livello = "A1",
            titolo = "Test",
            sottotitolo = "",
            obiettivo = "",
            minuti = 5,
            teoria = TeoriaGrammatica(regola = listOf("Regola di test.")),
            esercizi = listOf(
                sceltaUnita("t1", "A?", listOf("a", "b"), 0, "spiegazione"),
                sceltaUnita("t2", "B?", listOf("a", "b"), 0, "spiegazione"),
                sceltaUnita("t3", "C?", listOf("a", "b"), 0, "spiegazione")
            )
        )
        assertEquals(3, SessioneLezione.esercizi(piccola, seme = 1).size)
    }

    @Test
    fun `non ci sono esercizi ripetuti nella sessione`() {
        val unita = unitaRicca()
        repeat(20) { seme ->
            val ids = SessioneLezione.esercizi(unita, seme = seme).map { it.id }
            assertEquals("Sessione con duplicati al seme $seme", ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `la sessione contiene tutti i tipi presenti nel banco`() {
        val unita = unitaRicca()
        val tipiUnita = unita.esercizi.map { it.tipo }.toSet()
        val tipiSessione = SessioneLezione.esercizi(unita, seme = 42).map { it.tipo }.toSet()
        assertTrue(
            "Tipi mancanti nella sessione: ${tipiUnita - tipiSessione}",
            tipiSessione.containsAll(tipiUnita)
        )
    }

    @Test
    fun `le opzioni sono mescolate ma la risposta corretta resta giusta`() {
        val unita = unitaRicca()
        val originali = unita.esercizi.filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
        val perId = originali.associateBy { it.id }
        repeat(30) { seme ->
            SessioneLezione.esercizi(unita, seme = seme)
                .filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
                .forEach { vista ->
                    val originale = perId.getValue(vista.id)
                    val testoOriginale = originale.opzioni[originale.rispostaCorretta]
                    assertEquals(
                        "La risposta corretta è cambiata per ${vista.id}",
                        testoOriginale,
                        vista.opzioni[vista.rispostaCorretta]
                    )
                    assertEquals(
                        "Le opzioni sono state alterate per ${vista.id}",
                        originale.opzioni.sorted(),
                        vista.opzioni.sorted()
                    )
                }
        }
    }

    @Test
    fun `almeno una posizione della risposta cambia tra tentativi`() {
        val unita = unitaRicca()
        val posizioni = (1..12).map { seme ->
            SessioneLezione.esercizi(unita, seme = seme)
                .filter { it.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA }
                .map { it.rispostaCorretta }
        }
        assertTrue(
            "La posizione della risposta corretta non cambia mai",
            posizioni.toSet().size > 1
        )
    }
}
