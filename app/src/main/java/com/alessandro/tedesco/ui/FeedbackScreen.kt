package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.FeedbackEntry
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

/**
 * Schermata di feedback: l'utente invia le sue risposte in tedesco
 * e riceve la correzione dal tutor AI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onIndietro: () -> Unit,
    onInviaRisposta: (String) -> Unit,
    risposte: List<FeedbackEntry>,
    correzioneInCorso: Boolean,
    apiKey: String = ""
) {
    var testoRisposta by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Correzione risposte") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
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

            // Istruzioni
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Come funziona",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "1. Scrivi la tua risposta in tedesco\n" +
                               "2. Invia e ricevi la correzione\n" +
                               "3. Gli errori vengono registrati per il ripasso",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Campo di input
            OutlinedTextField(
                value = testoRisposta,
                onValueChange = { testoRisposta = it },
                label = { Text("La tua risposta in tedesco") },
                placeholder = { Text("Es: Ich bin 20 Jahre alt") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = apiKey.isNotBlank()
            )

            Spacer(Modifier.height(8.dp))

            // Pulsante invia
            Button(
                onClick = {
                    if (testoRisposta.isNotBlank()) {
                        onInviaRisposta(testoRisposta)
                        testoRisposta = ""
                    }
                },
                enabled = testoRisposta.isNotBlank() && !correzioneInCorso && apiKey.isNotBlank(),
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
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Invia e correggi")
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
