package com.alessandro.tedesco.data

import java.time.LocalDate
import kotlin.random.Random

/**
 * ROTORE ESERCIZI.
 *
 * Le sezioni con un banco fisso di esercizi (Sprechuebungen, Dialoghi,
 * Pronuncia) sono più utili se ogni giorno ripresentano i contenuti in un
 * ordine diverso: l'utente percepisce esercizi "nuovi" anche studiando la
 * stessa lista.
 *
 * Come funziona: partiamo dal giorno corrente: la rotazione è DETERMINISTICA
 * (stesso giorno = stesso ordine, niente sfarfallio) e cambia da un giorno
 * all'altro. Non salva NIENTE: dopo la storia recente delle perdite dati,
 * le nuove funzionalità non devono aggiungere stato persistito.
 */
object RotoreEsercizi {

    /** Giorno corrente (data acquisita SENZA ora: cambia a mezzanotte). */
    private fun giornoCorrente(): Long = LocalDate.now().toEpochDay()

    /**
     * Indice di partenza per un ciclo di esercizi uno-dopo-l'altro:
     * ogni giorno si riparte da un punto diverso della lista.
     */
    fun indiceIniziale(sezione: String, grandezza: Int): Int {
        if (grandezza <= 0) return 0
        val semina = (giornoCorrente() * 1009L + sezione.hashCode()).toInt()
        return (Random(semina).nextInt(grandezza) + grandezza) % grandezza
    }

    /**
     * Permutazione degli indici della lista per il giorno corrente: usata
     * dalle schermate a lista (Dialoghi, Pronuncia) per girare l'ordine.
     * Ritorna gli indici 0..grandezza-1 mescolati in modo deterministico.
     */
    fun ordine(sezione: String, grandezza: Int): List<Int> {
        if (grandezza <= 0) return emptyList()
        val semina = (giornoCorrente() * 1009L + sezione.hashCode()).toInt()
        val rnd = Random(semina)
        val indici = (0 until grandezza).toMutableList()
        // Fisher-Yates: mescola senza presentare duplicati né buchi.
        for (i in indici.size - 1 downTo 1) {
            val j = rnd.nextInt(i + 1)
            val t = indici[i]; indici[i] = indici[j]; indici[j] = t
        }
        return indici
    }
}
