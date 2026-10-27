package com.alessandro.tedesco.data

/**
 * Wortliste B1 curata: 100+ parole ad alta frequenza, divise per tema, con
 * esempio. Non sostituisce la Wortliste ufficiale Goethe (copyright), ma dà
 * un nucleo stabile e corretto su cui costruire il lessico d'esame e quello
 * sanitario.
 */
object WortlisteB1 {

    data class Voce(
        val tedesco: String,
        val italiano: String,
        val esempio: String,
        val tema: String,
        val livello: String = "B1"
    )

    private val grezzo = """
        der Termin|appuntamento|Ich habe morgen einen Termin.|Lavoro
        die Bewerbung|domanda di lavoro|Ich schreibe eine Bewerbung.|Lavoro
        der Lebenslauf|curriculum|Mein Lebenslauf ist aktuell.|Lavoro
        das Vorstellungsgespräch|colloquio di lavoro|Das Vorstellungsgespräch ist am Montag.|Lavoro
        die Stelle|posto di lavoro|Ich suche eine Stelle in Brixen.|Lavoro
        der Arbeitgeber|datore di lavoro|Mein Arbeitgeber ist eine Klinik.|Lavoro
        der Arbeitnehmer|dipendente|Der Arbeitnehmer hat Rechte.|Lavoro
        die Ausbildung|formazione|Meine Ausbildung dauert drei Jahre.|Lavoro
        die Weiterbildung|formazione continua|Ich mache eine Weiterbildung.|Lavoro
        die Erfahrung|esperienza|Ich habe zwei Jahre Erfahrung.|Lavoro
        die Verantwortung|responsabilità|Ich übernehme Verantwortung.|Lavoro
        die Zusammenarbeit|collaborazione|Die Zusammenarbeit im Team ist gut.|Lavoro
        der Vertrag|contratto|Ich unterschreibe den Vertrag.|Lavoro
        die Kündigung|licenziamento|Die Kündigung muss schriftlich sein.|Lavoro
        das Gehalt|stipendio|Das Gehalt ist fair.|Lavoro
        die Arbeitszeit|orario di lavoro|Die Arbeitszeit ist flexibel.|Lavoro
        der Feierabend|fine giornata lavorativa|Endlich Feierabend!|Lavoro
        die Überstunde|ora straordinaria|Ich mache heute eine Überstunde.|Lavoro
        der Kollege|collega|Mein Kollege hilft mir.|Lavoro
        die Kollegin|collega (f.)|Meine Kollegin ist krank.|Lavoro
        die Gesundheit|salute|Gesundheit ist wichtig.|Salute
        die Krankheit|malattia|Die Krankheit dauert eine Woche.|Salute
        der Schmerz|dolore|Der Schmerz ist stark.|Salute
        die Behandlung|trattamento|Die Behandlung beginnt morgen.|Salute
        der Befund|referto|Der Befund ist da.|Salute
        die Überweisung|impegnativa|Ich brauche eine Überweisung.|Salute
        die Krankenkasse|cassa mutua|Die Krankenkasse zahlt die Therapie.|Salute
        die Ordination|studio medico|Die Ordination ist im Zentrum.|Salute
        das Rezept|ricetta|Der Arzt schreibt ein Rezept.|Salute
        die Physiotherapie|fisioterapia|Ich arbeite in der Physiotherapie.|Salute
        die Bewegung|movimento|Bewegung hilft gegen Schmerzen.|Salute
        die Übung|esercizio|Diese Übung machen Sie täglich.|Salute
        der Patient|paziente|Der Patient kommt um neun.|Salute
        die Patientin|paziente (f.)|Die Patientin hat Knieschmerzen.|Salute
        die Anamnese|anamnesi|Die Anamnese dauert zehn Minuten.|Salute
        die Diagnose|diagnosi|Die Diagnose ist klar.|Salute
        der Therapieplan|piano terapeutico|Wir erstellen einen Therapieplan.|Salute
        die Kontrolle|controllo|Die Kontrolle ist nächste Woche.|Salute
        die Entlassung|dimissione|Die Entlassung ist am Freitag.|Salute
        die Zuweisung|invio|Ich habe eine Zuweisung vom Arzt.|Salute
        die Gesellschaft|società|Die Gesellschaft verändert sich.|Società
        die Politik|politica|Politik interessiert mich.|Società
        die Wahl|elezione|Die Wahl ist im Herbst.|Società
        die Demokratie|democrazia|Demokratie braucht Beteiligung.|Società
        die Meinung|opinione|Meine Meinung ist anders.|Società
        die Diskussion|discussione|Die Diskussion war lang.|Società
        der Vorteil|vantaggio|Der Vorteil ist klar.|Società
        der Nachteil|svantaggio|Der Nachteil ist der Preis.|Società
        die Lösung|soluzione|Wir brauchen eine Lösung.|Società
        das Problem|problema|Das Problem ist komplex.|Società
        die Regel|regola|Diese Regel gilt für alle.|Società
        die Freiheit|libertà|Freiheit ist wichtig.|Società
        die Gleichberechtigung|parità di diritti|Gleichberechtigung ist ein Ziel.|Società
        die Integration|integrazione|Integration braucht Zeit.|Società
        die Migration|migrazione|Migration prägt die Gesellschaft.|Società
        die Umwelt|ambiente|Wir müssen die Umwelt schützen.|Ambiente
        der Klimawandel|cambiamento climatico|Der Klimawandel ist real.|Ambiente
        die Energie|energia|Wir brauchen saubere Energie.|Ambiente
        der Müll|rifiuti|Der Müll wird getrennt.|Ambiente
        das Recycling|riciclo|Recycling spart Ressourcen.|Ambiente
        die Verschmutzung|inquinamento|Die Verschmutzung nimmt zu.|Ambiente
        der Verkehr|traffico|Der Verkehr ist laut.|Ambiente
        die Nachhaltigkeit|sostenibilità|Nachhaltigkeit ist ein Ziel.|Ambiente
        die Ressource|risorsa|Wasser ist eine Ressource.|Ambiente
        der Naturschutz|tutela della natura|Naturschutz beginnt vor Ort.|Ambiente
        die Sonnenenergie|energia solare|Sonnenenergie ist kostenlos.|Ambiente
        das Windrad|pala eolica|Das Windrad steht auf dem Hügel.|Ambiente
        die Technologie|tecnologia|Technologie entwickelt sich schnell.|Tecnologia
        die Digitalisierung|digitalizzazione|Die Digitalisierung verändert die Arbeit.|Tecnologia
        das Internet|internet|Das Internet ist langsam.|Tecnologia
        die Daten|dati|Die Daten sind sicher.|Tecnologia
        die Sicherheit|sicurezza|Sicherheit ist wichtig.|Tecnologia
        die Künstliche Intelligenz|intelligenza artificiale|Künstliche Intelligenz hilft bei Texten.|Tecnologia
        das Gerät|dispositivo|Das Gerät ist neu.|Tecnologia
        die Anwendung|applicazione|Die Anwendung ist einfach.|Tecnologia
        die Entwicklung|sviluppo|Die Entwicklung dauert Jahre.|Tecnologia
        die Forschung|ricerca|Die Forschung braucht Geld.|Tecnologia
        die Kultur|cultura|Kultur verbindet Menschen.|Cultura
        die Tradition|tradizione|Die Tradition ist alt.|Cultura
        die Veranstaltung|evento|Die Veranstaltung ist heute.|Cultura
        die Ausstellung|mostra|Die Ausstellung ist interessant.|Cultura
        das Museum|museo|Das Museum ist geschlossen.|Cultura
        die Literatur|letteratura|Ich lese gern Literatur.|Cultura
        die Musik|musica|Musik hilft beim Lernen.|Cultura
        das Theater|teatro|Wir gehen ins Theater.|Cultura
        der Brauch|usanza|Der Brauch ist typisch für Südtirol.|Cultura
        die Geschichte|storia|Die Geschichte ist spannend.|Cultura
        die Emotion|emozione|Emotionen sind wichtig.|Emozioni
        die Freude|gioia|Die Freude war groß.|Emozioni
        die Angst|paura|Die Angst vor Fehlern ist normal.|Emozioni
        die Sorge|preoccupazione|Ich habe Sorgen.|Emozioni
        die Hoffnung|speranza|Die Hoffnung bleibt.|Emozioni
        die Enttäuschung|delusione|Die Enttäuschung war groß.|Emozioni
        die Wut|rabbia|Die Wut hilft nicht.|Emozioni
        die Geduld|pazienza|Geduld ist wichtig beim Lernen.|Emozioni
        das Vertrauen|fiducia|Vertrauen muss man aufbauen.|Emozioni
        die Motivation|motivazione|Meine Motivation ist hoch.|Emozioni
        der Stress|stress|Stress macht krank.|Emozioni
        die Entspannung|rilassamento|Entspannung ist wichtig.|Emozioni
        die Reise|viaggio|Die Reise war schön.|Viaggio
        die Unterkunft|alloggio|Die Unterkunft ist günstig.|Viaggio
        die Ankunft|arrivo|Die Ankunft ist um acht.|Viaggio
        die Abfahrt|partenza|Die Abfahrt ist morgen.|Viaggio
        der Aufenthalt|soggiorno|Der Aufenthalt dauert drei Tage.|Viaggio
        die Sehenswürdigkeit|attrazione|Die Sehenswürdigkeit ist berühmt.|Viaggio
        die Fahrkarte|biglietto|Ich kaufe eine Fahrkarte.|Viaggio
        der Anschluss|coincidenza|Der Anschluss ist knapp.|Viaggio
        die Verspätung|ritardo|Der Zug hat Verspätung.|Viaggio
        das Gepäck|bagaglio|Mein Gepäck ist schwer.|Viaggio
    """.trimIndent()

