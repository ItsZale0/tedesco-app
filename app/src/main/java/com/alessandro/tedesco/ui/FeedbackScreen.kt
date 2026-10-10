package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.FeedbackEntry
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import com.alessandro.tedesco.data.GrammaticaB1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onIndietro: () -> Unit,
    onInviaRisposta: (String) -> Unit,
    risposte: List<FeedbackEntry>,
    correzioneInCorso: Boolean,
    apiKey: String = "",
    lezioneCorrente: Int = 1
) {
    var testoRisposta by remember { mutableStateOf("") }
    var esercizioSelezionato by remember { mutableStateOf<String?>(null) }

    // Esercizi di grammatica per la lezione corrente
    val eserciziLezione = remember(lezioneCorrente) {
        GrammaticaB1.eserciziPerLezione(lezioneCorrente)
    }

    ScreenScaffold("Correzione risposte", onBack = onIndietro) {

            // Avviso se manca la chiave API
            if (apiKey.isBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Per correggere le risposte serve una chiave API OpenRouter. " +
                            "Vai in Profilo → Tutor AI e inseriscila.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Sezione: Esercizi della lezione
            if (eserciziLezione.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Esercizi lezione $lezioneCorrente",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Scegli un esercizio, scrivi la tua risposta in tedesco e invia per la correzione.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Lista esercizi
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eserciziLezione) { esercizio ->
                        val selezionato = esercizioSelezionato == esercizio.id
                        Card(
                            onClick = { esercizioSelezionato = esercizio.id },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selezionato)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Esercizio ${esercizio.id} (L${esercizio.lezione})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = esercizio.domanda,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )
                                }
                                if (selezionato) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Selezionato",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Testo dell'esercizio selezionato
                esercizioSelezionato?.let { id ->
                    val esercizio = eserciziLezione.find { it.id == id }
                    esercizio?.let { e ->
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Esercizio selezionato: ${e.domanda}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Campo di input libero
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Oppure scrivi liberamente in tedesco",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = testoRisposta,
                        onValueChange = { testoRisposta = it },
                        label = { Text("La tua risposta in tedesco") },
                        placeholder = { Text("Es: Ich bin 20 Jahre alt") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (testoRisposta.isNotBlank()) {
                                onInviaRisposta(testoRisposta)
                                testoRisposta = ""
                            }
                        },
                        enabled = testoRisposta.isNotBlank() && !correzioneInCorso,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (correzioneInCorso) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Correzione in corso...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Invia e correggi")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Storico correzioni
            if (risposte.isNotEmpty()) {
                Text(
                    text = "Storico correzioni",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(risposte) { risposta ->
                        FeedbackCard(risposta = risposta)
                    }
                }
            }

    }
}

@Composable
private fun FeedbackCard(risposta: FeedbackEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (risposta.corretto)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = risposta.testo,
                style = MaterialTheme.typography.bodyMedium
            )
            if (risposta.corretto && risposta.correzione != null) {
                Spacer(Modifier.height(4.dp))
                MarkdownText(
                    markdown = risposta.correzione,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
