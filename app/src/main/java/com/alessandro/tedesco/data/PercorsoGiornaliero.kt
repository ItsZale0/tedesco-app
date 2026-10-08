package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.TestLivello
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Motore del percorso personalizzato del giorno.
 *
 * A differenza di CalcoloPercorsoAdattivo (che genera raccomandazioni piatte),
 * questo motore crea un percorso strutturato e sequenziale di passi che l'utente
 * può seguire durante la giornata. Ogni passo è concreto, ha una stima di tempo
 * e può essere completato.
 *
 * Il percorso si adatta dinamicamente:
 * - Cambia in base ai punteggi di competenza
 * - Considera le parole in scadenza
 * - Si adatta al livello CEFR dell'utente
 * - Aggiorna il progresso man mano che i passi vengono completati
 */
object CalcoloPercorsoGiornaliero {

    enum class TipoPasso {
        RIPASSO,
        GRAMMATICA,
        COMPRENSIONE,
        PRODUZIONE,
        VOCABOLARIO,
        TEST_LIVELLO,
        SESSIONE_GUIDATA
    }

    data class PassoPercorso(
        val id: String,
        val titolo: String,
        val descrizione: String,
        val tipo: TipoPasso,
        val priorita: Int,
        val stimaMinuti: Int,
        val azione: String,
        val urgente: Boolean = false,
        val completato: Boolean = false
    )

    data class PercorsoGiornaliero(
        val data: String,
        val passi: List<PassoPercorso>,
        val messaggioMotivazionale: String,
        val livelloAttuale: LivelloCEFR,
        val prossimoObiettivo: String,
        val minutiTotali: Int,
        val minutiCompletati: Int
    ) {
        val progressoGiornata: Float
            get() = if (minutiTotali == 0) 0f else minutiCompletati.toFloat() / minutiTotali

        val passiCompletati: Int
            get() = passi.count { it.completato }

        val passiTotali: Int
            get() = passi.size

        val percorsoCompletato: Boolean
            get() = passi.isNotEmpty() && passi.all { it.completato }

        val prossimoPasso: PassoPercorso?
            get() = passi.firstOrNull { !it.completato }
    }

