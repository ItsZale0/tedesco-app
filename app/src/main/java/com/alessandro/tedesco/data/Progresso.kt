package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity
import java.time.LocalDate

/**
 * UNICA fonte di verità per il progresso dell'utente.
 *
 * Tutti i numeri derivano da dati REALI (parole, ripassi registrati, test svolti):
 * nessun campo fantasma, nessun valore scritto a mano. Se un dato non esiste
 * (es. nessun test svolto) il valore è 0, mai inventato.
 *
 * Il livello è calcolato con i criteri UFFICIALI delle certificazioni
 * (Goethe/telc/ÖSD: soglia 60% su ogni dimensione, "maglia più debole").
 */
object CalcoloProgresso {

    /** Secondi stimati per singolo ripasso (lettura + risposta). */
    private const val SECONDI_PER_RIPASSO = 20

    /** Traguardo raggiunto (calcolato dai dati reali, non salvato). */
    data class Traguardo(
        val id: String,
        val nome: String,
        val icona: String,
        val raggiunto: Boolean
    )

    data class ProgressoReale(
        // --- Dove sono ---
        val livello: LivelloCEFR,
        val livelloObiettivo: LivelloCEFR,
        /** 0..1 — quanto manca al prossimo livello (media delle 4 competenze). */
        val versoProssimoLivello: Float,
        val prossimoLivello: LivelloCEFR?,

        // --- Cosa mi manca (dimensione più debole) ---
        val dimensioneDebole: String,
        val dimensioneDeboleValore: Float,
        val azioneConsigliata: String,

        // --- Cosa ho fatto (tutto REALE) ---
        val paroleImparate: Int,
        /** Parole sbagliate a una verifica e non ancora verificate (errori da riprendere). */
        val paroleFragili: Int,
        /** Parole mai verificate nel quiz (nuove da dimostrare). */
        val paroleDaVerificare: Int,
        val paroleDisponibili: Int,
        val ripassiTotali: Int,
        val accuratezza: Float,
        val streakGiorni: Int,
        val streakRecord: Int,
        val giorniStudioTotali: Int,
        val minutiStudiati: Int,

        // --- Dettaglio competenze ---
        val competenze: CalcoloCompetenze.PunteggiCompetenze,

        // --- Riferimenti ufficiali ---
        val wortlisteUfficiale: Int,
        val vocabolarioAttivoUfficiale: Int,
        val oreGuida: IntRange,

        // --- Traguardi ---
        val traguardi: List<Traguardo>
    ) {
        val traguardiRaggiunti: Int get() = traguardi.count { it.raggiunto }
        val paroleMancantiAlB1: Int get() = (vocabolarioAttivoUfficiale - paroleImparate).coerceAtLeast(0)
    }

