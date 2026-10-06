package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProgressoUtente
import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.local.WordSource
import com.alessandro.tedesco.data.CalcoloCompetenze
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Conteggio delle parole per una singola lezione. */
data class ConteggioLezione(
    val lezione: Int,
    val totale: Int,
    val mature: Int
)

/** Attività di ripasso in un giorno specifico. */
data class AttivitaGiorno(
    val etichetta: String,
    val ripassi: Int
)

/** Statistiche complete dell'app, calcolate in modo puro da CalcoloStatistiche. */
data class Statistiche(
    val paroleTotali: Int,
    val paroleSynced: Int,
    val paroleCustom: Int,
    val daRipassare: Int,
    val nuove: Int,
    val inApprendimento: Int,
    val mature: Int,
    val lezioniTotali: Int,
    val streakGiorni: Int,
    val accuratezza: Float,
    val ripassiTotali: Int,
    val erroriTotali: Int,
    val paroleApprese: Int,
    val obiettivoB1: Int,
    val progressoB1: Float,
    val livelloStimato: LivelloCEFR,
    val perLezione: List<ConteggioLezione>,
    val ultimi7giorni: List<AttivitaGiorno>,
    val punteggiCompetenze: CalcoloCompetenze.PunteggiCompetenze?
)

/**
 * Motore di calcolo puro per le statistiche.
 * Nessun accesso a Android, nessuna coroutine, nessun I/O: girabile in un unit test JVM.
 */
object CalcoloStatistiche {

    const val OBIETTIVO_B1 = 2000

    private val FORMATTER_GIORNO = DateTimeFormatter.ofPattern("EEE", Locale.ITALIAN)

    /**
     * Calcola tutte le statistiche a partire dai dati grezzi.
     *
     * @param parole lista completa delle parole (incluse archiviate, qui filtrate)
     * @param reviews mappa wordId -> ReviewEntity
     * @param progresso progresso utente (per livelloCorrente e streakGiorni)
     * @param now timestamp corrente in millisecondi
     */
    fun calcola(
        parole: List<WordEntity>,
        reviews: Map<String, ReviewEntity>,
        progresso: ProgressoUtente,
        now: Long
    ): Statistiche {
        // Filtra le parole archiviate: non devono comparire in nessun conteggio
        val paroleAttive = parole.filter { !it.archived }

        val paroleTotali = paroleAttive.size
        val paroleSynced = paroleAttive.count { it.source == WordSource.SYNCED }
        val paroleCustom = paroleAttive.count { it.source == WordSource.CUSTOM }

        // Mappa wordId -> ReviewEntity per le parole attive
        val reviewAttive = paroleAttive.mapNotNull { w ->
            reviews[w.id]?.let { w.id to it }
        }.toMap()

        // Parole da ripassare: review con dueAt <= now
        val daRipassare = reviewAttive.values.count { it.dueAt <= now }

        // Classificazione per stadio di apprendimento
        // Una parola senza review conta come "nuove"
        val nuove = paroleAttive.count { w ->
            val r = reviews[w.id]
            r == null || r.repetitions == 0
        }
        val inApprendimento = reviewAttive.values.count { it.repetitions in 1..3 }
        val mature = reviewAttive.values.count { it.repetitions >= 4 }

        // Lezioni distinte tra le parole attive
        val lezioniDistinte = paroleAttive.map { it.lesson }.distinct().sorted()
        val lezioniTotali = lezioniDistinte.size

        // Ripassi ed errori totali
        val ripassiTotali = reviewAttive.values.sumOf { it.repetitions }
        val erroriTotali = reviewAttive.values.sumOf { it.lapses }

        // Accuratezza: ripassi riusciti / (ripassi + errori)
        val accuratezza = if (ripassiTotali + erroriTotali == 0) {
            0f
        } else {
            ripassiTotali.toFloat() / (ripassiTotali + erroriTotali).toFloat()
        }

        // Parole apprese = mature (repetitions >= 4)
        val paroleApprese = mature

        // Progresso verso B1 (2000 parole)
        val progressoB1 = (paroleApprese.toFloat() / OBIETTIVO_B1.toFloat()).coerceAtMost(1f)

        // Livello stimato da paroleApprese, senza scendere sotto il livello corrente
        val livelloCalcolato = livelloDaParole(paroleApprese)
        val livelloStimato = if (progresso.livelloCorrente.ordine > livelloCalcolato.ordine) {
            progresso.livelloCorrente
        } else {
            livelloCalcolato
        }

        // Conteggio per lezione
        val perLezione = lezioniDistinte.map { lezione ->
            val paroleLezione = paroleAttive.filter { it.lesson == lezione }
            ConteggioLezione(
                lezione = lezione,
                totale = paroleLezione.size,
                mature = paroleLezione.count { w ->
                    val r = reviews[w.id]
                    r != null && r.repetitions >= 4
                }
            )
        }

        // Attività degli ultimi 7 giorni (dal più vecchio a oggi)
        val oggi = LocalDate.ofEpochDay(now / (1000 * 60 * 60 * 24))
        val ultimi7giorni = (6 downTo 0).map { offset ->
            val giorno = oggi.minusDays(offset.toLong())
            val etichetta = giorno.format(FORMATTER_GIORNO)
            val ripassi = reviewAttive.values.count { r ->
                r.lastReviewedAt != null && stessoGiorno(r.lastReviewedAt, giorno)
            }
            AttivitaGiorno(etichetta = etichetta, ripassi = ripassi)
        }

        // Calcolo delle 4 competenze
        val punteggiCompetenze = CalcoloCompetenze.calcola(
            parole = parole,
            reviews = reviews,
            progresso = progresso,
            now = now
        )

        return Statistiche(
            paroleTotali = paroleTotali,
            paroleSynced = paroleSynced,
            paroleCustom = paroleCustom,
            daRipassare = daRipassare,
            nuove = nuove,
            inApprendimento = inApprendimento,
            mature = mature,
            lezioniTotali = lezioniTotali,
            streakGiorni = progresso.streakGiorni,
            accuratezza = accuratezza,
            ripassiTotali = ripassiTotali,
            erroriTotali = erroriTotali,
            paroleApprese = paroleApprese,
            obiettivoB1 = OBIETTIVO_B1,
            progressoB1 = progressoB1,
            livelloStimato = livelloStimato,
            perLezione = perLezione,
            ultimi7giorni = ultimi7giorni,
            punteggiCompetenze = punteggiCompetenze
        )
    }

    /**
     * Determina il livello CEFR stimato in base al numero di parole apprese.
     * Soglie: <50 A0, <150 A1, <400 A2, <900 B1, <1800 B2, <3500 C1, >=3500 C2.
     */
    private fun livelloDaParole(paroleApprese: Int): LivelloCEFR {
        return when {
            paroleApprese < 50 -> LivelloCEFR.A0
            paroleApprese < 150 -> LivelloCEFR.A1
            paroleApprese < 400 -> LivelloCEFR.A2
            paroleApprese < 900 -> LivelloCEFR.B1
            paroleApprese < 1800 -> LivelloCEFR.B2
            paroleApprese < 3500 -> LivelloCEFR.C1
            else -> LivelloCEFR.C2
        }
    }

    /**
     * Verifica se un timestamp (millisecondi) cade nello stesso giorno di calendario
     * della LocalDate fornita, usando il timezone di sistema.
     */
    private fun stessoGiorno(timestamp: Long, giorno: LocalDate): Boolean {
        val data = LocalDate.ofEpochDay(timestamp / (1000 * 60 * 60 * 24))
        return data == giorno
    }
}
