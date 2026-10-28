package com.alessandro.tedesco.data

import com.alessandro.tedesco.data.local.ProgressoLezione

/**
 * Le lezioni NON sono più una sequenza numerica scollegata dal percorso:
 * ogni lezione È una unità del percorso grammaticale A0 → B2.
 *
 * Questo file fa da ponte tra [GrammaticaSyllabus] e la UI:
 * - numera le unità in ordine didattico (1..N);
 * - calcola quali lezioni sono accessibili (prerequisiti completati oppure
 *   livello già raggiunto dal profilo);
 * - suggerisce la lezione giusta per il livello dello studente (Emma parte da
 *   B1, Alessandro da A0);
 * - genera la teoria offline in markdown, senza dipendere dall'AI.
 *
 * Tutto è puro: nessun I/O, nessuna rete. Il ViewModel si limita a salvare
 * l'id della lezione aperta e il progresso.
 */
data class LezionePercorso(
    /** Numero progressivo mostrato all'utente (1..N). */
    val numero: Int,
    val unita: UnitaGrammatica,
    val stato: StatoUnita,
    val padronanza: PadronanzaGrammatica?,
    /** True se lo studente può aprirla: prerequisiti fatti o livello già raggiunto. */
    val accessibile: Boolean
) {
    val id: String get() = unita.id
    val livello: String get() = unita.livello
    val titolo: String get() = unita.titolo
    val completata: Boolean get() = stato == StatoUnita.COMPLETATA || stato == StatoUnita.DA_RIPASSARE
}

/**
 * Lezione aperta in home: unità del percorso + punto in cui si era rimasti.
 * Un solo oggetto da osservare evita che la UI mostri la lezione prima del
 * progresso (o viceversa) e riparta sempre da zero.
 */
data class LezioneAperta(
    val lezione: LezionePercorso,
    val progresso: ProgressoLezione
) {
    val id: String get() = lezione.id
    val totale: Int get() = lezione.unita.esercizi.size
    /** Indice della domanda da mostrare, sempre dentro i limiti. */
    val indice: Int get() = progresso.indice.coerceIn(0, totale)
    val conclusa: Boolean get() = progresso.completata || indice >= totale
}

object PercorsoLezioni {

    /** Ordine CEFR usato per l'accesso libero fino al livello del profilo. */
    private val ORDINE_LIVELLI = listOf("A0", "A1", "A2", "B1", "B2")

    fun ordineLivello(livello: String?): Int {
        // Accetta sia "B1" sia "B1 - Intermedio": il label del profilo è lungo.
        val codice = livello
            ?.uppercase()
            ?.substringBefore(" - ")
            ?.substringBefore(" ")
            ?.trim()
            .orEmpty()
        return ORDINE_LIVELLI.indexOf(codice)
            .let { if (it < 0) ORDINE_LIVELLI.lastIndex else it }
    }

    /** Tutte le lezioni, con stato e accessibilità. */
    fun tutte(
        mappa: Map<String, PadronanzaGrammatica>,
        livelloProfilo: String? = null,
        now: Long = System.currentTimeMillis()
    ): List<LezionePercorso> {
        val soglia = ordineLivello(livelloProfilo)
        return GrammaticaSyllabus.percorso(mappa, now).mapIndexed { indice, unitaConStato ->
            val unita = unitaConStato.unita
            LezionePercorso(
                numero = indice + 1,
                unita = unita,
                stato = unitaConStato.stato,
                padronanza = unitaConStato.padronanza,
                accessibile = unitaConStato.stato != StatoUnita.BLOCCATA ||
                    ordineLivello(unita.livello) <= soglia
            )
        }
    }

    fun perId(
        id: String,
        mappa: Map<String, PadronanzaGrammatica>,
        livelloProfilo: String? = null
    ): LezionePercorso? = tutte(mappa, livelloProfilo).firstOrNull { it.id == id }