    fun calcola(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        log: List<RipassoLogEntity>,
        obiettivo: LivelloCEFR,
        now: Long
    ): ProgressoReale {
        val competenze = CalcoloCompetenze.calcola(parole, reviews, progresso, now, log)
        val livello = competenze.livelloComplessivo
        val paroleAttive = parole.filter { !it.archived }
        // Parole VERIFICATE: dimostrate nel quiz oggettivo (non "viste N volte").
        // Solo quelle ancora nel vocabolario attivo.
        val verifiche = VerificaLessico.verificheDaLog(log)
        val paroleImparate = paroleAttive.count { verifiche[it.id] == true }

        // --- Streak REALE dal log (giorni consecutivi con almeno un ripasso) ---
        val giorni = log.map { LocalDate.ofEpochDay(it.timestamp / 86_400_000L) }.toSet()
        val oggi = LocalDate.ofEpochDay(now / 86_400_000L)
        var streak = 0
        var d = oggi
        // La streak conta se oggi o ieri c'e' attivita' (altrimenti e' gia' rotta)
        if (!giorni.contains(oggi) && !giorni.contains(oggi.minusDays(1))) {
            streak = 0
        } else {
            if (!giorni.contains(oggi)) d = oggi.minusDays(1)
            while (giorni.contains(d)) { streak++; d = d.minusDays(1) }
        }
        // Record = la sequenza piu' lunga di giorni consecutivi in tutto lo storico
        var record = 0
        var corrente = 0
        var prev: LocalDate? = null
        for (g in giorni.sorted()) {
            corrente = if (prev != null && g == prev!!.plusDays(1)) corrente + 1 else 1
            if (corrente > record) record = corrente
            prev = g
        }

        // --- Accuratezza REALE dal log ---
        val ripassiTotali = log.size
        val accuratezza = if (ripassiTotali == 0) 0f
            else log.count { it.corretta }.toFloat() / ripassiTotali.toFloat()

        // --- Minuti di studio stimati (realistici, dal numero di ripassi) ---
        val minutiStudiati = (ripassiTotali * SECONDI_PER_RIPASSO) / 60

        // --- Dimensione piu' debole: cosa fare adesso ---
        val dimensioni = listOf(
            "Lessico" to competenze.lessico,
            "Grammatica" to competenze.grammatica,
            "Comprensione" to competenze.comprensione,
            "Produzione" to competenze.produzione
        )
        val (nomeDebole, valoreDebole) = dimensioni.minBy { it.second }
        val azione = when (nomeDebole) {
            "Lessico" -> "Impara parole nuove"
            "Grammatica" -> "Fai esercizi di grammatica"
            "Comprensione" -> "Leggi un testo (Lettura)"
            "Produzione" -> "Scrivi una frase (Produzione)"
            else -> "Continua a esercitarti"
        }

        // --- Verso il prossimo livello (media delle competenze, criterio chiaro) ---
        val media = dimensioni.map { it.second }.average().toFloat()
        val prossimo = LivelloCEFR.entries.firstOrNull { it.ordine == livello.ordine + 1 }
            ?.takeIf { it.ordine <= obiettivo.ordine }

        // --- Traguardi (calcolati dai dati reali) ---
        val traguardi = listOf(
            Traguardo("p1", "Prima parola", "\uD83C\uDF31", paroleImparate >= 1),
            Traguardo("p10", "10 parole", "\uD83D\uDCDA", paroleImparate >= 10),
            Traguardo("p25", "25 parole", "\uD83D\uDCD8", paroleImparate >= 25),
            Traguardo("p50", "50 parole", "\uD83D\uDDA3\uFE0F", paroleImparate >= 50),
            Traguardo("p100", "100 parole", "\uD83C\uDF1F", paroleImparate >= 100),
            Traguardo("s7", "7 giorni di fila", "\uD83D\uDD25", record >= 7),
            Traguardo("s30", "30 giorni di fila", "\uD83C\uDFC3", record >= 30),
            Traguardo("r100", "100 ripassi", "\u2705", ripassiTotali >= 100),
            Traguardo("r500", "500 ripassi", "\uD83D\uDCAA", ripassiTotali >= 500),
            Traguardo("c60", "Una competenza al 60%", "\uD83C\uDFAF",
                dimensioni.any { it.second >= CertificazioniUfficiali.SOGLIA_SUPERAMENTO })
        )

        return ProgressoReale(
            livello = livello,
            livelloObiettivo = obiettivo,
            versoProssimoLivello = (media / 100f).coerceIn(0f, 1f),
            prossimoLivello = prossimo,
            dimensioneDebole = nomeDebole,
            dimensioneDeboleValore = valoreDebole,
            azioneConsigliata = azione,
            paroleImparate = paroleImparate,
            paroleFragili = VerificaLessico.paroleFragili(paroleAttive, log, verifiche).size,
            paroleDaVerificare = VerificaLessico.paroleDaVerificare(paroleAttive, log, verifiche, Int.MAX_VALUE).size,
            paroleDisponibili = paroleAttive.size,
            ripassiTotali = ripassiTotali,
            accuratezza = accuratezza,
            streakGiorni = streak,
            streakRecord = record,
            giorniStudioTotali = giorni.size,
            minutiStudiati = minutiStudiati,
            competenze = competenze,
            wortlisteUfficiale = CertificazioniUfficiali.wortlisteRicettiva(livello),
            vocabolarioAttivoUfficiale = CertificazioniUfficiali.vocabolarioAttivoRichiesto(obiettivo),
            oreGuida = CertificazioniUfficiali.oreGuidaCumulate(livello),
            traguardi = traguardi
        )
    }
}
