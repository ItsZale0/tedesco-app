package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProfiloConfig
import com.alessandro.tedesco.data.local.ProfiliRepository
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ProfiloStato
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.TipoProfilo

/**
 * Logica pura per la gestione dei preset dei profili.
 *
 * Estratta da [ProfileManager] per poterla testare senza Android/DataStore.
 * Il bug storico era: la lista profili restava vuota perché due coroutine
 * concorrenti (lettura da DataStore e creazione preset) si sovrascrivevano.
 */
object ProfiliPreset {

    const val FEED_URL =
        "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab/main/vokabeln.json"
    /** Vocabolario personale di Emma: ospitato sul GitHub di Alessandro. */
    const val FEED_URL_EMMA =
        "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab-emma/main/vokabeln.json"
    const val GUIDA_DOC_ID = "12yKY4Bpp6IqX7q8tgNYkFXIoAQsZR8yVd4mZhcD5I7g"
    const val SHEET_CUSTOM = "1OZ0BIbOC1wRVwWjJOLpgiBLGf6OzkRzJL7n9-5WbfD8"

    /** Canale ntfy su cui l'app pubblica le parole nuove di Emma. */
    const val NTFY_TOPIC_EMMA = "tedesco-emma-vocab-c7f3a91b"

    /**
     * Chiave Groq di default per il tutor AI.
     * Sostituire con una chiave reale prima di pubblicare l'app.
     * L'utente può sovrascriverla in Profilo → Tutor AI.
     */
    const val DEFAULT_TUTOR_API_KEY = "sk-or-v1-REPLACE_WITH_REAL_KEY"

    /** Id di tutti i preset previsti, in ordine. */
    val ID_PRESET: List<String> = TipoProfilo.entries.map { it.id }

    /** Id dei preset assenti da [presenti]. */
    fun mancanti(presenti: Set<String>): Set<String> = ID_PRESET.toSet() - presenti

    /** Costruisce i profili preset. Nessuno viene attivato. */
    fun crea(now: Long = System.currentTimeMillis()): Map<String, ProfiloUtente> {
        fun preset(tipo: TipoProfilo, sheetId: String?, feedUrl: String = "", guidaDocId: String? = null): ProfiloUtente = ProfiloUtente(
            id = tipo.id,
            config = ProfiloConfig(
                tipo = tipo,
                nomeVisualizzato = tipo.nome,
                enableCustomWords = tipo.enableCustomWords,
                enableGoogleSheets = tipo.enableGoogleSheets,
                googleSheetId = sheetId,
                feedUrl = feedUrl,
                guidaDocId = guidaDocId,
                tutorApiKey = DEFAULT_TUTOR_API_KEY,
                livelloIniziale = tipo.livelloIniziale,
                mostraContestoMedico = tipo.mostraContestoMedico
            ),
            stato = ProfiloStato(
                progresso = ProgressoUtente(
                    livelloCorrente = tipo.livelloIniziale.toLivelloCEFR()
                )
            ),
            creatoIl = now,
            ultimoAccesso = now
        )

        return mapOf(
            TipoProfilo.ALESSANDRO.id to preset(TipoProfilo.ALESSANDRO, null, FEED_URL, GUIDA_DOC_ID),
            TipoProfilo.ALESSANDRO_CUSTOM.id to preset(TipoProfilo.ALESSANDRO_CUSTOM, SHEET_CUSTOM, FEED_URL, GUIDA_DOC_ID),
            // Emma: vocabolario suo, ospitato sul GitHub di Alessandro.
            // Parte vuota e si popola con le parole che aggiunge nell'app.
            TipoProfilo.EMMA.id to preset(TipoProfilo.EMMA, SHEET_CUSTOM, FEED_URL_EMMA, GUIDA_DOC_ID)
        )
    }

    /**
     * Riconcilia quanto letto da DataStore con i preset previsti:
     * i preset mancanti vengono aggiunti, quelli già presenti (con i loro
     * progressi) restano intatti. `profiloAttivoId` non viene mai forzato.
     */
    fun riconcilia(letto: ProfiliRepository): ProfiliRepository {
        val mancanti = mancanti(letto.profili.keys)
        if (mancanti.isEmpty()) return letto
        val aggiunte = crea().filterKeys { it in mancanti }
        return letto.copy(profili = letto.profili + aggiunte)
    }
}
