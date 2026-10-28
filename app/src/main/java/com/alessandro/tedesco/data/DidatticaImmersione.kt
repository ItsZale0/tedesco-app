package com.alessandro.tedesco.data

/**
 * Modalità Immersione: didattica della modalità separata attivabile da Impostazioni.
 *
 * Metodo a 5 passi, da "Alle lernen Deutsch falsch (So funktioniert es)":
 *  1. Mentalità: il tedesco è logico; lo si capisce, non lo si memorizza.
 *  2. Input massiccio: circa 80% ascolto+lettura e 20% produzione, con
 *     materiale comprensibile intorno al 70-80% di comprensione.
 *  3. Vocabolario in FRASI (non parole isolate) + scomposizione dei
 *     composti: ogni pezzo conosciuto apre dieci composti nuovi.
 *  4. Shadowing: ripetere in tempo reale con un parlante nativo (qui: TTS).
 *  5. Sistema in 3 blocchi: immersione, ripasso, ascolto passivo
 *     (bonus: 15-20 minuti prima di dormire).
 *
 * Tutto offline: frasi e micro-storie sono scritte per livello e la
 * lettura ad alta voce usa il TTS già presente in casa.
 */

/** Una frase del percorso, con la traduzione rivelabile a richiesta. */
data class FraseImmersione(
    val livello: String,
    val tedesco: String,
    val italiano: String
)

/** Micro-storia comprensibile: input continuo con difficoltà calibrata. */
data class StoriaImmersione(
    val livello: String,
    val titolo: String,
    val contesto: String,
    val frasi: List<FraseImmersione>,
    val domande: List<DomandaComprensione>
)

/** Domanda di comprensione: verifica leggera, nessuna tabella di grammatica. */
data class DomandaComprensione(
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int
) {
    fun corretta(scelta: Int?): Boolean = scelta == rispostaCorretta
}

/** Parola ad alta frequenza vista dentro una frase vera. */
data class ParolaInContesto(
    val parola: String,
    val significato: String,
    val frase: String,
    val traduzione: String
)

/** Composto: le parti concatenate compongono la parola intera. */
data class Composto(
    val composto: String,
    val parti: List<String>,
    val significati: List<String>,
    val italiano: String
) {
    fun scomposizione(): String = parti.joinToString(" + ")
}

/** Frase da ombreggiare (blocco shadowing), con una nota sul ritmo. */
data class FraseShadowing(
    val tedesco: String,
    val italiano: String,
    val notaRitmo: String
)

object DidatticaImmersione {

    val LIVELLI: List<String> = listOf("A0", "A1", "A2", "B1", "B2")

    /** Normalizza "B1 - Intermedio" → "B1"; comunque un livello esistente. */
    fun perLivello(livello: String?): String {
        val codice = livello?.uppercase()
            ?.substringBefore(" - ")?.substringBefore(" ")?.trim().orEmpty()
        return if (codice in LIVELLI) codice else "A1"
    }

    // --------------------------------------------------------------
    // Blocco 1 — Immersione: micro-storie comprensibili
    // --------------------------------------------------------------

    fun storiaAttiva(livello: String?): StoriaImmersione = when (perLivello(livello)) {
        "A0" -> storiaA0()
        "A1" -> storiaA1()
        "A2" -> storiaA2()
        "B1" -> storiaB1()
        else -> storiaB2()
    }

    /** Cinque frasi nuove da annotare, cambiate a ogni giro (rotazione con wrap). */
    fun frasiNuove(livello: String?, giro: Int = 0): List<FraseImmersione> {
        val tutte = storiaAttiva(livello).frasi
        if (tutte.isEmpty()) return emptyList()
        val taglio = ((giro % 3) * 5) % tutte.size
        val base = tutte.drop(taglio).take(5)
        return if (base.size < 5) base + tutte.take(5 - base.size) else base
    }

    private fun frase(tedesco: String, italiano: String) = FraseImmersione("", tedesco, italiano)

    private fun domanda(domanda: String, opzioni: List<String>, rispostaCorretta: Int) =
        DomandaComprensione(domanda, opzioni, rispostaCorretta)

