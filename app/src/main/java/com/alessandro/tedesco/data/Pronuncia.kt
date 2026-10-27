package com.alessandro.tedesco.data

/**
 * Pronuncia: coppie minime e frasi utili. Il riconoscimento vocale non basta
 * (spesso "aggiusta" la pronuncia): qui si ascolta, si ripete e ci si registra
 * per confrontare.
 */
object Pronuncia {

    data class CoppiaMinima(
        val a: String,
        val b: String,
        val nota: String,
        val italianoA: String,
        val italianoB: String
    )

    data class Consiglio(
        val titolo: String,
        val testo: String,
        val esempio: String
    )

    val coppie: List<CoppiaMinima> = listOf(
        CoppiaMinima("schon", "schön", "u corta vs ü", "già", "bello"),
        CoppiaMinima("Mutter", "Mütter", "u vs ü", "madre", "madri"),
        CoppiaMinima("Kuchen", "Küchen", "u vs ü", "torta", "cucine"),
        CoppiaMinima("Höhle", "Hölle", "ö lunga vs ö corta", "caverna", "inferno"),
        CoppiaMinima("Ofen", "offen", "o lunga vs o corta + ff", "forno", "aperto"),
        CoppiaMinima("bitten", "bieten", "i corta vs ie lunga", "chiedere", "offrire"),
        CoppiaMinima("fühlen", "füllen", "ü lunga vs ü corta", "sentire", "riempire"),
        CoppiaMinima("Beet", "Bett", "e lunga vs e corta", "aiuola", "letto"),
        CoppiaMinima("Stadt", "Staat", "a corta vs a lunga", "città", "stato"),
        CoppiaMinima("Kirche", "Kirsche", "ch dolce vs sch", "chiesa", "ciliegia"),
        CoppiaMinima("wissen", "weißen", "ss vs ß + ei", "sapere", "bianco (verbo)"),
        CoppiaMinima("reisen", "reißen", "s vs ß", "viaggiare", "strappare")
    )

    val consigli: List<Consiglio> = listOf(
        Consiglio("ü", "Dì 'i' con le labbra arrotondate come per 'u'. Non è 'u'.", "Mütter, Küche, Übung"),
        Consiglio("ö", "Dì 'e' con le labbra arrotondate come per 'o'.", "schön, Höhle, können"),
        Consiglio("ch", "Dopo a/o/u è un suono gutturale (come la 'c' toscana di 'casa'); dopo i/e è dolce.", "Bach, Buch, ich, Kirche"),
        Consiglio("r", "In finale è quasi muto, vocalizzato: 'der' suona 'dea'. Non arrotare la r italiana.", "der, Mutter, Arzt"),
        Consiglio("z", "È 'ts', non 'z' italiana: 'zehn' = 'tsehn'.", "Zeit, zehn, Arzt"),
        Consiglio("s iniziale", "Davanti a vocale è sonora ('s' di 'rosa'): 'sie' = 'zii'.", "sie, sagen, Sonne"),
        Consiglio("ei / ie", "'ei' = 'ai'; 'ie' = 'ii' lunga. Sono opposti: non confonderli.", "mein vs Wien, Zeit vs sie"),
        Consiglio("Auslautverhärtung", "A fine sillaba b/d/g diventano p/t/k.", "Kind = 'kint', gab = 'gap', Tag = 'tak'"),
        Consiglio("accento", "Nelle parole tedesche l'accento è spesso sulla prima sillaba.", "Árbeit, Múttern, Phýsiotherapie (eccezione)")
    )

    val scioglilingua: List<String> = listOf(
        "Fischers Fritz fischt frische Fische.",
        "Brautkleid bleibt Brautkleid und Blaukraut bleibt Blaukraut.",
        "Zehn zahme Ziegen zogen zehn Zentner Zucker zum Zoo.",
        "Schnecken erschrecken, wenn Schnecken an Schnecken schlecken.",
        "Der Whiskymixer mixt Whisky, wenn der Whiskymixer Whisky mixt."
    )

    val frasiCliniche: List<String> = listOf(
        "Bitte beugen Sie das Knie langsam.",
        "Bitte strecken Sie das Bein.",
        "Atmen Sie tief ein und langsam aus.",
        "Wie stark sind die Schmerzen auf einer Skala von 1 bis 10?",
        "Bitte entspannen Sie die Schulter.",
        "Ich zeige Ihnen die Übung, dann machen Sie sie nach.",
        "Bitte bringen Sie den Befund mit.",
        "Haben Sie noch Fragen?"
    )
}
