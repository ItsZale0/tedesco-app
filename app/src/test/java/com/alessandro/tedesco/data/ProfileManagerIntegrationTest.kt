package com.alessandro.tedesco.data

import androidx.test.core.app.ApplicationProvider
import com.alessandro.tedesco.data.local.TipoProfilo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test d'integrazione del [ProfileManager] con un Context Android reale
 * (Robolectric) e il vero DataStore su disco temporaneo.
 *
 * Riproduce il bug segnalato: "esce la selezione profilo ma non posso
 * selezionare nulla" — cioè la lista profili restava vuota.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProfileManagerIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun nuovoManager(): ProfileManager = ProfileManager(
        context = ApplicationProvider.getApplicationContext(),
        json = json,
        io = Dispatchers.IO
    )

    @Before
    fun pulisci() = runBlocking {
        // Ogni test parte da uno stato pulito
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .profiliDataStorePulito()
    }

    @Test
    fun `dopo inizializza la lista profili non e' vuota`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        val profili = manager.profiliDisponibili()
        assertEquals("La selezione deve mostrare 3 profili", 3, profili.size)
    }

    @Test
    fun `nessun profilo e' attivo dopo l'inizializzazione`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        // Requisito: si sceglie il profilo PRIMA di entrare
        assertNull(manager.profiloAttivoId())
        assertNull(manager.profiloAttivo())
    }

    @Test
    fun `il flow pronto diventa true e i profili sono osservabili`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        assertTrue(manager.prontoFlow.firstValue())
        assertEquals(3, manager.repositoryFlow.firstValue().profili.size)
    }

    @Test
    fun `inizializzazioni concorrenti non svuotano la lista`() = runBlocking {
        // Il bug originale: lettura e creazione preset in parallelo
        val manager = nuovoManager()
        val esiti = (1..8).map { async(Dispatchers.IO) { manager.inizializza() } }.awaitAll()

        assertEquals(8, esiti.size)
        assertEquals(3, manager.profiliDisponibili().size)
    }

    @Test
    fun `selezionare un profilo lo rende attivo e persistente`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        manager.selezionaProfilo(TipoProfilo.EMMA.id)
        assertEquals(TipoProfilo.EMMA.id, manager.profiloAttivoId())
        assertEquals("Emma", manager.nomeProfiloAttivo())
        assertTrue(manager.isCustomWordsEnabled())

        // Un nuovo manager sullo stesso DataStore deve ritrovare la scelta
        val manager2 = nuovoManager()
        manager2.inizializza()
        assertEquals(TipoProfilo.EMMA.id, manager2.profiloAttivoId())
    }

    @Test
    fun `uscire dal profilo riporta alla selezione senza perdere i dati`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()
        manager.selezionaProfilo(TipoProfilo.ALESSANDRO_CUSTOM.id)
        manager.aggiornaStatoAttivo(
            manager.statoAttivo().copy(etag = "MIO-ETAG")
        )

        manager.esciDalProfilo()
        assertNull(manager.profiloAttivoId())
        // I profili restano disponibili per una nuova scelta
        assertEquals(3, manager.profiliDisponibili().size)

        // Rientrando, i dati sono ancora li'
        manager.selezionaProfilo(TipoProfilo.ALESSANDRO_CUSTOM.id)
        assertEquals("MIO-ETAG", manager.statoAttivo().etag)
    }

    @Test
    fun `il profilo standard non abilita le parole personalizzate`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()
        manager.selezionaProfilo(TipoProfilo.ALESSANDRO.id)

        assertFalse(manager.isCustomWordsEnabled())
        assertFalse(manager.isGoogleSheetsEnabled())
        assertNotNull(manager.getFeedUrl())
    }

    @Test
    fun `i dati di un profilo non finiscono in un altro`() = runBlocking {
        val manager = nuovoManager()
        manager.inizializza()

        manager.selezionaProfilo(TipoProfilo.ALESSANDRO.id)
        manager.aggiornaStatoAttivo(manager.statoAttivo().copy(etag = "SOLO-ALESSANDRO"))

        manager.selezionaProfilo(TipoProfilo.EMMA.id)
        assertTrue(
            "Emma non deve vedere i dati di Alessandro",
            manager.statoAttivo().etag.isEmpty()
        )
    }

    @Test
    fun `un DataStore con repo vuoto viene riparato con i preset`() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Simula lo stato lasciato dalla versione buggata: repo senza profili
        ctx.scriviProfiliJson("""{"profili":{},"profiloAttivoId":null}""")

        val manager = nuovoManager()
        manager.inizializza()

        assertEquals(3, manager.profiliDisponibili().size)
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstValue(): T = first()