    val tutte: List<Voce> = grezzo.lines().mapNotNull { riga ->
        val parti = riga.split("|")
        if (parti.size != 4) return@mapNotNull null
        val tedesco = parti[0].trim()
        val italiano = parti[1].trim()
        if (tedesco.isEmpty() || italiano.isEmpty()) null
        else Voce(
            tedesco = tedesco,
            italiano = italiano,
            esempio = parti[2].trim(),
            tema = parti[3].trim()
        )
    }

    val temi: List<String> get() = tutte.map { it.tema }.distinct()

    fun perTema(tema: String): List<Voce> = tutte.filter { it.tema.equals(tema, ignoreCase = true) }

    fun cerca(query: String): List<Voce> {
        val q = query.trim()
        if (q.isEmpty()) return tutte
        return tutte.filter {
            it.tedesco.contains(q, ignoreCase = true) ||
                it.italiano.contains(q, ignoreCase = true)
        }
    }

    /** Articolo separato dalla parola (per il vocabolario). */
    fun articoloDi(tedesco: String): String? {
        val primo = tedesco.substringBefore(" ").lowercase()
        return if (primo in setOf("der", "die", "das")) primo else null
    }

    fun parolaSenzaArticolo(tedesco: String): String =
        if (articoloDi(tedesco) != null) tedesco.substringAfter(" ") else tedesco
}
