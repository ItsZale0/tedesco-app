package com.alessandro.tedesco.data

/**
 * Sprechübungen: esercizi di produzione orale (parlato).
 *
 * Ogni esercizio presenta una situazione e un prompt: l'utente parla,
 * si registra e confronta con la risposta modello (letta dal TTS).
 * Copre i formati del Goethe-Zertifikat (Sprechen Teil 1-3) e le
 * Partnerseiten della Grammatik Aktiv.
 */
object Sprechuebungen {

    data class Prompt(
        val id: String,
        val livello: String,
        val situazione: String,
        val prompt: String,
        val vocabolario: List<String>,
        val modello: String
    )

    val prompts: List<Prompt> = listOf(
        // ---- A0 (primo contatto: salute, alfabeto, numeri, presentazioni minime) ----
        Prompt("sp-a0-01", "A0", "Salutare", "Saluta qualcuno e chiedi come sta.", listOf("Hallo", "Guten Tag", "Wie geht es Ihnen?", "mir geht es gut"), "Hallo! Guten Tag. Wie geht es Ihnen? — Mir geht es gut, danke."),
        Prompt("sp-a0-02", "A0", "Dire il proprio nome", "Dì come ti chiami e di dove sei.", listOf("Ich heiße...", "Ich bin aus...", "Und du?"), "Hallo, ich heiße Alessandro. Ich bin aus Italien. Und du? Wie heißt du?"),
        Prompt("sp-a0-03", "A0", "Sillabare il nome", "Sillaba il tuo nome alla reception, lettera per lettera.", listOf("Mein Name ist...", "der Buchstabe", "Bitte buchstabieren Sie"), "Entschuldigung, bitte buchstabieren Sie Ihren Namen. — Mein Name ist Alessandro: A-L-E-S-S-A-N-D-R-O."),
        Prompt("sp-a0-04", "A0", "Numeri e telefono", "Dì i numeri da 1 a 12 e lascia il tuo numero di telefono.", listOf("eins, zwei, drei", "Meine Nummer ist...", "null, sieben"), "Eins, zwei, drei, vier, fünf, sechs, sieben, acht, neun, zehn, elf, zwölf. Meine Telefonnummer ist null-drei-vier-sieben-zwei..."),
        Prompt("sp-a0-05", "A0", "Non stare bene", "Dì che non ti senti bene e ringrazia un fisioterapista.", listOf("Ich bin krank", "Es tut weh", "Danke schön", "Tschüss"), "Ich bin krank. Der Rücken tut weh. Danke schön für Ihre Hilfe. Tschüss!"),
        // ---- A1 ----
        Prompt("sp-a1-01", "A1", "Presentazione", "Presentati: nome, età, dove vivi, cosa fai.", listOf("heißen", "wohnen", "arbeiten", "lernen"), "Ich heiße Anna. Ich wohne in Bozen. Ich bin Studentin. Ich lerne Deutsch."),
        Prompt("sp-a1-02", "A1", "Al supermercato", "Chiedi dove si trova il pane e compra 2 pagnotte.", listOf("Wo ist...?", "Ich möchte...", "zwei", "Brot"), "Entschuldigung, wo ist das Brot? Ich möchte zwei Brote, bitte."),
        Prompt("sp-a1-03", "A1", "Al ristorante", "Ordina un caffè e un pezzo di torta.", listOf("Ich hätte gern", "Kaffee", "Kuchen", "bitte"), "Ich hätte gern einen Kaffee und ein Stück Kuchen, bitte."),
        Prompt("sp-a1-04", "A1", "Presentare un amico", "Presenta un amico: nome, provenienza, lavoro.", listOf("Das ist...", "Er/Sie kommt aus...", "Er/Sie arbeitet als..."), "Das ist Marco. Er kommt aus Italien. Er arbeitet als Physiotherapeut."),
        Prompt("sp-a1-05", "A1", "Chiedere l'ora", "Chiedi l'ora e rispondi che sono le 3.", listOf("Wie viel Uhr ist es?", "Es ist ... Uhr", "drei"), "Entschuldigung, wie viel Uhr ist es? — Es ist drei Uhr."),
        // ---- A2 ----
        Prompt("sp-a2-01", "A2", "Descrivere un appartamento", "Descrivi il tuo appartamento: stanze, posizione, prezzo.", listOf("Meine Wohnung hat...", "Sie liegt in...", "Sie kostet..."), "Meine Wohnung hat zwei Zimmer. Sie liegt in der Stadtmitte. Sie kostet 800 Euro im Monat."),
        Prompt("sp-a2-02", "A2", "Al lavoro", "Racconta la tua giornata lavorativa.", listOf("Ich stehe auf um...", "Ich arbeite von... bis...", "Am Abend..."), "Ich stehe um 7 Uhr auf. Ich arbeite von 9 bis 17 Uhr. Am Abend koche ich und sehe fern."),
        Prompt("sp-a2-03", "A2", "Prenotare un tavolo", "Prenota un tavolo per 4 persone per stasera alle 20.", listOf("Ich möchte reservieren", "für 4 Personen", "um 20 Uhr", "auf den Namen"), "Ich möchte einen Tisch für vier Personen reservieren, heute um 20 Uhr, auf den Namen Müller."),
        Prompt("sp-a2-04", "A2", "Descrivere una persona", "Descrivi un amico: aspetto, carattere, hobby.", listOf("Er/Sie ist...", "Er/Sie hat...", "Sein/Ihr Hobby ist..."), "Marco ist groß und sportlich. Er hat braune Haare. Sein Hobby ist Fußball."),
        Prompt("sp-a2-05", "A2", "Al telefono", "Lascia un messaggio: non puoi venire all'appuntamento.", listOf("Hier ist...", "Ich kann leider nicht kommen", "Können wir verschieben?", "Tschüss"), "Hier ist Anna. Ich kann leider nicht kommen. Können wir den Termin verschieben? Tschüss!"),
        // ---- B1 ----
        Prompt("sp-b1-01", "B1", "Esperienza di viaggio", "Racconta un viaggio che hai fatto: dove, quando, cosa hai fatto.", listOf("Ich war in...", "Es war...", "Wir haben...", "Das Beste war..."), "Ich war letztes Jahr in Berlin. Es war sehr interessant. Wir haben viele Museen besucht. Das Beste war das Brandenburger Tor."),
        Prompt("sp-b1-02", "B1", "Pro e contro", "Dai la tua opinione su lavorare da casa: vantaggi e svantaggi.", listOf("Ich finde, dass...", "Ein Vorteil ist...", "Ein Nachteil ist...", "Trotzdem..."), "Ich finde, dass Homeoffice gut ist. Ein Vorteil ist die Flexibilität. Ein Nachteil ist die Einsamkeit. Trotzdem bevorzuge ich es."),
        Prompt("sp-b1-03", "B1", "Esperienza lavorativa", "Racconta la tua esperienza professionale e i tuoi obiettivi.", listOf("Ich arbeite als...", "Meine Aufgaben sind...", "In Zukunft möchte ich..."), "Ich arbeite als Physiotherapeut. Meine Aufgaben sind Behandlungen und Beratung. In Zukunft möchte ich mich weiterbilden."),
        Prompt("sp-b1-04", "B1", "Pianificare insieme", "Proponi a un collega di organizzare un evento: data, luogo, attività.", listOf("Wir könnten...", "Was hältst du von...?", "Wann passt es dir?", "Einverstanden?"), "Wir könnten am Samstag ein Fest machen. Was hältst du von einem Grillabend? Wann passt es dir? Einverstanden?"),
        Prompt("sp-b1-05", "B1", "Raccontare un problema", "Racconta un problema che hai avuto e come lo hai risolto.", listOf("Ich hatte ein Problem...", "Das Problem war...", "Ich habe... gemacht", "Am Ende..."), "Ich hatte ein Problem mit meinem Computer. Das Problem war ein Virus. Ich habe einen Techniker gerufen. Am Ende war alles gelöst."),
        // ---- B2 ----
        Prompt("sp-b2-01", "B2", "Presentazione professionale", "Presenta il tuo profilo professionale: formazione, esperienza, obiettivi.", listOf("Ich habe... studiert", "Meine Erfahrung umfasst...", "Mein Ziel ist...", "Ich bin besonders interessiert an..."), "Ich habe Physiotherapie studiert. Meine Erfahrung umfasst fünf Jahre in einer Klinik. Mein Ziel ist, mich auf Sportmedizin zu spezialisieren."),
        Prompt("sp-b2-02", "B2", "Argomentare", "Argomenta su un tema attuale: vantaggi e svantaggi, poi la tua posizione.", listOf("Einerseits... andererseits...", "Ich bin der Meinung, dass...", "Zusammenfassend...", "Meiner Ansicht nach..."), "Einerseits spart Homeoffice Zeit, andererseits fehlt der soziale Kontakt. Ich bin der Meinung, dass eine Mischung am besten ist. Zusammenfassend: Flexibilität ist wichtig, aber nicht auf Kosten der Teamarbeit."),
        Prompt("sp-b2-03", "B2", "Spiegare un processo", "Spiega un processo o una procedura step by step.", listOf("Zuerst...", "Dann...", "Anschließend...", "Zum Schluss..."), "Zuerst begrüße ich den Patienten. Dann erkläre ich die Übung. Anschließend demonstriere ich die Bewegung. Zum Schluss lasse ich den Patienten die Übung selbst machen."),
        Prompt("sp-b2-04", "B2", "Discutere", "Discuti con un collega: proponi una soluzione e difendi la tua posizione.", listOf("Ich würde vorschlagen...", "Das hat den Vorteil, dass...", "Allerdings...", "Es wäre sinnvoll, wenn..."), "Ich würde vorschlagen, die Termine neu zu organisieren. Das hat den Vorteil, dass die Wartezeiten kürzer werden. Allerdings brauchen wir mehr Personal. Es wäre sinnvoll, wenn wir das mit dem Team besprechen."),
        Prompt("sp-b2-05", "B2", "Raccontare un'esperienza", "Racconta un'esperienza significativa e cosa hai imparato.", listOf("Es war eine Situation, in der...", "Ich habe gelernt, dass...", "Rückblickend...", "Das hat mich geprägt, weil..."), "Es war eine Situation, in der ich einen schwierigen Patienten hatte. Ich habe gelernt, dass Geduld wichtig ist. Rückblickend war es eine wertvolle Erfahrung. Das hat mich geprägt, weil ich jetzt besser mit Stress umgehen kann.")
    )

    fun perLivello(livello: String?): List<Prompt> {
        val codice = livello?.uppercase()?.substringBefore(" ")?.trim() ?: "A1"
        val esatti = prompts.filter { it.livello == codice }
        if (esatti.isNotEmpty()) return esatti
        // Livello senza contenuto dedicato (es. A0 prima dei suoi prompt, C1, C2):
        // si mostra quello del livello precedente invece di una schermata vuota.
        return prompts.filter { it.livello == "A1" }
    }

    fun perCodice(codice: String): Prompt? = prompts.firstOrNull { it.id == codice }
}