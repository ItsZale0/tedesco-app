package com.alessandro.tedesco.data

data class EsercizioGrammatica(
    val id: String,
    val categoria: CategoriaGrammatica,
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String,
    val esempio: String
)

enum class CategoriaGrammatica {
    VERBI_TEMPI,
    VERBI_MODALI,
    PASSIV,
    KONJUNKTIV_II,
    RELATIVSATZ,
    KONNEKTOREN,
    PRAEPOSITIONEN,
    ADJEKTIVDEKLINATION,
    ARTICOLI,
    INFINITIV_ZU
}

object GrammaticaB1 {

    val esercizi: List<EsercizioGrammatica> = listOf(
        // === VERBI_TEMPI ===
        EsercizioGrammatica(
            id = "v1",
            categoria = CategoriaGrammatica.VERBI_TEMPI,
            domanda = "Ich ___ gestern ins Kino gegangen.",
            opzioni = listOf("bin", "habe", "war", "hatte"),
            rispostaCorretta = 0,
            spiegazione = "Perfekt con 'sein' per verbi di movimento.",
            esempio = "Ich bin gestern ins Kino gegangen."
        ),
        EsercizioGrammatica(
            id = "v2",
            categoria = CategoriaGrammatica.VERBI_TEMPI,
            domanda = "Er ___ den ganzen Tag geschlafen.",
            opzioni = listOf("hat", "ist", "wird", "war"),
            rispostaCorretta = 0,
            spiegazione = "Perfekt con 'haben' per la maggior parte dei verbi.",
            esempio = "Er hat den ganzen Tag geschlafen."
        ),
        EsercizioGrammatica(
            id = "v3",
            categoria = CategoriaGrammatica.VERBI_TEMPI,
            domanda = "Wir ___ letzte Woche nach Berlin gefahren.",
            opzioni = listOf("sind", "haben", "waren", "hatten"),
            rispostaCorretta = 0,
            spiegazione = "Perfekt con 'sein' per verbi di movimento (fahren).",
            esempio = "Wir sind letzte Woche nach Berlin gefahren."
        ),

        // === VERBI_MODALI ===
        EsercizioGrammatica(
            id = "m1",
            categoria = CategoriaGrammatica.VERBI_MODALI,
            domanda = "Du ___ mir bitte helfen.",
            opzioni = listOf("kannst", "könntest", "könntet", "kann"),
            rispostaCorretta = 0,
            spiegazione = "Präsens di 'können' per richieste cortesi.",
            esempio = "Kannst du mir bitte helfen?"
        ),
        EsercizioGrammatica(
            id = "m2",
            categoria = CategoriaGrammatica.VERBI_MODALI,
            domanda = "Ich ___ morgen früh aufstehen.",
            opzioni = listOf("muss", "müsste", "musste", "müsse"),
            rispostaCorretta = 0,
            spiegazione = "Präsens di 'müssen' per obbligo.",
            esempio = "Ich muss morgen früh aufstehen."
        ),

        // === PASSIV ===
        EsercizioGrammatica(
            id = "p1",
            categoria = CategoriaGrammatica.PASSIV,
            domanda = "Das Buch ___ von vielen Menschen gelesen.",
            opzioni = listOf("ist", "wurde", "wird", "hat"),
            rispostaCorretta = 0,
            spiegazione = "Passiv Perfekt con 'sein' + Partizip II + 'worden'.",
            esempio = "Das Buch ist von vielen Menschen gelesen worden."
        ),
        EsercizioGrammatica(
            id = "p2",
            categoria = CategoriaGrammatica.PASSIV,
            domanda = "Die Hausaufgaben ___ morgen erledigt.",
            opzioni = listOf("werden", "wurden", "sind", "haben"),
            rispostaCorretta = 0,
            spiegazione = "Passiv Futur con 'werden' + Partizip II + 'werden'.",
            esempio = "Die Hausaufgaben werden morgen erledigt."
        ),

        // === KONJUNKTIV_II ===
        EsercizioGrammatica(
            id = "k1",
            categoria = CategoriaGrammatica.KONJUNKTIV_II,
            domanda = "Wenn ich mehr Zeit hätte, ___ ich mehr Deutsch lernen.",
            opzioni = listOf("würde", "werde", "würdest", "werden"),
            rispostaCorretta = 0,
            spiegazione = "Konjunktiv II con 'würde' + Infinitiv per irreale Bedingungen.",
            esempio = "Wenn ich mehr Zeit hätte, würde ich mehr Deutsch lernen."
        ),
        EsercizioGrammatica(
            id = "k2",
            categoria = CategoriaGrammatica.KONJUNKTIV_II,
            domanda = "Ich ___ gern nach Deutschland reisen.",
            opzioni = listOf("würde", "werde", "würdest", "werden"),
            rispostaCorretta = 0,
            spiegazione = "Konjunktiv II per desideri irreali.",
            esempio = "Ich würde gern nach Deutschland reisen."
        ),

        // === RELATIVSATZ ===
        EsercizioGrammatica(
            id = "r1",
            categoria = CategoriaGrammatica.RELATIVSATZ,
            domanda = "Der Mann, ___ ich gestern getroffen habe, ist mein Lehrer.",
            opzioni = listOf("den", "dem", "dessen", "der"),
            rispostaCorretta = 0,
            spiegazione = "Relativpronomen Akkusativ maskulin.",
            esempio = "Der Mann, den ich gestern getroffen habe, ist mein Lehrer."
        ),
        EsercizioGrammatica(
            id = "r2",
            categoria = CategoriaGrammatica.RELATIVSATZ,
            domanda = "Die Frau, ___ Auto gestohlen wurde, ruft die Polizei.",
            opzioni = listOf("deren", "der", "die", "dem"),
            rispostaCorretta = 0,
            spiegazione = "Relativpronomen Genitiv femminile.",
            esempio = "Die Frau, deren Auto gestohlen wurde, ruft die Polizei."
        ),

        // === KONNEKTOREN ===
        EsercizioGrammatica(
            id = "kn1",
            categoria = CategoriaGrammatica.KONNEKTOREN,
            domanda = "Ich lerne Deutsch, ___ ich in Deutschland arbeiten will.",
            opzioni = listOf("weil", "dass", "obwohl", "wenn"),
            rispostaCorretta = 0,
            spiegazione = "'weil' introduce una causa.",
            esempio = "Ich lerne Deutsch, weil ich in Deutschland arbeiten will."
        ),
        EsercizioGrammatica(
            id = "kn2",
            categoria = CategoriaGrammatica.KONNEKTOREN,
            domanda = "___ es regnet, gehen wir spazieren.",
            opzioni = listOf("Obwohl", "Weil", "Wenn", "Damit"),
            rispostaCorretta = 0,
            spiegazione = "'Obwohl' introduce una concessione.",
            esempio = "Obwohl es regnet, gehen wir spazieren."
        ),

        // === PRAEPOSITIONEN ===
        EsercizioGrammatica(
            id = "pr1",
            categoria = CategoriaGrammatica.PRAEPOSITIONEN,
            domanda = "Ich warte ___ den Bus.",
            opzioni = listOf("auf", "für", "mit", "zu"),
            rispostaCorretta = 0,
            spiegazione = "'warten auf' richiede Akkusativ.",
            esempio = "Ich warte auf den Bus."
        ),
        EsercizioGrammatica(
            id = "pr2",
            categoria = CategoriaGrammatica.PRAEPOSITIONEN,
            domanda = "Er freut sich ___ das Wochenende.",
            opzioni = listOf("auf", "über", "für", "mit"),
            rispostaCorretta = 0,
            spiegazione = "'sich freuen auf' richiede Akkusativ.",
            esempio = "Er freut sich auf das Wochenende."
        ),

        // === ADJEKTIVDEKLINATION ===
        EsercizioGrammatica(
            id = "a1",
            categoria = CategoriaGrammatica.ADJEKTIVDEKLINATION,
            domanda = "Ich sehe einen ___ Hund. (groß)",
            opzioni = listOf("großen", "große", "großem", "großer"),
            rispostaCorretta = 0,
            spiegazione = "Declinazione forte: Akkusativ maskulin -> -en.",
            esempio = "Ich sehe einen großen Hund."
        ),
        EsercizioGrammatica(
            id = "a2",
            categoria = CategoriaGrammatica.ADJEKTIVDEKLINATION,
            domanda = "Ich gebe ___ Frau ein Buch. (neu)",
            opzioni = listOf("der neuen", "die neue", "den neuen", "dem neuen"),
            rispostaCorretta = 0,
            spiegazione = "Declinazione debole: Dativ femminile -> -en.",
            esempio = "Ich gebe der neuen Frau ein Buch."
        ),

        // === ARTICOLI ===
        EsercizioGrammatica(
            id = "ar1",
            categoria = CategoriaGrammatica.ARTICOLI,
            domanda = "___ Buch liegt auf dem Tisch.",
            opzioni = listOf("Der", "Die", "Das", "Den"),
            rispostaCorretta = 2,
            spiegazione = "'Buch' è neutro, Nominativ -> 'das'.",
            esempio = "Das Buch liegt auf dem Tisch."
        ),
        EsercizioGrammatica(
            id = "ar2",
            categoria = CategoriaGrammatica.ARTICOLI,
            domanda = "Ich sehe ___ Frau.",
            opzioni = listOf("der", "die", "das", "den"),
            rispostaCorretta = 1,
            spiegazione = "'Frau' è femminile, Akkusativ -> 'die'.",
            esempio = "Ich sehe die Frau."
        ),

        // === INFINITIV_ZU ===
        EsercizioGrammatica(
            id = "i1",
            categoria = CategoriaGrammatica.INFINITIV_ZU,
            domanda = "Ich beginne ___ Deutsch lernen.",
            opzioni = listOf("zu", "Ø", "mit", "für"),
            rispostaCorretta = 0,
            spiegazione = "'beginnen' richiede 'zu' + Infinitiv.",
            esempio = "Ich beginne zu Deutsch lernen."
        ),
        EsercizioGrammatica(
            id = "i2",
            categoria = CategoriaGrammatica.INFINITIV_ZU,
            domanda = "Ich helfe dir ___ das Problem lösen.",
            opzioni = listOf("Ø", "zu", "mit", "für"),
            rispostaCorretta = 0,
            spiegazione = "'helfen' NON richiede 'zu' (Infinitiv senza zu).",
            esempio = "Ich helfe dir das Problem lösen."
        )
    )

    fun eserciziPerCategoria(categoria: CategoriaGrammatica): List<EsercizioGrammatica> {
        return esercizi.filter { it.categoria == categoria }
    }

    fun eserciziCasuali(n: Int = 5): List<EsercizioGrammatica> {
        return esercizi.shuffled().take(n)
    }
}
