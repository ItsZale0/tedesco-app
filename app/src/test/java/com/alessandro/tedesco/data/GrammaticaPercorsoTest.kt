package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiloStato
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GrammaticaPercorsoTest {

    private val giorno = TimeUnit.DAYS.toMillis(1)
    private val adesso = 1_700_000_000_000L

    @Test
    fun ciSonoTreUnitaPilota() {
        assertEquals(3, GrammaticaSyllabus.unitaPilota.size)
        assertEquals(
            listOf("a1-akkusativ", "a1-praesens", "a2-perfekt"),
            GrammaticaSyllabus.unitaPilota.map { it.id }
        )
    }

    @Test
    fun ogniUnitaHaTeoriaEdEsercizi() {
        GrammaticaSyllabus.unitaPilota.forEach { unita ->
            assertTrue("Teoria mancante in ${unita.id}", unita.teoria.regola.size >= 3)
            assertTrue("Esempi mancanti in ${unita.id}", unita.teoria.esempi.size >= 3)
            assertTrue("Tabelle mancanti in ${unita.id}", unita.teoria.tabelle.isNotEmpty())
            assertTrue("Errori tipici mancanti in ${unita.id}", unita.teoria.erroriTipici.isNotEmpty())
            assertTrue("Merksatz mancante in ${unita.id}", unita.teoria.merksatz.isNotBlank())
            assertTrue("Esercizi insufficienti in ${unita.id}", unita.esercizi.size >= 12)
            assertEquals(
                "ID esercizi duplicati in ${unita.id}",
                unita.esercizi.size,
                unita.esercizi.map { it.id }.distinct().size
            )
        }
    }

    @Test
    fun ogniEsercizioHaUnaRispostaValida() {
        GrammaticaSyllabus.unitaPilota.forEach { unita ->
            unita.esercizi.forEach { esercizio ->
                when (esercizio.tipo) {
                    TipoEsercizioGrammatica.SCELTA_MULTIPLA -> {
                        assertTrue(
                            "Opzioni mancanti in ${esercizio.id}",
                            esercizio.opzioni.size >= 2
                        )
                        assertEquals(
                            "Opzioni duplicate in ${esercizio.id}",
                            esercizio.opzioni.size,
                            esercizio.opzioni.distinct().size
                        )
                        assertTrue(
                            "Risposta fuori range in ${esercizio.id}",
                            esercizio.rispostaCorretta in esercizio.opzioni.indices
                        )
                    }

                    else -> {
                        assertTrue(
                            "Risposta testuale mancante in ${esercizio.id}",
                            esercizio.rispostaTesto.isNotBlank()
                        )
                    }
                }
                assertTrue(
                    "Spiegazione mancante in ${esercizio.id}",
                    esercizio.spiegazione.isNotBlank()
                )
            }
        }
    }

    @Test
    fun iPrerequisitiEsistono() {
        val ids = GrammaticaSyllabus.unitaPilota.map { it.id }.toSet()
        GrammaticaSyllabus.unitaPilota.forEach { unita ->
            unita.prerequisiti.forEach { prerequisito ->
                assertTrue(
                    "Prerequisito inesistente ${prerequisito} in ${unita.id}",
                    prerequisito in ids
                )
            }
        }
    }

    @Test
    fun conProfiloVuotoSoloLaPrimaUnitaEDisponibile() {
        val percorso = GrammaticaSyllabus.percorso(emptyMap(), adesso)
        assertEquals(StatoUnita.DISPONIBILE, percorso[0].stato)
        assertEquals(StatoUnita.BLOCCATA, percorso[1].stato)
        assertEquals(StatoUnita.BLOCCATA, percorso[2].stato)
    }

    @Test
    fun completareLaPrimaUnitaSbloccaLaSeconda() {
        val prima = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val mappa = mapOf("a1-akkusativ" to prima)
        val percorso = GrammaticaSyllabus.percorso(mappa, adesso)
        assertEquals(StatoUnita.COMPLETATA, percorso[0].stato)
        assertEquals(StatoUnita.DISPONIBILE, percorso[1].stato)
        assertEquals(StatoUnita.BLOCCATA, percorso[2].stato)
    }

    @Test
    fun laSogliaEPercentoNonPerNumeroAssoluto() {
        val sottoSoglia = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 9,
            totali = 12,
            now = adesso
        )
        assertFalse(sottoSoglia.completata)
        assertEquals(75, sottoSoglia.percentuale)
        assertEquals(0, sottoSoglia.ripassi)

        val sopraSoglia = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        assertTrue(sopraSoglia.completata)
        assertEquals(83, sopraSoglia.percentuale)
        assertEquals(1, sopraSoglia.ripassi)
        assertEquals(adesso + giorno, sopraSoglia.prossimaRipasso)
    }

    @Test
    fun gliIntervalliDiRipassoSonoUnoTreSetteSedici() {
        var padronanza: PadronanzaGrammatica? = null
        val attesi = listOf(1, 3, 7, 16)

        attesi.forEach { giorni ->
            padronanza = GrammaticaSyllabus.valuta(
                unitaId = "a1-akkusativ",
                corrette = 10,
                totali = 12,
                precedente = padronanza,
                now = adesso
            )
            assertEquals(
                "Intervallo errato al ripasso ${padronanza!!.ripassi}",
                adesso + giorni * giorno,
                padronanza!!.prossimaRipasso
            )
        }
    }

    @Test
    fun unRipassoFallitoNonRichiedeLaUnitaGiaCompletata() {
        val completata = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val fallita = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 6,
            totali = 12,
            precedente = completata,
            now = adesso + 2 * giorno
        )
        assertTrue(fallita.completata)
        assertEquals(50, fallita.percentuale)
        assertEquals(adesso + 3 * giorno, fallita.prossimaRipasso)
    }

    @Test
    fun unaUnitaCompletataEDaRipassareQuandoScade() {
        val completata = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val mappa = mapOf("a1-akkusativ" to completata)

        val primaDellaScadenza = GrammaticaSyllabus.percorso(mappa, adesso + giorno / 2)
        assertEquals(StatoUnita.COMPLETATA, primaDellaScadenza[0].stato)

        val dopoLaScadenza = GrammaticaSyllabus.percorso(mappa, adesso + 2 * giorno)
        assertEquals(StatoUnita.DA_RIPASSARE, dopoLaScadenza[0].stato)
    }

    @Test
    fun laProssimaUnitaPreferisceIlRipassoScaduto() {
        val completata = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val mappa = mapOf("a1-akkusativ" to completata)
        val prossima = GrammaticaSyllabus.prossimaUnita(mappa, adesso + 2 * giorno)
        assertNotNull(prossima)
        assertEquals("a1-akkusativ", prossima!!.unita.id)
        assertEquals(StatoUnita.DA_RIPASSARE, prossima.stato)
    }

    @Test
    fun normalizzaMaiuscoleSpaziEPunteggiatura() {
        assertEquals("ich sehe den hund", GrammaticaSyllabus.normalizza("  Ich  sehe   den Hund.  "))
        assertEquals("ich sehe den hund", GrammaticaSyllabus.normalizza("ICH SEHE DEN HUND!"))
        assertEquals("du arbeitest viel", GrammaticaSyllabus.normalizza("Du arbeitest viel?"))
    }

    @Test
    fun accettaLaScritturaSenzaUmlaut() {
        assertEquals(
            GrammaticaSyllabus.traslittera("Ich fähre"),
            GrammaticaSyllabus.traslittera("Ich faehre")
        )
        assertTrue(
            GrammaticaSyllabus.rispostaTestoCorretta(
                EsercizioUnita(
                    id = "test",
                    tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
                    domanda = "Partizip II di sehen",
                    rispostaTesto = "gesehen"
                ),
                "  GESEHEN. "
            )
        )
    }

    @Test
    fun laRispostaTestualeAccettaLeAlternative() {
        val esercizio = EsercizioUnita(
            id = "test",
            tipo = TipoEsercizioGrammatica.PRODUZIONE,
            domanda = "Traduci",
            rispostaTesto = "Ich arbeite im Krankenhaus.",
            alternative = listOf("Ich arbeite in einem Krankenhaus.")
        )
        assertTrue(GrammaticaSyllabus.rispostaTestoCorretta(esercizio, "ich arbeite im krankenhaus"))
        assertTrue(GrammaticaSyllabus.rispostaTestoCorretta(esercizio, "ICH ARBEITE IN EINEM KRANKENHAUS"))
        assertFalse(GrammaticaSyllabus.rispostaTestoCorretta(esercizio, "Ich arbeite"))
    }

    @Test
    fun ilProfiloSalvaEPuoiRileggereLaPadronanza() {
        val padronanza = GrammaticaSyllabus.valuta(
            unitaId = "a1-akkusativ",
            corrette = 10,
            totali = 12,
            now = adesso
        )
        val stato = ProfiloStato(
            padronanzaGrammatica = mapOf("a1-akkusativ" to padronanza)
        )
        val json = Json.encodeToString(stato)
        val riletto = Json.decodeFromString<ProfiloStato>(json)
        assertEquals(83, riletto.padronanzaGrammatica["a1-akkusativ"]?.percentuale)
        assertTrue(riletto.padronanzaGrammatica["a1-akkusativ"]?.completata == true)
    }
}