    private fun storiaA0() = StoriaImmersione(
        livello = "A0",
        titolo = "Ein Tag mit Moritz",
        contesto = "Una frase alla volta, mai due regole insieme. Il tedesco non è caos: sono scaglioni che si ripetono.",
        frasi = listOf(
            frase("Moritz steht um halb sieben auf.", "Moritz si alza alle sette e mezza."),
            frase("Er macht zuerst Kaffee.", "Prima fa il caffè."),
            frase("Der Kaffee ist stark.", "Il caffè è forte."),
            frase("Danach duscht er kurz.", "Dopo si doccia in fretta."),
            frase("Seine Tasche steht an der Tür.", "La sua borsa sta alla porta."),
            frase("Seine Wohnung ist klein.", "Il suo appartamento è piccolo."),
            frase("Im Fenster ist ein kleines Loch.", "Nella finestra c'è un buco piccolo."),
            frase("Der Wind kommt durch das Loch.", "Il vento entra dal buco."),
            frase("Moritz schließt das Fenster.", "Moritz chiude la finestra.")
        ),
        domande = listOf(
            domanda("A che ora si alza Moritz?", listOf("Alle fünf", "Um halb sieben", "Um neun Uhr"), 1),
            domanda("Cosa fa per primo?", listOf("Duscht", "Macht Kaffee", "Liest"), 1),
            domanda("Perché chiude la finestra?", listOf("Weil der Wind kommt", "Weil es dunkel ist", "Weil es kalt ist"), 0)
        )
    )

    private fun storiaA1() = StoriaImmersione(
        livello = "A1",
        titolo = "Einkauf am Markt",
        contesto = "Le stesse strutture in una scena reale: 1000 parole usuali coprono l'85% della conversazione quotidiana.",
        frasi = listOf(
            frase("Am Samstag geht Lena auf den Markt.", "Il sabato Lena va al mercato."),
            frase("Sie kauft Brot, Käse und Äpfel.", "Compra pane, formaggio e mele."),
            frase("Der Verkäufer hat keine Äpfel mehr.", "Il venditore non ha più mele."),
            frase("Darum nimmt sie Birnen.", "Per questo prende le pere."),
            frase("Weil sie viel kauft, braucht sie eine Tasche.", "Poiché compra tanto, le serve una borsa."),
            frase("Später fährt sie mit dem Rad nach Hause.", "Più tardi rientra a casa in bici."),
            frase("Der Radweg ist eine große Hilfe.", "La pista ciclabile è un grande aiuto."),
            frase("Dort ist ein schöner schattiger Hof.", "Lì c'è un cortile ombreggiato."),
            frase("Lena trinkt dort einen Kaffee.", "Lena si beve lì un caffè.")
        ),
        domande = listOf(
            domanda("Cosa prende al posto delle mele?", listOf("Birnen", "Eier", "Zitronen"), 0),
            domanda("Come torna a casa?", listOf("Mit dem Rad", "Mit dem Zug", "Mit dem Bus"), 0),
            domanda("Perché prende una borsa?", listOf("Weil sie viel kauft", "Weil es regnet", "Weil sie müde ist"), 0)
        )
    )

    private fun storiaA2() = StoriaImmersione(
        livello = "A2",
        titolo = "Beim Hautarzt",
        contesto = "Perfekt in una scena reale: le azioni finite raccontate con le parole di tutti i giorni.",
        frasi = listOf(
            frase("Letzte Woche hat Marco einen Ausschlag am Bein bekommen.", "La settimana scorsa Marco ha avuto un'eruzione sulla gamba."),
            frase("Er hat schnell einen Termin beim Hautarzt vereinbart.", "Ha fissato in fretta un appuntamento dal dermatologo."),
            frase("Der Arzt hat die Haut genau betrachtet.", "Il medico ha guardato la pelle con attenzione."),
            frase("„Haben Sie am Wochenende viel geschwitzt?“, fragte er.", "«Ha sudato molto nel weekend?», chiese."),
            frase("„Ein bisschen“, sagte Marco.", "«Un po'», disse Marco."),
            frase("Der Arzt hat gesagt, der Ausschlag ist harmlos.", "Il medico ha detto che l'eruzione è innocua."),
            frase("Er hat eine Salbe gegeben.", "Ha dato una pomata."),
            frase("Der Juckreiz ist bald verschwunden.", "Il prurito è sparito presto."),
            frase("Heute geht es Marco wieder gut.", "Oggi Marco sta di nuovo bene.")
        ),
        domande = listOf(
            domanda("Cosa ha avuto Marco?", listOf("Einen Ausschlag", "Eine Grippe", "Husten"), 0),
            domanda("Chi ha visto?", listOf("Den Zahnarzt", "Den Hautarzt", "Den Hausarzt"), 1),
            domanda("Cosa gli ha dato il medico?", listOf("Eine Salbe", "Tabletten", "Einen Verband"), 0)
        )
    )