    /**
     * Genera il percorso personalizzato del giorno.
     *
     * @param statistiche statistiche complete dell'app
     * @param competenze punteggi delle 4 competenze
     * @param daRipassare numero di parole in scadenza
     * @param livelloCorrente livello CEFR dell'utente
     * @param obiettivoLivello livello obiettivo (default B1)
     * @param ultimoTest ultimo test di livello effettuato (null se mai fatto)
     * @param passiCompletati oggi IDs dei passi già completati oggi
     */
    fun genera(
        statistiche: Statistiche,
        competenze: CalcoloCompetenze.PunteggiCompetenze,
        daRipassare: Int,
        livelloCorrente: LivelloCEFR,
        obiettivoLivello: LivelloCEFR = LivelloCEFR.B1,
        ultimoTest: TestLivello? = null,
        passiCompletatiOggi: Set<String> = emptySet()
    ): PercorsoGiornaliero {
        val data = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(Date())
        val passi = mutableListOf<PassoPercorso>()
        var prioritaCounter = 1

        // 1. RIPASSO: sempre il primo passo se ci sono parole in scadenza
        if (daRipassare > 0) {
            val minuti = (daRipassare * 0.5f).toInt().coerceIn(5, 30)
            passi.add(
                PassoPercorso(
                    id = "ripasso",
                    titolo = "Ripasso flashcard",
                    descrizione = "$daRipassare parole in scadenza · ~$minuti min",
                    tipo = TipoPasso.RIPASSO,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "ripasso",
                    urgente = daRipassare >= 10,
                    completato = "ripasso" in passiCompletatiOggi
                )
            )
        }

        // 2. GRAMMATICA: se sotto soglia, passo prioritario
        if (competenze.grammatica < 60f) {
            val minuti = when {
                competenze.grammatica < 30f -> 20
                competenze.grammatica < 50f -> 15
                else -> 10
            }
            passi.add(
                PassoPercorso(
                    id = "grammatica",
                    titolo = "Esercizi di grammatica",
                    descrizione = "Grammatica al ${competenze.grammatica.toInt()}% · ~$minuti min",
                    tipo = TipoPasso.GRAMMATICA,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "grammatica",
                    urgente = competenze.grammatica < 40f,
                    completato = "grammatica" in passiCompletatiOggi
                )
            )
        }

        // 3. COMPRENSIONE: se sotto soglia
        if (competenze.comprensione < 60f) {
            val minuti = when {
                competenze.comprensione < 30f -> 20
                competenze.comprensione < 50f -> 15
                else -> 10
            }
            passi.add(
                PassoPercorso(
                    id = "comprensione",
                    titolo = "Quiz di comprensione",
                    descrizione = "Comprensione al ${competenze.comprensione.toInt()}% · ~$minuti min",
                    tipo = TipoPasso.COMPRENSIONE,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "quiz-comprensione",
                    urgente = competenze.comprensione < 40f,
                    completato = "comprensione" in passiCompletatiOggi
                )
            )
        }

        // 4. PRODUZIONE: se sotto soglia
        if (competenze.produzione < 60f) {
            val minuti = when {
                competenze.produzione < 30f -> 20
                competenze.produzione < 50f -> 15
                else -> 10
            }
            passi.add(
                PassoPercorso(
                    id = "produzione",
                    titolo = "Esercizi di produzione",
                    descrizione = "Produzione al ${competenze.produzione.toInt()}% · ~$minuti min",
                    tipo = TipoPasso.PRODUZIONE,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "quiz-produzione",
                    urgente = competenze.produzione < 40f,
                    completato = "produzione" in passiCompletatiOggi
                )
            )
        }

        // 5. VOCABOLARIO: se lessico basso
        if (competenze.lessico < 50f) {
            val minuti = when {
                competenze.lessico < 20f -> 15
                competenze.lessico < 35f -> 10
                else -> 8
            }
            passi.add(
                PassoPercorso(
                    id = "vocabolario",
                    titolo = "Nuove parole",
                    descrizione = "Lessico al ${competenze.lessico.toInt()}% · ~$minuti min",
                    tipo = TipoPasso.VOCABOLARIO,
                    priorita = prioritaCounter++,
                    stimaMinuti = minuti,
                    azione = "nuove",
                    completato = "vocabolario" in passiCompletatiOggi
                )
            )
        }

        // 6. TEST DI LIVELLO: se vicino all'obiettivo
        val progressoObiettivo = when (obiettivoLivello) {
            LivelloCEFR.B1 -> competenze.lessico / 100f * 0.5f +
                competenze.grammatica / 100f * 0.3f +
                competenze.comprensione / 100f * 0.2f
            else -> competenze.lessico / 100f * 0.4f +
                competenze.grammatica / 100f * 0.3f +
                competenze.comprensione / 100f * 0.15f +
                competenze.produzione / 100f * 0.15f
        }
        if (progressoObiettivo > 0.7f && livelloCorrente < obiettivoLivello) {
            passi.add(
                PassoPercorso(
                    id = "test-livello",
                    titolo = "Verifica il tuo livello",
                    descrizione = "Test di ${obiettivoLivello.label} · ~15 min",
                    tipo = TipoPasso.TEST_LIVELLO,
                    priorita = prioritaCounter++,
                    stimaMinuti = 15,
                    azione = "testb1",
                    completato = "test-livello" in passiCompletatiOggi
                )
            )
        }

        // 7. SESSIONE GUIDATA: se non c'è altro da fare o come approfondimento
        if (passi.isEmpty() || (daRipassare == 0 && statistiche.paroleTotali >= 500)) {
            passi.add(
                PassoPercorso(
                    id = "sessione",
                    titolo = "Sessione di approfondimento",
                    descrizione = "Ripasso completo e nuovi argomenti · ~20 min",
                    tipo = TipoPasso.SESSIONE_GUIDATA,
                    priorita = prioritaCounter++,
                    stimaMinuti = 20,
                    azione = "sessioni",
                    completato = "sessione" in passiCompletatiOggi
                )
            )
        }

        // Se ancora vuoto (caso estremo), crea un passo di sessione
        if (passi.isEmpty()) {
            passi.add(
                PassoPercorso(
                    id = "sessione",
                    titolo = "Sessione di studio",
                    descrizione = "Inizia il tuo percorso di studio · ~15 min",
                    tipo = TipoPasso.SESSIONE_GUIDATA,
                    priorita = 1,
                    stimaMinuti = 15,
                    azione = "sessioni",
                    completato = "sessione" in passiCompletatiOggi
                )
            )
        }

        val minutiTotali = passi.sumOf { it.stimaMinuti }
        val minutiCompletati = passi.filter { it.completato }.sumOf { it.stimaMinuti }

        val messaggio = when {
            passi.any { it.urgente } -> "Oggi conviene concentrarsi sulle aree con il badge Priorità."
            daRipassare >= 20 -> "Tante parole da ripassare. Inizia dal ripasso!"
            daRipassare >= 10 -> "Buon ritmo! Mantieni la costanza."
            statistiche.streakGiorni >= 7 -> "Streak di ${statistiche.streakGiorni} giorni! Sei inarrestabile."
            competenze.grammatica >= 60f && daRipassare == 0 -> "Prosegui così, stai facendo ottimi progressi."
            competenze.lessico < 30f -> "Sei all'inizio del viaggio. Ogni parola conta!"
            else -> "Buona sessione di studio!"
        }

        val prossimoObiettivo = when {
            livelloCorrente < LivelloCEFR.A1 -> "Raggiungere A1 con 50 parole"
            livelloCorrente < LivelloCEFR.A2 -> "Raggiungere A2 con 150 parole"
            livelloCorrente < LivelloCEFR.B1 -> "Raggiungere B1 con 900 parole"
            livelloCorrente < LivelloCEFR.B2 -> "Raggiungere B2 con 1800 parole"
            else -> "Raggiungere C1 con 3500 parole"
        }

        return PercorsoGiornaliero(
            data = data,
            passi = passi,
            messaggioMotivazionale = messaggio,
            livelloAttuale = livelloCorrente,
            prossimoObiettivo = prossimoObiettivo,
            minutiTotali = minutiTotali,
            minutiCompletati = minutiCompletati
        )
    }

    /**
     * Marca un passo come completato e restituisce il percorso aggiornato.
     */
    fun completaPasso(percorso: PercorsoGiornaliero, passoId: String): PercorsoGiornaliero {
        val nuoviPassi = percorso.passi.map { passo ->
            if (passo.id == passoId) passo.copy(completato = true) else passo
        }
        val minutiCompletati = nuoviPassi.filter { it.completato }.sumOf { it.stimaMinuti }
        return percorso.copy(
            passi = nuoviPassi,
            minutiCompletati = minutiCompletati
        )
    }

    /**
     * Resetta il percorso per un nuovo giorno.
     */
    fun resetGiorno(percorso: PercorsoGiornaliero): PercorsoGiornaliero {
        val nuoviPassi = percorso.passi.map { it.copy(completato = false) }
        return percorso.copy(
            passi = nuoviPassi,
            minutiCompletati = 0
        )
    }
}
