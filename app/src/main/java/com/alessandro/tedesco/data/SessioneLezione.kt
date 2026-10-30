package com.alessandro.tedesco.data

/**
 * Costruisce la sessione di esercizi di una lezione.
 *
 * Perché non si mostrano sempre tutti gli esercizi nell'ordine del file:
 * rifacendo la stessa lezione lo studente ritrovava le stesse domande, spesso
 * con la risposta corretta nella stessa posizione. Bastava ricordare "la B"
 * per arrivare all'80% senza aver capito la regola.
 *
 * Qui ogni tentativo riceve:
 * - un sottoinsieme diverso del banco (quando il banco è più grande della sessione);
 * - un ordine diverso delle domande;
 * - opzioni di scelta multipla rimescolate (la risposta giusta cambia posizione).
 *
 * Tutto è deterministico rispetto al [seme]: la stessa sessione non cambia
 * mentre lo studente risponde, ma cambia al tentativo successivo.
 *
 * Se una unità ha pochi esercizi si mostrano tutti: meglio una sessione corta
 * che inventare contenuti. Il banco minimo è garantito dai test.
 */
object SessioneLezione {

    /** Quanti esercizi mostrare per tentativo (se il banco è più grande). */
    const val ESERCIZI_PER_SESSIONE = 8

    /**
     * Esercizi della sessione, già mescolati.
     *
     * @param unita unità del percorso grammaticale
     * @param seme seme del tentativo (timestamp di apertura + numero di prove)
     * @param quanti dimensione massima della sessione
     */
    fun esercizi(
        unita: UnitaGrammatica,
        seme: Int,
        quanti: Int = ESERCIZI_PER_SESSIONE
    ): List<EsercizioUnita> {
        val banco = unita.esercizi
        if (banco.isEmpty()) return emptyList()
        val n = quanti.coerceIn(1, banco.size)
        // Serve la permutazione COMPLETA: il taglio a n avviene dopo aver
        // garantito la presenza di tutti i tipi (altrimenti un tipo potrebbe
        // restare fuori proprio perché finito oltre l'ottavo posto).
        val mescolati = RotoreEsercizi.sessione(
            sezione = "unita:${unita.id}",
            grandezza = banco.size,
            quanti = banco.size,
            seme = seme
        )
        val scelti = conTuttiITipi(banco, mescolati, n)
        return scelti.map { indice ->
            val esercizio = banco[indice]
            if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                val (opzioni, corretta) = RotoreEsercizi.mescolaOpzioni(
                    seme = seme * 31 + esercizio.id.hashCode(),
                    opzioni = esercizio.opzioni,
                    rispostaCorretta = esercizio.rispostaCorretta
                )
                esercizio.copy(opzioni = opzioni, rispostaCorretta = corretta)
            } else {
                esercizio
            }
        }
    }

    /**
     * Garantisce che la sessione contenga almeno un esercizio per ogni tipo
     * presente nel banco (scegliere, completare, trasformare, produrre): una
     * sessione di sole scelte multiple non allena la produzione.
     */
    private fun conTuttiITipi(
        banco: List<EsercizioUnita>,
        mescolati: List<Int>,
        n: Int
    ): List<Int> {
        val scelti = LinkedHashSet<Int>()
        TipoEsercizioGrammatica.entries.forEach { tipo ->
            if (scelti.size < n) {
                mescolati.firstOrNull { banco[it].tipo == tipo && it !in scelti }
                    ?.let { scelti += it }
            }
        }
        for (indice in mescolati) {
            if (scelti.size >= n) break
            scelti += indice
        }
        // Mantiene l'ordine mescolato, non l'ordine dei tipi.
        return mescolati.filter { it in scelti }
    }
}