    private fun storiaB1() = StoriaImmersione(
        livello = "B1",
        titolo = "Der Anruf, der alles änderte",
        contesto = "Proposizioni con verbo in fondo: la regola, vista dentro una frase vera, non in una tabella.",
        frasi = listOf(
            frase("Als Frau Bachmann zurück nach Hamburg kam, war sie unruhig.", "Quando la signora Bachmann tornò ad Amburgo, era agitata."),
            frase("Sie hatte gehofft, dass die Wohnung bald frei wird.", "Aveva sperato che l'appartamento si liberasse presto."),
            frase("Doch der Vermieter rief an und sagte, dass die Miete steigt.", "Ma il padrone di casa chiamò e disse che l'affitto sale."),
            frase("Obwohl sie müde war, lief sie zum Rathaus.", "Sebbene fosse stanca, corse al municipio."),
            frase("Der Termin, den sie sich notiert hatte, war schon vergeben.", "L'appuntamento che si era segnato era già occupato."),
            frase("Am Ende war sie froh, dass sie nicht aufgab.", "Alla fine era felice di non essersi arresa."),
            frase("Danach fand sie eine Wohnung mit kleinem Hof.", "Dopo trovò un appartamento con un piccolo cortile."),
            frase("Sie unterschrieb den Mietvertrag mit ruhiger Hand.", "Firmò il contratto d'affitto con mano tranquilla."),
            frase("„Jetzt kann ich wieder atmen“, sagte sie.", "«Ora posso di nuovo respirare», disse.")
        ),
        domande = listOf(
            domanda("Come si sentiva al ritorno?", listOf("Unruhig", "Glücklich", "Ruhig"), 0),
            domanda("Dove corse?", listOf("Zum Rathaus", "Zum Bahnhof", "Zum Arzt"), 0),
            domanda("Cosa trovò alla fine?", listOf("Eine Wohnung", "Ein Auto", "Einen Job"), 0)
        )
    )

    private fun storiaB2() = StoriaImmersione(
        livello = "B2",
        titolo = "Der Brief aus dem Rathaus",
        contesto = "Passivo con modali e Konjunktiv: le forme della burocrazia reale, riconosciute nel testo prima che spiegate.",
        frasi = listOf(
            frase("Die Bescheinigung muss bis Ende des Monats vorgelegt werden.", "L'attestato va presentato entro la fine del mese."),
            frase("Ohne die Unterschrift des Chefs wäre der Antrag abgelehnt worden.", "Senza la firma del capo la richiesta sarebbe stata respinta."),
            frase("Ein Formular, das seit Jahren nicht geändert worden war, musste neu ausgefüllt werden.", "Un modulo non cambiato da anni doveva essere ricompilato."),
            frase("Die Frau, deren Anwalt sie kannte, bat um eine kurze Fristverlängerung.", "La donna, di cui conosceva l'avvocato, chiese una breve proroga."),
            frase("Es ist fraglich, ob die Verlängerung gerechtfertigt ist.", "È discutibile che la proroga sia giustificata."),
            frase("Die zuständige Behörde prüft die Unterlagen sorgfältig.", "L'ufficio competente esamina i documenti attentamente."),
            frase("Falls etwas fehlt, wird der Fall erneut geöffnet.", "Se manca qualcosa, il caso viene riaperto."),
            frase("Voraussichtlich wird der Fall am Montag entschieden werden.", "Probabilmente il caso verrà deciso lunedì."),
            frase("Dann kann die Frau ruhig schlafen.", "Allora la signora potrà dormire tranquilla.")
        ),
        domande = listOf(
            domanda("Chi deve presentare l'attestato?", listOf("Die Frau", "Die Stadt", "Der Anwalt"), 0),
            domanda("Cosa chiese la donna?", listOf("Eine Fristverlängerung", "Einen Job", "Ein Formular"), 0),
            domanda("Quando si deciderà il caso?", listOf("Am Montag", "Am Freitag", "Nie"), 0)
        )
    )

    // --------------------------------------------------------------
    // Blocco 2 — Ripasso: shadowing + vocabolario in frasi
    // --------------------------------------------------------------

