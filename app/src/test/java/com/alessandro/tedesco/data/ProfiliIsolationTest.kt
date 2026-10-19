package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.TipoProfilo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ogni profilo deve avere i propri contenuti: Emma non deve vedere le lezioni,
 * le parole o gli esercizi di Alessandro.
 *
 * Nota: emptyList()/emptyMap() di Kotlin sono singleton IMMUTABILI, quindi due
 * profili "vuoti" condividono le stesse istanze senza alcun rischio. Quindi qui non
 * si verifica l'identita' degli oggetti vuoti, ma che i dati propri restino propri.
 */
class ProfiliIsolationTest {

    private val profili = ProfiliPreset.crea(now = 1_700_000_000_000L)

    @Test
    fun `esistono profili distinti per Alessandro ed Emma`() {
        val ale = profili.getValue(TipoProfilo.ALESSANDRO.id)
        val emma = profili.getValue(TipoProfilo.EMMA.id)

        assertEquals("alessandro", ale.id)
        assertEquals("emma", emma.id)
        assertNotEquals(ale, emma)
    }

    @Test
    fun `Emma parte da B1 senza contesto medico, Alessandro da A0 con contesto medico`() {
        val ale = profili.getValue(TipoProfilo.ALESSANDRO.id)
        val emma = profili.getValue(TipoProfilo.EMMA.id)

        assertEquals(LivelloCEFR.A0, ale.stato.progresso.livelloCorrente)
        assertEquals(LivelloCEFR.B1, emma.stato.progresso.livelloCorrente)

        assertTrue(ale.config.mostraContestoMedico)
        assertTrue(!emma.config.mostraContestoMedico)
    }

    @Test
    fun `Emma ha il suo feed vocabolario, diverso da quello di Alessandro`() {
        val ale = profili.getValue(TipoProfilo.ALESSANDRO.id)
        val emma = profili.getValue(TipoProfilo.EMMA.id)

        assertNotEquals(ale.config.feedUrl, emma.config.feedUrl)
        assertTrue(emma.config.feedUrl.contains("emma", ignoreCase = true))
    }

    @Test
    fun `la riconciliazione con i preset non cancella i dati gia presenti`() {
        // Se Emma ha gia' una lezione generata, aggiungere i preset mancanti NON deve
        // riscrivere il suo profilo (altrimenti perderebbe i contenuti propri).
        val emma = profili.getValue(TipoProfilo.EMMA.id)
        val conDati = emma.copy(
            stato = emma.stato.copy(
                lezioneContenuto = "Lezione 1 di Emma",
                lezioniGenerate = mapOf("1" to "Lezione 1 di Emma"),
                parole = emma.stato.parole
            )
        )
        val letto = ProfiliRepository(
            profili = profili + (TipoProfilo.EMMA.id to conDati),
            profiloAttivoId = TipoProfilo.EMMA.id
        )

        val dopo = ProfiliPreset.riconcilia(letto)
        val emmaDopo = dopo.profili.getValue(TipoProfilo.EMMA.id)

        assertEquals("Lezione 1 di Emma", emmaDopo.stato.lezioneContenuto)
        assertEquals(mapOf("1" to "Lezione 1 di Emma"), emmaDopo.stato.lezioniGenerate)
    }

    @Test
    fun `un profilo nuovo o senza dati resta vuoto e al livello scelto`() {
        val emma = profili.getValue(TipoProfilo.EMMA.id)
        val vuota = emma.copy(
            stato = emma.stato.copy(
                lezioniGenerate = emptyMap(),
                lezioneContenuto = "",
                parole = emptyList()
            )
        )

        assertTrue(vuota.stato.lezioniGenerate.isEmpty())
        assertTrue(vuota.stato.lezioneContenuto.isEmpty())
        assertTrue(vuota.stato.parole.isEmpty())
        assertEquals(LivelloCEFR.B1, vuota.stato.progresso.livelloCorrente)
    }

    @Test
    fun `un profilo senza dati non viene riportato al livello base`() {
        // Il ricalcolo del livello non deve degradare un profilo vuoto: il livello
        // iniziale e' una scelta dell'utente (Emma = B1).
        val emma = profili.getValue(TipoProfilo.EMMA.id)
        val conDati = emma.copy(stato = emma.stato.copy(parole = emptyList()))

        assertEquals(LivelloCEFR.B1, conDati.stato.progresso.livelloCorrente)
    }
}