    /** Numero della lezione corrispondente all'unità (1-based), 1 se non esiste. */
    fun numeroDi(id: String): Int =
        GrammaticaSyllabus.unita.indexOfFirst { it.id == id }
            .let { if (it < 0) 1 else it + 1 }

    /**
     * Lezione consigliata:
     * 1. la prima lezione non completata del livello corrente o superiore;
     * 2. la prima lezione accessibile non completata (recupero livelli base);
     * 3. un ripasso scaduto;
     * 4. la prima lezione del percorso.
     */
    fun consigliata(
        mappa: Map<String, PadronanzaGrammatica>,
        livelloProfilo: String? = null,
        now: Long = System.currentTimeMillis()
    ): LezionePercorso? {
        val lista = tutte(mappa, livelloProfilo, now)
        if (lista.isEmpty()) return null
        val soglia = ordineLivello(livelloProfilo)
        return lista.firstOrNull {
            it.accessibile && !it.completata && ordineLivello(it.livello) >= soglia
        } ?: lista.firstOrNull { it.accessibile && !it.completata }
            ?: lista.firstOrNull { it.stato == StatoUnita.DA_RIPASSARE }
            ?: lista.first()
    }

    /** Lezione successiva accessibile, altrimenti la prima da ripassare. */
    fun prossima(
        id: String,
        mappa: Map<String, PadronanzaGrammatica>,
        livelloProfilo: String? = null
    ): LezionePercorso? {
        val lista = tutte(mappa, livelloProfilo)
        if (lista.isEmpty()) return null
        val indice = lista.indexOfFirst { it.id == id }
        val dopo = if (indice < 0) null else lista.drop(indice + 1).firstOrNull { it.accessibile }
        return dopo
            ?: lista.firstOrNull { it.stato == StatoUnita.DA_RIPASSARE && it.accessibile }
            ?: lista.firstOrNull { it.accessibile && !it.completata }
            ?: lista.last()
    }

    /**
     * Teoria della lezione in markdown, disponibile SEMPRE e offline.
     * È quello che la home mostra al posto della lezione generata dall'AI.
     */
    fun teoriaMarkdown(unita: UnitaGrammatica): String = buildString {
        val teoria = unita.teoria
        appendLine("## ${unita.titolo}")
        if (unita.sottotitolo.isNotBlank()) {
            appendLine("*${unita.sottotitolo}*")
            appendLine()
        }
        appendLine("**Obiettivo:** ${unita.obiettivo}")
        appendLine()

        if (teoria.regola.isNotEmpty()) {
            appendLine("### La regola")
            teoria.regola.forEach { appendLine("- $it") }
            appendLine()
        }

        teoria.tabelle.forEach { tabella ->
            appendLine("### ${tabella.titolo}")
            appendLine("| ${tabella.intestazioni.joinToString(" | ")} |")
            appendLine("|${tabella.intestazioni.joinToString("|") { "---" }}|")
            tabella.righe.forEach { riga ->
                appendLine("| ${riga.joinToString(" | ")} |")
            }
            if (tabella.nota.isNotBlank()) appendLine()
            if (tabella.nota.isNotBlank()) appendLine("_${tabella.nota}_")
            appendLine()
        }

        if (teoria.esempi.isNotEmpty()) {
            appendLine("### Esempi")
            teoria.esempi.forEach { esempio ->
                val nota = esempio.nota.takeIf { it.isNotBlank() }?.let { " — $it" }.orEmpty()
                appendLine("- **${esempio.tedesco}** — ${esempio.italiano}$nota")
            }
            appendLine()
        }

        if (teoria.erroriTipici.isNotEmpty()) {
            appendLine("### Errori tipici")
            teoria.erroriTipici.forEach { errore ->
                appendLine("- ~~${errore.sbagliato}~~ → **${errore.corretto}** — ${errore.spiegazione}")
            }
            appendLine()
        }

        if (teoria.merksatz.isNotBlank()) {
            appendLine("> **Merksatz:** ${teoria.merksatz}")
        }
    }.trim()
}