    fun shadowing(livello: String?): List<FraseShadowing> = when (perLivello(livello)) {
        "A0" -> listOf(
            FraseShadowing("Ich stehe auf.", "Mi alzo.", "Verbo separabile: auf va in fondo."),
            FraseShadowing("Der Kaffee ist stark.", "Il caffè è forte.", "Articolo DER corto e duro."),
            FraseShadowing("Ich gehe nach Hause.", "Vado a casa.", "Ritmo regolare: due note di tempo."),
            FraseShadowing("Das ist mein Rad.", "Questa è la mia bici.", "Frase corta, ritmo lento.")
        )
        "A1" -> listOf(
            FraseShadowing("Ich kaufe am Morgen Brot.", "Compro pane al mattino.", "La particella di tempo è in fondo."),
            FraseShadowing("Wir haben keine Äpfel mehr.", "Non abbiamo più mele.", "MEHR cade leggermente, come respiro."),
            FraseShadowing("Kannst du mir helfen?", "Puoi darmi una mano?", "KANNST in prima posizione, tono alto."),
            FraseShadowing("Ich hätte gern ein Kilo Äpfel.", "Vorrei un chilo di mele.", "Frase da mercato, ormai familiare.")
        )
        "A2" -> listOf(
            FraseShadowing("Ich habe den Termin vereinbart.", "Ho fissato l'appuntamento.", "Ausiliario in seconda, Partizip in fondo."),
            FraseShadowing("Am Ende der Woche bin ich müde.", "A fine settimana sono stanco.", "Piccola pausa prima di 'bin'."),
            FraseShadowing("Können wir das morgen besprechen?", "Possiamo parlarne domani?", "Domanda che sale alla fine."),
            FraseShadowing("Das war toll!", "È stato bello!", "Risposta breve, tono pieno.")
        )
        "B1" -> listOf(
            FraseShadowing("Als ich zurückkam, war alles anders.", "Quando sono tornato, tutto era cambiato.", "als + passato, verbo in fondo."),
            FraseShadowing("Obwohl es regnete, ging ich ohne Mantel aus.", "Sebbene piovesse, uscii senza cappotto.", "Obwohl: verbo impilato all'ultimo posto."),
            FraseShadowing("Es wäre gut, wenn wir früher gingen.", "Sarebbe bene se andassimo prima.", "Konjunktiv II, leggero come un respiro."),
            FraseShadowing("Mir scheint, das ist ein gutes Angebot.", "Mi sembra una buona offerta.", "Tono che sale su ANGEBOT.")
        )
        else -> listOf(
            FraseShadowing("Das müsste schnell erledigt werden.", "Andrebbe fatto in fretta.", "Passivo + modale: due infiniti alla fine."),
            FraseShadowing("Es ist fraglich, ob das gerechtfertigt ist.", "È discutibile che sia giustificato.", "Accento forte su gerechtfertigt."),
            FraseShadowing("Der Bericht, den wir erhalten haben, ist unvollständig.", "La relazione ricevuta è incompleta.", "La relativa: verbo in fondo."),
            FraseShadowing("Voraussichtlich wird er am Montag entschieden.", "Probabilmente verrà deciso lunedì.", "VOR-aus-sichtlich: accento all'inizio.")
        )
    }

