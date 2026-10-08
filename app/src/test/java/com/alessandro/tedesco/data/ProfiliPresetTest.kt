package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.TipoProfilo
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica il fix del bug "la selezione profilo non mostra nulla".
 *
 * Causa: due coroutine concorrenti (lettura DataStore + creazione preset)
 * si sovrascrivevano, lasciando la lista profili vuota.
 */
class ProfiliPresetTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `repo vuoto viene popolato con i tre preset`() {
        val vuoto = ProfiliRepository()
        val risultato = ProfiliPreset.riconcilia(vuoto)

        assertEquals(3, risultato.profili.size)
        assertTrue(risultato.profili.containsKey(TipoProfilo.ALESSANDRO.id))
        assertTrue(risultato.profili.containsKey(TipoProfilo.ALESSANDRO_CUSTOM.id))
        assertTrue(risultato.profili.containsKey(TipoProfilo.EMMA.id))
    }

    @Test
    fun `riconciliazione non attiva nessun profilo`() {
        val risultato = ProfiliPreset.riconcilia(ProfiliRepository())
        // Requisito utente: la selezione avviene prima di entrare
        assertNull(risultato.profiloAttivoId)
    }

    @Test
    fun `un solo preset mancante viene aggiunto senza toccare gli altri`() {
        val esistente = ProfiliPreset.crea(now = 1L)
        // Simula un DataStore parziale: manca Emma
        val parziale = esistente.filterKeys { it != TipoProfilo.EMMA.id }
        val repo = ProfiliRepository(profili = parziale, profiloAttivoId = TipoProfilo.ALESSANDRO.id)

        val risultato = ProfiliPreset.riconcilia(repo)

        assertEquals(3, risultato.profili.size)
        assertTrue(risultato.profili.containsKey(TipoProfilo.EMMA.id))
        // Il profilo attivo non viene azzerato dalla riparazione
        assertEquals(TipoProfilo.ALESSANDRO.id, risultato.profiloAttivoId)
    }

    @Test
    fun `i progressi di un profilo esistente sopravvivono alla riconciliazione`() {
        val crea = ProfiliPreset.crea(now = 1L)
        val conProgressi = crea.mapValues { (_, p) ->
            p.copy(stato = ProfiloStato(etag = "ETAG-123", lastSync = 999L))
        }
        val repo = ProfiliRepository(profili = conProgressi.filterKeys { it != TipoProfilo.EMMA.id })

        val risultato = ProfiliPreset.riconcilia(repo)

        assertEquals("ETAG-123", risultato.profili[TipoProfilo.ALESSANDRO.id]?.stato?.etag)
        assertEquals(999L, risultato.profili[TipoProfilo.ALESSANDRO.id]?.stato?.lastSync)
    }

    @Test
    fun `repo completo non viene modificato`() {
        val completo = ProfiliRepository(
            profili = ProfiliPreset.crea(now = 1L),
            profiloAttivoId = TipoProfilo.EMMA.id
        )
        val risultato = ProfiliPreset.riconcilia(completo)

        assertEquals(completo, risultato)
    }

    @Test
    fun `i preset hanno la configurazione corretta`() {
        val p = ProfiliPreset.crea()

        val alessandro = p[TipoProfilo.ALESSANDRO.id]!!
        assertFalse("Alessandro standard non ha parole custom", alessandro.config.enableCustomWords)
        assertFalse(alessandro.config.enableGoogleSheets)
        assertNull(alessandro.config.googleSheetId)

        val emma = p[TipoProfilo.EMMA.id]!!
        assertTrue(emma.config.enableCustomWords)
        assertTrue(emma.config.enableGoogleSheets)
        assertNotNull(emma.config.googleSheetId)

        val custom = p[TipoProfilo.ALESSANDRO_CUSTOM.id]!!
        assertTrue(custom.config.enableCustomWords)
        assertTrue(custom.config.enableGoogleSheets)
    }

    @Test
    fun `i preset hanno la chiave OpenRouter di default`() {
        val p = ProfiliPreset.crea()

        p.forEach { (_, profilo) ->
            assertTrue(
                "Il profilo ${profilo.config.nomeVisualizzato} deve avere la chiave API di default",
                profilo.config.tutorApiKey.isNotBlank()
            )
            assertEquals(
                "La chiave API di default deve essere quella di ProfiliPreset",
                ProfiliPreset.DEFAULT_TUTOR_API_KEY,
                profilo.config.tutorApiKey
            )
        }
    }

    @Test
    fun `la chiave OpenRouter di default non e' vuota`() {
        assertTrue(
            "DEFAULT_TUTOR_API_KEY non deve essere vuota",
            ProfiliPreset.DEFAULT_TUTOR_API_KEY.isNotBlank()
        )
        assertTrue(
            "DEFAULT_TUTOR_API_KEY deve iniziare con sk-or-v1-",
            ProfiliPreset.DEFAULT_TUTOR_API_KEY.startsWith("sk-or-v1-")
        )
    }

    @Test
    fun `il repository serializza e deserializza senza perdere profili`() {
        val originale = ProfiliPreset.riconcilia(ProfiliRepository())
        val stringa = json.encodeToString(ProfiliRepository.serializer(), originale)
        val riletto = json.decodeFromString(ProfiliRepository.serializer(), stringa)

        assertEquals(3, riletto.profili.size)
        assertEquals(originale, riletto)
    }

    @Test
    fun `un JSON corrotto non fa crashare la lettura`() {
        val rotto = "{ questo non e' json valido"
        val risultato = runCatching {
            json.decodeFromString(ProfiliRepository.serializer(), rotto)
        }.getOrElse { ProfiliRepository() }

        // Il chiamante riparte da un repo vuoto, poi riconciliato coi preset
        assertEquals(0, risultato.profili.size)
        assertEquals(3, ProfiliPreset.riconcilia(risultato).profili.size)
    }
}
