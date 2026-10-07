package com.alessandro.tedesco.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.TipoProfilo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val PROFILI_DATASTORE = "profili_data"
private val Context.profiliDataStore by preferencesDataStore(name = PROFILI_DATASTORE)

private object ProfiliKeys {
    val PROFILI_JSON = stringPreferencesKey("profili_json")
}

/**
 * Helper usati dai test d'integrazione per pilotare direttamente il DataStore
 * dei profili (che è privato altrimenti).
 */
internal suspend fun Context.scriviProfiliJson(json: String) {
    profiliDataStore.edit { it[ProfiliKeys.PROFILI_JSON] = json }
}

internal suspend fun Context.profiliDataStorePulito() {
    profiliDataStore.edit { it.clear() }
}

/**
 * Gestisce i profili utente.
 *
 * Alla prima apertura NON viene attivato alcun profilo: l'app mostra la
 * schermata di selezione e si entra nel corso solo dopo aver scelto.
 *
 * L'inizializzazione è serializzata da un [Mutex]: lettura da DataStore e
 * creazione dei preset avvengono in un'unica sequenza. Prima due coroutine
 * concorrenti (una che legge, una che crea i preset) si sovrascrivevano a
 * vicenda lasciando la lista profili vuota — la schermata appariva ma non
 * si poteva selezionare nulla.
 */
class ProfileManager(
    private val context: Context,
    private val json: Json,
    private val io: CoroutineDispatcher
) {
    private val _repository = MutableStateFlow(ProfiliRepository())
    val repositoryFlow: Flow<ProfiliRepository> = _repository

    /** true quando i profili sono stati caricati (o creati) e sono utilizzabili */
    private val _pronto = MutableStateFlow(false)
    val prontoFlow: Flow<Boolean> = _pronto

    private val mutex = Mutex()
    private var inizializzato = false

    /**
     * Carica i profili da DataStore e riconcilia con i preset previsti.
     * Idempotente e sicuro se chiamata più volte o in concorrenza.
     */
    suspend fun inizializza(forza: Boolean = false) {
        mutex.withLock {
            if (inizializzato && !forza) return

            val letto = leggiDaDataStore()
            val repo = ProfiliPreset.riconcilia(letto)
            val riparato = repo != letto

            _repository.value = repo
            if (riparato) scriviSuDataStore(repo)

            inizializzato = true
            _pronto.value = true
        }
    }

    /** Ricarica i profili forzando la rilettura (pulsante "Riprova"). */
    suspend fun ricarica() {
        inizializza(forza = true)
    }

    private suspend fun leggiDaDataStore(): ProfiliRepository = withContext(io) {
        val data = context.profiliDataStore.data.first()
        val jsonStr = data[ProfiliKeys.PROFILI_JSON]
        if (jsonStr.isNullOrBlank()) {
            ProfiliRepository()
        } else {
            runCatching {
                json.decodeFromString(ProfiliRepository.serializer(), jsonStr)
            }.getOrElse { ProfiliRepository() }
        }
    }

    private suspend fun scriviSuDataStore(repo: ProfiliRepository) = withContext(io) {
        val jsonStr = json.encodeToString(ProfiliRepository.serializer(), repo)
        context.profiliDataStore.edit { it[ProfiliKeys.PROFILI_JSON] = jsonStr }
    }

    private suspend fun saveToDataStore() = scriviSuDataStore(_repository.value)

    /** Tutti i profili disponibili, nell'ordine dei preset. */
    fun profiliDisponibili(): List<ProfiloUtente> {
        val ordine = ProfiliPreset.ID_PRESET
        return _repository.value.profili.values.sortedBy { p ->
            ordine.indexOf(p.config.tipo.id).let { if (it < 0) Int.MAX_VALUE else it }
        }
    }

    fun profiloAttivoId(): String? = _repository.value.profiloAttivoId

    fun profiloAttivo(): ProfiloUtente? {
        val id = _repository.value.profiloAttivoId ?: return null
        return _repository.value.profili[id]
    }

    /** Seleziona il profilo attivo. */
    suspend fun selezionaProfilo(profiloId: String) {
        val repo = _repository.value
        val esistente = repo.profili[profiloId]
            ?: throw IllegalArgumentException("Profilo non trovato: $profiloId")
        val aggiornato = esistente.copy(ultimoAccesso = System.currentTimeMillis())
        _repository.value = repo.copy(
            profili = repo.profili + (profiloId to aggiornato),
            profiloAttivoId = profiloId
        )
        saveToDataStore()
    }

    /** Torna alla schermata di selezione profilo. */
    suspend fun esciDalProfilo() {
        _repository.value = _repository.value.copy(profiloAttivoId = null)
        saveToDataStore()
    }

    /** Aggiorna lo stato del profilo attivo (parole, reviews, guida, progresso). */
    suspend fun aggiornaStatoAttivo(stato: ProfiloStato) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(stato = stato, ultimoAccesso = System.currentTimeMillis())
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    fun statoAttivo(): ProfiloStato = profiloAttivo()?.stato ?: ProfiloStato()

    // ---- Scorciatoie sul profilo attivo ----

    fun isCustomWordsEnabled(): Boolean = profiloAttivo()?.config?.enableCustomWords == true

    fun isGoogleSheetsEnabled(): Boolean = profiloAttivo()?.config?.enableGoogleSheets == true

    fun getGoogleSheetId(): String? = profiloAttivo()?.config?.googleSheetId

    fun getGuidaDocId(): String? = profiloAttivo()?.config?.guidaDocId

    fun getFeedUrl(): String = profiloAttivo()?.config?.feedUrl ?: ProfiliPreset.FEED_URL

    fun nomeProfiloAttivo(): String = profiloAttivo()?.config?.nomeVisualizzato ?: ""

    /** Ordine dei preset, esposto per l'UI. */
    fun ordinePreset(): List<String> = ProfiliPreset.ID_PRESET

    /** Aggiorna il feed URL del profilo attivo. */
    suspend fun aggiornaFeedUrl(nuovoUrl: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(config = profilo.config.copy(feedUrl = nuovoUrl))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /** Aggiorna l'ID del documento Google della guida per il profilo attivo. */
    suspend fun aggiornaGuidaDocId(nuovoDocId: String?) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(config = profilo.config.copy(guidaDocId = nuovoDocId))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }

    /**
     * Canale ntfy su cui pubblicare le parole nuove.
     * Solo per i profili personalizzati (Emma e Alessandro Personalizzato):
     * gli altri non hanno un vocabolario su GitHub da aggiornare.
     */
    fun topicPubblicazione(): String =
        if (isCustomWordsEnabled()) ProfiliPreset.NTFY_TOPIC_EMMA else ""

    /** Aggiorna la chiave API del tutor AI per il profilo attivo. */
    suspend fun aggiornaTutorApiKey(nuovaChiave: String) {
        val repo = _repository.value
        val id = repo.profiloAttivoId ?: return
        val profilo = repo.profili[id] ?: return
        val aggiornato = profilo.copy(config = profilo.config.copy(tutorApiKey = nuovaChiave.trim()))
        _repository.value = repo.copy(profili = repo.profili + (id to aggiornato))
        saveToDataStore()
    }
}
