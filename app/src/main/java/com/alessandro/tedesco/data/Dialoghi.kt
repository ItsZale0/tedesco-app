package com.alessandro.tedesco.data

/** Dialoghi per l'ascolto: conversazioni realistiche a 2 voci. */
object Dialoghi {

    data class BattutaDialogo(
        val parlante: String,      // "A" o "B"
        val testo: String,
        val traduzione: String
    )

    data class DomandaDialogo(
        val domanda: String,
        val opzioni: List<String>,
        val corretta: Int
    )

    data class DialogoAscolto(
        val id: String,
        val titolo: String,
        val livello: String,
        val battute: List<BattutaDialogo>,
        val domande: List<DomandaDialogo>,
        val traduzione: String // descrizione generale
    )

    val dialoghi: List<DialogoAscolto> = listOf(
        DialogoAscolto(
            id = "dial_01",
            titolo = "Alla reception",
            livello = "A2",
            traduzione = "Un paziente si registra in fisioterapia.",
            battute = listOf(
                BattutaDialogo("A", "Guten Tag, ich habe einen Termin um zehn Uhr.", "Buongiorno, ho un appuntamento alle dieci."),
                BattutaDialogo("B", "Guten Tag. Wie ist Ihr Name, bitte?", "Buongiorno. Come si chiama, per favore?"),
                BattutaDialogo("A", "Ich heiße Rossi. Mario Rossi.", "Mi chiamo Rossi. Mario Rossi."),
                BattutaDialogo("B", "Herr Rossi. Haben Sie die Überweisung dabei?", "Signor Rossi. Ha l'impegnativa con sé?"),
                BattutaDialogo("A", "Ja, hier ist sie. Und den Befund vom Orthopäden.", "Sì, eccola. E il referto dell'ortopedico."),
                BattutaDialogo("B", "Perfekt. Bitte nehmen Sie Platz. Die Therapeutin kommt gleich.", "Perfetto. Si accomodi pure. La terapista arriva tra poco."),
                BattutaDialogo("A", "Danke schön. Wie lange dauert die erste Sitzung?", "Grazie. Quanto dura la prima seduta?"),
                BattutaDialogo("B", "Etwa 45 Minuten. Wir machen die Anamnese und einen Befund.", "Circa 45 minuti. Facciamo l'anamnesi e un esame.")
            ),
            domande = listOf(
                DomandaDialogo("A che ora è l'appuntamento?", listOf("Alle 9", "Alle 10", "Alle 11", "Alle 12"), 1),
                DomandaDialogo("Cosa porta il paziente?", listOf("Solo l'impegnativa", "Impegnativa e referto", "Solo il referto", "Niente"), 1),
                DomandaDialogo("Quanto dura la prima seduta?", listOf("30 minuti", "45 minuti", "60 minuti", "90 minuti"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_02",
            titolo = "Anamnesi: mal di schiena",
            livello = "B1",
            traduzione = "Prima seduta: il fisioterapista fa l'anamnesi a un paziente con lombalgia.",
            battute = listOf(
                BattutaDialogo("A", "Guten Morgen, ich bin Herr Müller, Ihr Physiotherapeut. Was führt Sie zu uns?", "Buongiorno, sono il signor Müller, il suo fisioterapista. Cosa la porta da noi?"),
                BattutaDialogo("B", "Ich habe seit drei Wochen Rückenschmerzen, vor allem morgens.", "Ho mal di schiena da tre settimane, soprattutto la mattina."),
                BattutaDialogo("A", "Können Sie mir zeigen, wo genau es wehtut?", "Mi può indicare dove fa male esattamente?"),
                BattutaDialogo("B", "Hier, im unteren Rücken. Manchmal strahlt es ins Bein aus.", "Qui, in basso alla schiena. A volte irradia alla gamba."),
                BattutaDialogo("A", "Haben Sie Taubheitsgefühle oder Kribbeln im Bein?", "Ha intorpidimento o formicolio alla gamba?"),
                BattutaDialogo("B", "Nein, nur Schmerzen. Aber nachts wache ich manchmal auf.", "No, solo dolore. Ma di notte mi sveglio a volte."),
                BattutaDialogo("A", "Verstehen Sie. Machen Sie Sport? Wie ist Ihr Arbeitsalltag?", "Capisco. Fa sport? Com'è la sua giornata lavorativa?"),
                BattutaDialogo("B", "Ich sitze viel am Schreibtisch. Jogge aber zweimal die Woche.", "Sto molto alla scrivania. Ma corro due volte a settimana."),
                BattutaDialogo("A", "Gut. Dann schauen wir uns die Beweglichkeit an. Bitte beugen Sie sich vor.", "Bene. Allora controlliamo la mobilità. Si pieghi in avanti per favore.")
            ),
            domande = listOf(
                DomandaDialogo("Da quanto tempo ha dolore il paziente?", listOf("Una settimana", "Due settimane", "Tre settimane", "Un mese"), 2),
                DomandaDialogo("Dove irradia il dolore?", listOf("Al braccio", "Alla gamba", "Al collo", "Non irradia"), 1),
                DomandaDialogo("Cosa fa il paziente di lavoro?", listOf("Lavoro fisico", "Lavoro alla scrivania", "Insegnante", "In pensione"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_03",
            titolo = "Telefonata: appuntamento",
            livello = "A2",
            traduzione = "Chiamata per fissare un appuntamento di fisioterapia.",
            battute = listOf(
                BattutaDialogo("A", "Physiotherapie Huber, guten Tag.", "Fisioterapia Huber, buongiorno."),
                BattutaDialogo("B", "Guten Tag, ich möchte gerne einen Termin vereinbaren.", "Buongiorno, vorrei fissare un appuntamento."),
                BattutaDialogo("A", "Gerne. Haben Sie eine Überweisung vom Arzt?", "Volentieri. Ha un'impegnativa del medico?"),
                BattutaDialogo("B", "Ja, die habe ich. Ich habe Knieschmerzen seit einer Woche.", "Sì, ce l'ho. Ho dolore al ginocchio da una settimana."),
                BattutaDialogo("A", "Verstehe. Haben Sie morgens oder nachmittags Zeit?", "Capisco. Ha tempo la mattina o il pomeriggio?"),
                BattutaDialogo("B", "Nachmittags wäre besser. Ab 14 Uhr.", "Il pomeriggio sarebbe meglio. Dalle 14."),
                BattutaDialogo("A", "Morgen um 14:30 wäre frei. Passt das?", "Domani alle 14:30 sarebbe libero. Va bene?"),
                BattutaDialogo("B", "Ja, perfekt. Danke schön!", "Sì, perfetto. Grazie mille!"),
                BattutaDialogo("A", "Bitte bringen Sie die Überweisung und bequeme Kleidung mit.", "Porti l'impegnativa e abbigliamento comodo.")
            ),
            domande = listOf(
                DomandaDialogo("Quando ha tempo il paziente?", listOf("Mattina", "Pomeriggio dalle 14", "Sera", "Weekend"), 1),
                DomandaDialogo("Quando è l'appuntamento?", listOf("Oggi 14:30", "Domani 14:30", "Domani 10:00", "Dopodomani 14:30"), 1),
                DomandaDialogo("Cosa deve portare?", listOf("Solo documento", "Impegnativa e abbigliamento comodo", "Referto RM", "Niente"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_04",
            titolo = "In pausa: colleghi austriaci",
            livello = "B1",
            traduzione = "Due colleghi in mensa usano varianti austriache (Jänner, Erdapfel, Topfen).",
            battute = listOf(
                BattutaDialogo("A", "Na, hast du heuer schon Urlaub gemacht?", "Allora, hai fatto le ferie quest'anno?"),
                BattutaDialogo("B", "Ja, im Jänner war ich in Südtirol. Die Berge sind traumhaft.", "Sì, a gennaio ero in Südtirol. Le montagne sono da sogno."),
                BattutaDialogo("A", "Und? Gab's dort gute Erdäpfel? Die Knödel sind der Hammer.", "E? Ci sono buone patate lì? I canederli sono spettacolari."),
                BattutaDialogo("B", "Absolut. Und einen Topfenstrudel muss man probieren. Nicht Quark, Topfen!", "Assolutamente. E devi provare lo strudel di topfen. Non quark, topfen!"),
                BattutaDialogo("A", "Stimmt. Bei uns sagt man Semmel, nicht Brötchen. Und Paradeiser für Tomaten.", "Vero. Da noi si dice Semmel, non Brötchen. E Paradeiser per pomodori."),
                BattutaDialogo("B", "Genau. In der Ordination sagen die Patienten oft 'Jänner' statt Januar.", "Esatto. In ambulatorio i pazienti dicono spesso 'Jänner' invece di Januar."),
                BattutaDialogo("A", "Macht nichts. Hauptsache, wir verstehen uns. Prost!", "Non importa. L'importante è capirsi. Cin cin!"),
                BattutaDialogo("B", "Prost! Auf die Erdäpfel!", "Cin cin! Alle patate!")
            ),
            domande = listOf(
                DomandaDialogo("Quando è andato in vacanza il collega B?", listOf("A luglio", "A gennaio", "Ad agosto", "A Natale"), 1),
                DomandaDialogo("Quale dolce austriaco menzionano?", listOf("Apfelstrudel", "Topfenstrudel", "Sachertorte", "Kaiserschmarrn"), 1),
                DomandaDialogo("Come dicono 'panino' in Austria?", listOf("Brötchen", "Semmel", "Weck", "Schrippe"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_05",
            titolo = "Esercizi a casa: istruzioni",
            livello = "B1",
            traduzione = "Il fisioterapista spiega gli esercizi da fare a casa per il ginocchio.",
            battute = listOf(
                BattutaDialogo("A", "So, jetzt zeige ich Ihnen die Hausübungen für Ihr Knie.", "Allora, le mostro gli esercizi da fare a casa per il ginocchio."),
                BattutaDialogo("B", "Wie oft soll ich die machen?", "Quanto spesso devo farli?"),
                BattutaDialogo("A", "Dreimal am Tag. Morgens, mittags, abends. Je zehn Wiederholungen.", "Tre volte al giorno. Mattina, mezzogiorno, sera. Dieci ripetizioni ciascuna."),
                BattutaDialogo("B", "Und wenn es wehtut? Soll ich aufhören?", "E se fa male? Devo smettere?"),
                BattutaDialogo("A", "Leichter Schmerz ist okay. Aber scharfer Schmerz: sofort aufhören.", "Un leggero dolore va bene. Ma dolore acuto: smetta subito."),
                BattutaDialogo("B", "Verstanden. Wann kommt die Kontrolle?", "Capito. Quando è il controllo?"),
                BattutaDialogo("A", "In zwei Wochen. Bringen Sie das Tagebuch mit, wo Sie die Übungen eintragen.", "Tra due settimane. Porti il diario dove segna gli esercizi."),
                BattutaDialogo("B", "Alles klar. Danke, Herr Müller.", "Tutto chiaro. Grazie, signor Müller."),
                BattutaDialogo("A", "Gute Besserung! Und denken Sie dran: Bewegung ist die beste Medizin.", "Buona guarigione! E ricordi: il movimento è la migliore medicina.")
            ),
            domande = listOf(
                DomandaDialogo("Quante volte al giorno gli esercizi?", listOf("Una", "Due", "Tre", "Quattro"), 2),
                DomandaDialogo("Quando è il prossimo controllo?", listOf("Tra una settimana", "Tra due settimane", "Tra un mese", "Non c'è"), 1),
                DomandaDialogo("Cosa portare al controllo?", listOf("Il referto", "Il diario degli esercizi", "La ricetta", "L'impegnativa"), 1)
            )
        )
    )
}