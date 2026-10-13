package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ReviewEntity
import com.alessandro.tedesco.data.local.RipassoLogEntity
import com.alessandro.tedesco.data.local.WordEntity
import kotlin.math.abs

/**
 * VERIFICA OGGETTIVA del lessico.
 *
 * Il ripasso classico e' self-report ("Lo so" / "Non lo so"): l'utente puo' dire
 * "lo so" senza saperlo, e `repetitions >= 4` misura quante volte ha VISTO la
 * parola, non se la SA. Qui si verifica in modo oggettivo: si mostra la parola
 * tedesca e si sceglie il significato italiano corretto tra 3-4 distrattori.
 * Solo una risposta corretta al primo colpo conta come "verificata".
 *
 * Da questa evidenza nascono:
 * - `paroleVerificate`: quante parole l'utente ha DIMOSTRATO di sapere
 * - il punteggio LESSICO del progresso, che non e' piu' un contatore di ripassi
 * - la lista di parole "fragili" da riproporre nel quiz
 */
object VerificaLessico {

    /** Distrattori da mostrare oltre alla risposta corretta. */
    const val DISTRATTORI = 3

    /** Una domanda del quiz di verifica. */
    data class Domanda(
        val parola: WordEntity,
        val opzioni: List<String>,   // significati italiani, la corretta e' inclusa
        val corretta: String
    )

    /** Esito di una sessione di verifica. */
    data class Esito(
        val totale: Int,
        val corrette: Int,
        val paroleVerificate: List<String>,  // id delle parole sapute al primo colpo
        val paroleDaRivedere: List<String>   // id delle parole sbagliate
    ) {
        val punteggio: Float get() = if (totale == 0) 0f else corrette.toFloat() / totale * 100f
    }

    /**
     * Costruisce le domande mescolando i significati di altre parole come distrattori.
     * Le parole con lo stesso significato italiano non vengono usate come distrattori
     * (eviterebbe domande ambigue con due risposte giuste).
     */
    fun costruisciDomande(
        parole: List<WordEntity>,
        distrattori: Int = DISTRATTORI,
        casuale: kotlin.random.Random = kotlin.random.Random.Default
    ): List<Domanda> {
        if (parole.size < 2) return emptyList()
        return parole.map { p ->
            val candidati = parole
                .filter { it.id != p.id && it.italian.isNotBlank() }
                .filter { it.italian.trim().lowercase() != p.italian.trim().lowercase() }
                .map { it.italian.trim() }
                .distinct()
                .shuffled(casuale)
                .take(distrattori)
            val opzioni = (candidati + p.italian.trim()).distinct().shuffled(casuale)
            Domanda(parola = p, opzioni = opzioni, corretta = p.italian.trim())
        }
    }

    /**
     * Punteggio lessico (0-100): percentuale di parole VERIFICATE sulle parole
     * disponibili. Non usa `repetitions`: usa solo evidenza di risposte corrette
     * registrate nel log (esito del quiz di verifica).
     *
     * @param paroleDisponibili parole attive (non archiviate)
     * @param verifiche mappa wordId -> true se verificata (saputa al primo colpo)
     */
    fun punteggioLessico(paroleDisponibili: Int, verifiche: Map<String, Boolean>): Float {
        if (paroleDisponibili == 0) return 0f
        val verificate = verifiche.count { it.value }
        return (verificate.toFloat() / paroleDisponibili * 100f).coerceIn(0f, 100f)
    }

    /**
     * Ricostruisce quali parole risultano verificate dal log dei ripassi.
     * Una parola e' "verificata" se l'ultima risposta registrata e' corretta
     * E il totale di risposte corrette e' >= 2 (evita il colpo di fortuna).
     * Le parole sbagliate di recente restano non verificate.
     */
    fun verificheDaLog(log: List<RipassoLogEntity>): Map<String, Boolean> {
        val perParola = log.groupBy { it.wordId }
        return perParola.mapValues { (_, eventi) ->
            val ordinati = eventi.sortedBy { it.timestamp }
            val ultimo = ordinati.last()
            val corrette = ordinati.count { it.corretta }
            ultimo.corretta && corrette >= 2
        }
    }

    /**
     * Parole "fragili": sbagliate almeno una volta e non ancora verificate.
     * Sono quelle da riproporre per prime nel quiz.
     */
    fun paroleFragili(
        parole: List<WordEntity>,
        log: List<RipassoLogEntity>,
        verifiche: Map<String, Boolean>
    ): List<WordEntity> {
        val sbagliate = log.filter { !it.corretta }.map { it.wordId }.toSet()
        return parole.filter { w -> !w.archived && sbagliate.contains(w.id) && verifiche[w.id] != true }
    }

    /** Parole mai verificate (nuove o deboli), priorita' a quelle con piu' errori. */
    fun paroleDaVerificare(
        parole: List<WordEntity>,
        log: List<RipassoLogEntity>,
        verifiche: Map<String, Boolean>,
        limite: Int
    ): List<WordEntity> {
        val erroriPerParola = log.filter { !it.corretta }.groupingBy { it.wordId }.eachCount()
        return parole
            .filter { !it.archived && verifiche[it.id] != true }
            .sortedByDescending { erroriPerParola[it.id] ?: 0 }
            .take(limite)
    }
}