    fun paroleNelContesto(livello: String?): List<ParolaInContesto> = when (perLivello(livello)) {
        "A0" -> listOf(
            ParolaInContesto("aufstehen", "alzarsi", "Ich stehe früh auf.", "Mi alzo presto."),
            ParolaInContesto("Kaffee", "caffè", "Der Kaffee ist fertig.", "Il caffè è pronto."),
            ParolaInContesto("Fenster", "finestra", "Das Fenster ist offen.", "La finestra è aperta."),
            ParolaInContesto("duschen", "farsi la doccia", "Ich dusche kurz.", "Mi faccio la doccia in fretta."),
            ParolaInContesto("Tasche", "borsa", "Die Tasche ist alt.", "La borsa è vecchia."),
            ParolaInContesto("Wohnung", "appartamento", "Meine Wohnung ist klein.", "Il mio appartamento è piccolo.")
        )
        "A1" -> listOf(
            ParolaInContesto("kaufen", "comprare", "Ich kaufe am Samstag ein.", "Faccio la spesa il sabato."),
            ParolaInContesto("Verkäufer", "venditore", "Der Verkäufer lacht.", "Il venditore ride."),
            ParolaInContesto("fahren", "andare (in veicolo)", "Ich fahre mit dem Rad.", "Vado con la bici."),
            ParolaInContesto("darum", "per questo", "Darum nehme ich Birnen.", "Per questo prendo le pere."),
            ParolaInContesto("Markt", "mercato", "Der Markt öffnet um acht.", "Il mercato apre alle otto."),
            ParolaInContesto("Radweg", "pista ciclabile", "Der Radweg ist neu.", "La pista ciclabile è nuova.")
        )
        "A2" -> listOf(
            ParolaInContesto("Ausschlag", "eruzione cutanea", "Der Ausschlag juckt.", "L'eruzione prude."),
            ParolaInContesto("vereinbaren", "fissare", "Ich vereinbare einen Termin.", "Fisso un appuntamento."),
            ParolaInContesto("schwitzen", "sudare", "Ich schwitze im Zug.", "Sudo nel treno."),
            ParolaInContesto("harmlos", "innocuo", "Das ist harmlos.", "È innocuo."),
            ParolaInContesto("Salbe", "pomata", "Die Salbe hilft schnell.", "La pomata aiuta presto."),
            ParolaInContesto("Juckreiz", "prurito", "Der Juckreiz ist weg.", "Il prurito è sparito.")
        )
        "B1" -> listOf(
            ParolaInContesto("unruhig", "agitato", "Sie war ganz unruhig.", "Era tutta agitata."),
            ParolaInContesto("Vermieter", "padrone di casa", "Der Vermieter rief um elf.", "Il padrone di casa chiamò alle undici."),
            ParolaInContesto("aufgeben", "arrendersi", "Ich gebe nicht auf.", "Non mollo."),
            ParolaInContesto("Angebot", "offerta", "Das Angebot ist fair.", "L'offerta è equa."),
            ParolaInContesto("Hof", "cortile", "Der Hof ist ruhig.", "Il cortile è tranquillo."),
            ParolaInContesto("Mietvertrag", "contratto d'affitto", "Der Mietvertrag ist fertig.", "Il contratto d'affitto è pronto.")
        )
        else -> listOf(
            ParolaInContesto("Bescheinigung", "attestato", "Die Bescheinigung kommt per Post.", "L'attestato arriva per posta."),
            ParolaInContesto("zuständig", "competente", "Welches Amt ist zuständig?", "Quale ufficio è competente?"),
            ParolaInContesto("gerechtfertigt", "giustificato", "Der Preis ist gerechtfertigt.", "Il prezzo è giustificato."),
            ParolaInContesto("Unterlagen", "documenti", "Die Unterlagen fehlen noch.", "I documenti mancano ancora."),
            ParolaInContesto("Fristverlängerung", "proroga", "Die Fristverlängerung war kurz.", "La proroga era breve."),
            ParolaInContesto("entscheiden", "decidere", "Wann wird entschieden?", "Quando si deciderà?")
        )
    }

    fun composti(livello: String?): List<Composto> = when (perLivello(livello)) {
        "A0" -> listOf(
            Composto("Fensterbank", listOf("Fenster", "bank"), listOf("finestra", "panchinetta"), "mensola della finestra"),
            Composto("Wasserfall", listOf("Wasser", "fall"), listOf("acqua", "caduta"), "cascata"),
            Composto("Krankenhaus", listOf("Kranken", "haus"), listOf("malati", "casa"), "ospedale")
        )
        "A1" -> listOf(
            Composto("Radweg", listOf("Rad", "weg"), listOf("bici", "via"), "pista ciclabile"),
            Composto("Markthalle", listOf("Markt", "halle"), listOf("mercato", "sala"), "mercato coperto"),
            Composto("Schwarzbrot", listOf("Schwarz", "brot"), listOf("nero", "pane"), "pane nero")
        )
        "A2" -> listOf(
            Composto("Schwitzbad", listOf("Schwitz", "bad"), listOf("sudore", "bagno"), "bagnodoccia caldo"),
            Composto("Arztbesuch", listOf("Arzt", "besuch"), listOf("medico", "visita"), "visita medica"),
            Composto("Schmerzmittel", listOf("Schmerz", "mittel"), listOf("dolore", "medio"), "antidolorifico")
        )
        "B1" -> listOf(
            Composto("Tagesplan", listOf("Tages", "plan"), listOf("della giornata", "piano"), "programma quotidiano"),
            Composto("Mietvertrag", listOf("Miet", "vertrag"), listOf("affitto", "contratto"), "contratto d'affitto"),
            Composto("Querstraße", listOf("Quer", "straße"), listOf("attraverso", "via"), "via laterale")
        )
        else -> listOf(
            Composto("Fristverlängerung", listOf("Frist", "ver", "längerung"), listOf("termine", "più", "allungamento"), "proroga"),
            Composto("Krankenhausaufenthalt", listOf("Kranken", "haus", "aufenthalt"), listOf("malati", "casa", "soggiorno"), "ricovero in ospedale"),
            Composto("Antragsformular", listOf("Antrag", "sformular"), listOf("richiesta", "modulo"), "modulo di richiesta")
        )
    }

    // --------------------------------------------------------------
    // Blocco 3 — Ascolto passivo: frasi in loop con TTS
    // --------------------------------------------------------------

    fun frasiPassive(livello: String?): List<String> =
        storiaAttiva(livello).frasi.map { it.tedesco } + shadowing(livello).map { it.tedesco }.distinct()
}
