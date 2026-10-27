package com.alessandro.tedesco.data

/**
 * Helper compatti per scrivere le unità del percorso senza ripetere
 * continuamente i nomi dei campi. Restano semplici funzioni di fabbrica:
 * non cambiano il modello salvato né la logica di valutazione.
 */

internal fun unitaGrammatica(
    id: String,
    livello: String,
    titolo: String,
    sottotitolo: String,
    obiettivo: String,
    minuti: Int,
    prerequisiti: List<String> = emptyList(),
    regola: List<String>,
    tabelle: List<TabellaGrammatica>,
    esempi: List<EsempioGrammatica>,
    errori: List<ErroreTipico>,
    merksatz: String,
    esercizi: List<EsercizioUnita>
): UnitaGrammatica = UnitaGrammatica(
    id = id,
    livello = livello,
    titolo = titolo,
    sottotitolo = sottotitolo,
    obiettivo = obiettivo,
    minuti = minuti,
    prerequisiti = prerequisiti,
    teoria = TeoriaGrammatica(
        regola = regola,
        tabelle = tabelle,
        esempi = esempi,
        erroriTipici = errori,
        merksatz = merksatz
    ),
    esercizi = esercizi
)

internal fun tabellaUnita(
    titolo: String,
    intestazioni: List<String>,
    righe: List<List<String>>,
    nota: String = ""
): TabellaGrammatica = TabellaGrammatica(
    titolo = titolo,
    intestazioni = intestazioni,
    righe = righe,
    nota = nota
)

internal fun esempioUnita(
    tedesco: String,
    italiano: String,
    nota: String = ""
): EsempioGrammatica = EsempioGrammatica(
    tedesco = tedesco,
    italiano = italiano,
    nota = nota
)

internal fun erroreUnita(
    sbagliato: String,
    corretto: String,
    spiegazione: String
): ErroreTipico = ErroreTipico(
    sbagliato = sbagliato,
    corretto = corretto,
    spiegazione = spiegazione
)

internal fun sceltaUnita(
    id: String,
    domanda: String,
    opzioni: List<String>,
    corretta: Int,
    spiegazione: String,
    suggerimento: String = ""
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.SCELTA_MULTIPLA,
    domanda = domanda,
    opzioni = opzioni,
    rispostaCorretta = corretta,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)

internal fun completaUnita(
    id: String,
    domanda: String,
    risposta: String,
    spiegazione: String,
    suggerimento: String = "Scrivi solo la parola o la forma mancante.",
    alternative: List<String> = emptyList()
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.COMPLETAMENTO,
    domanda = domanda,
    rispostaTesto = risposta,
    alternative = alternative,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)

internal fun trasformaUnita(
    id: String,
    domanda: String,
    risposta: String,
    spiegazione: String,
    suggerimento: String = "Scrivi la frase completa.",
    alternative: List<String> = emptyList()
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.TRASFORMAZIONE,
    domanda = domanda,
    rispostaTesto = risposta,
    alternative = alternative,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)

internal fun produciUnita(
    id: String,
    domanda: String,
    risposta: String,
    spiegazione: String,
    suggerimento: String = "Scrivi la frase completa in tedesco.",
    alternative: List<String> = emptyList()
): EsercizioUnita = EsercizioUnita(
    id = id,
    tipo = TipoEsercizioGrammatica.PRODUZIONE,
    domanda = domanda,
    rispostaTesto = risposta,
    alternative = alternative,
    spiegazione = spiegazione,
    suggerimento = suggerimento
)
