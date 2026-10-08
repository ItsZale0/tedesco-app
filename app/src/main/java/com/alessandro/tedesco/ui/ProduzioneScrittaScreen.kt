package com.alessandro.tedesco.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.ui.theme.Spaziature

data class EsercizioScrittura(
    val id: String,
    val titolo: String,
    val istruzioni: String,
    val esempio: String,
    val suggerimenti: List<String>
)

val ESERCIZI_SCRITTURA = listOf(
    EsercizioScrittura(
        id = "sc1",
        titolo = "Presentazione personale",
        istruzioni = "Scrivi 3-4 frasi su di te: come ti chiami, da dove vivi, cosa fai.",
        esempio = "Ich heiße Alessandro. Ich wohne in Bozen. Ich bin Physiotherapeut. Ich lerne Deutsch.",
        suggerimenti = listOf("Nome", "Città", "Lavoro", "Hobby")
    ),
    EsercizioScrittura(
        id = "sc2",
        titolo = "La tua giornata",
        istruzioni = "Descrivi la tua giornata tipica: quando ti alzi, cosa fai, quando lavori.",
        esempio = "Ich stehe um 7 Uhr auf. Ich frühstücke um 8 Uhr. Ich arbeite von 9 bis 17 Uhr.",
        suggerimenti = listOf("Ore", "Attività", "Lavoro", "Tempo libero")
    ),
    EsercizioScrittura(
        id = "sc3",
        titolo = "Un messaggio al collega",
        istruzioni = "Scrivi un messaggio breve a un collega per chiedere un favore.",
        esempio = "Hallo Marco, kannst du mir bitte helfen? Ich habe eine Frage. Danke!",
        suggerimenti = listOf("Saluto", "Richiesta", "Ringraziamento")
    ),
    EsercizioScrittura(
        id = "sc4",
        titolo = "Descrivi il tuo studio",
        istruzioni = "Descrivi il tuo studio di fisioterapia: dove è, cosa c'è dentro, come è organizzato.",
        esempio = "Mein Studio ist in der Stadtmitte. Es gibt ein Behandlungszimmer und ein Wartezimmer.",
        suggerimenti = listOf("Posizione", "Stanze", "Attrezzatura", "Personale")
    ),
    EsercizioScrittura(
        id = "sc5",
        titolo = "Email formale",
        istruzioni = "Scrivi un'email formale per chiedere un appuntamento.",
        esempio = "Sehr geehrte Frau Müller, ich möchte einen Termin vereinbaren. Vielen Dank.",
        suggerimenti = listOf("Oggetto", "Saluto formale", "Richiesta", "Chiusura")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduzioneScrittaScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit
) {
    var esercizioSelezionato by remember { mutableStateOf<EsercizioScrittura?>(null) }
    var testoUtente by remember { mutableStateOf("") }
    var mostraCorrezione by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produzione Scritta") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        if (esercizioSelezionato == null) {
            // Lista esercizi
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spaziature.md),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                item {
                    Text(
                        text = "Scegli un esercizio di scrittura",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Spaziature.md)
                    )
                }

                items(ESERCIZI_SCRITTURA) { esercizio ->
                    Card(
                        onClick = { esercizioSelezionato = esercizio },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spaziature.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(Spaziature.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = esercizio.titolo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = esercizio.istruzioni,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Schermata esercizio
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spaziature.md)
            ) {
                // Header esercizio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = esercizioSelezionato!!.titolo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spaziature.sm))
                        Text(
                            text = esercizioSelezionato!!.istruzioni,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Esempio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = "Esempio:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = esercizioSelezionato!!.esempio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Suggerimenti
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = "Suggerimenti:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        esercizioSelezionato!!.suggerimenti.forEach { suggerimento ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text("• ", style = MaterialTheme.typography.bodyMedium)
                                Text(suggerimento, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Campo di testo
                OutlinedTextField(
                    value = testoUtente,
                    onValueChange = { testoUtente = it },
                    label = { Text("Scrivi qui in tedesco...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )

                Spacer(Modifier.height(Spaziature.md))

                // Pulsanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spaziature.md)
                ) {
                    OutlinedButton(
                        onClick = { esercizioSelezionato = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Indietro")
                    }
                    Button(
                        onClick = { mostraCorrezione = true },
                        modifier = Modifier.weight(1f),
                        enabled = testoUtente.isNotBlank()
                    ) {
                        Text("Verifica")
                    }
                }

                // Correzione
                if (mostraCorrezione) {
                    Spacer(Modifier.height(Spaziature.md))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(Spaziature.sm))
                                Text(
                                    text = "Correzione automatica",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                text = "Il tuo testo è stato salvato. Riceverai una correzione dettagliata dal tutor.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                text = "Suggerimento: rileggi il testo ad alta voce per trovare errori.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
