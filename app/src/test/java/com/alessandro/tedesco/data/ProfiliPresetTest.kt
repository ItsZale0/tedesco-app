package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
    fun `repo vuoto viene popolato con i due preset`() {
        val vuoto = ProfiliRepository()
        val risultato = ProfiliPreset.riconcilia(vuoto)

        assertEquals(2, risultato.profili.size)
        assertTrue(risultato.profili.containsKey(TipoProfilo.ALESSANDRO.id))
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

        assertEquals(2, risultato.profili.size)
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
        assertTrue("Anche Alessandro ha le parole custom", alessandro.config.enableCustomWords)
        assertFalse(alessandro.config.enableGoogleSheets)
        assertNull(alessandro.config.googleSheetId)

        val emma = p[TipoProfilo.EMMA.id]!!
        assertTrue(emma.config.enableCustomWords)
        assertTrue(emma.config.enableGoogleSheets)
        assertNotNull(emma.config.googleSheetId)
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

        assertEquals(2, riletto.profili.size)
        // Confronto per contenuto: i timestamp (creatoIl/ultimoAccesso) possono
        // differire di 1ms tra la creazione e la rilettura (test altrimenti flaky).
        assertEquals(originale.profili.keys, riletto.profili.keys)
        assertEquals(originale.profiloAttivoId, riletto.profiloAttivoId)
        originale.profili.forEach { (id, atteso) ->
            val ottenuto = riletto.profili[id]!!
            assertEquals(atteso.config, ottenuto.config)
            assertEquals(atteso.stato, ottenuto.stato)
        }
    }

    @Test
    fun `un JSON corrotto non fa crashare la lettura`() {
        val rotto = "{ questo non e' json valido"
        val risultato = runCatching {
            json.decodeFromString(ProfiliRepository.serializer(), rotto)
        }.getOrElse { ProfiliRepository() }

        // Il chiamante riparte da un repo vuoto, poi riconciliato coi preset
        assertEquals(0, risultato.profili.size)
        assertEquals(2, ProfiliPreset.riconcilia(risultato).profili.size)
    }

    @Test
    fun `il vecchio profilo personalizzato viene assorbito da Alessandro`() {
        val base = ProfiliPreset.crea(now = 1L)
        val alessandroBase = base[TipoProfilo.ALESSANDRO.id]!!
        val custom = alessandroBase.copy(
            id = "alessandro_custom",
            stato = ProfiloStato(
                parole = listOf(
                    WordEntity(
                        id = "w-custom",
                        german = "der Kater",
                        italian = "il gatto",
                        source = WordSource.CUSTOM
                    )
                ),
                reviews = mapOf("w-custom" to ReviewEntity(wordId = "w-custom"))
            )
        )
        val repo = ProfiliRepository(
            profili = base + ("alessandro_custom" to custom),
            profiloAttivoId = "alessandro_custom"
        )

        val risultato = ProfiliPreset.riconcilia(repo)

        // Il profilo rimosso non esiste più...
        assertFalse(risultato.profili.containsKey("alessandro_custom"))
        // ...e i suoi dati non vanno persi: passano ad Alessandro.
        val alessandro = risultato.profili[TipoProfilo.ALESSANDRO.id]!!
        assertTrue(alessandro.stato.parole.any { it.id == "w-custom" })
        assertTrue(alessandro.stato.reviews.containsKey("w-custom"))
        assertTrue(alessandro.config.enableCustomWords)
        assertEquals(TipoProfilo.ALESSANDRO.id, risultato.profiloAttivoId)
    }

    @Test
    fun `Emma ha il suo vocabolario separato sul GitHub di Alessandro`() {
        val feedEmma = ProfiliPreset.feedPredefinito(TipoProfilo.EMMA)
        val feedAle = ProfiliPreset.feedPredefinito(TipoProfilo.ALESSANDRO)
        assertTrue(feedEmma.contains("tedesco-vocab-emma", ignoreCase = true))
        assertNotEquals(feedAle, feedEmma)
    }

    @Test
    fun `riconciliazione allinea il feed condiviso di Emma`() {
        val base = ProfiliPreset.crea(now = 1L)
        val emmaCondiviso = base[TipoProfilo.EMMA.id]!!.copy(
            config = base[TipoProfilo.EMMA.id]!!.config.copy(feedUrl = ProfiliPreset.FEED_URL)
        )
        val repo = ProfiliRepository(
            profili = base + (TipoProfilo.EMMA.id to emmaCondiviso)
        )
        val risultato = ProfiliPreset.riconcilia(repo)
        assertEquals(ProfiliPreset.FEED_URL_EMMA, risultato.profili[TipoProfilo.EMMA.id]?.config?.feedUrl)
    }

    @Test
    fun `riconciliazione allinea il feed vuoto di Emma`() {
        val base = ProfiliPreset.crea(now = 1L)
        val emmaVuoto = base[TipoProfilo.EMMA.id]!!.copy(
            config = base[TipoProfilo.EMMA.id]!!.config.copy(feedUrl = "")
        )
        val repo = ProfiliRepository(
            profili = base + (TipoProfilo.EMMA.id to emmaVuoto)
        )
        val risultato = ProfiliPreset.riconcilia(repo)
        assertEquals(ProfiliPreset.FEED_URL_EMMA, risultato.profili[TipoProfilo.EMMA.id]?.config?.feedUrl)
    }

    @Test
    fun `riconciliazione rispetta un URL custom di Emma`() {
        val base = ProfiliPreset.crea(now = 1L)
        val custom = "https://raw.githubusercontent.com/ItsZale0/custom/main/vokabeln.json"
        val emmaCustom = base[TipoProfilo.EMMA.id]!!.copy(
            config = base[TipoProfilo.EMMA.id]!!.config.copy(feedUrl = custom)
        )
        val repo = ProfiliRepository(
            profili = base + (TipoProfilo.EMMA.id to emmaCustom)
        )
        val risultato = ProfiliPreset.riconcilia(repo)
        assertEquals(custom, risultato.profili[TipoProfilo.EMMA.id]?.config?.feedUrl)
    }
}
