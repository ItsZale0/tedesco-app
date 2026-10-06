package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.TestB1
import com.alessandro.tedesco.data.TestB1Data
import com.alessandro.tedesco.data.SezioneTestB1
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestB1Screen(vm: TedescoViewModel) {
    var sezione by remember { mutableStateOf(SezioneTestB1.LESEN) }
    var domande by remember { mutableStateOf(TestB1Data.domandePerSezione(SezioneTestB1.LESEN)) }
    var indice by remember { mutableStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test - ${sezione.name}", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spaziaturaSchermo())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                if (!completato) {
                    val domanda = domande[indice]
                    
                    // Sezione
                    AssistChip(
                        onClick = { },
                        label = { Text(sezione.name) }
                    )
                    
                    // Domanda
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = domanda.domanda,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(Spaziature.md)
                        )
                    }
                    
                    // Opzioni
                    domanda.opzioni.forEachIndexed { index, opzione ->
                        val selezionato = rispostaSelezionata == index
                        val colore = when {
                            risultato == null -> MaterialTheme.colorScheme.surface
                            index == domanda.rispostaCorretta -> MaterialTheme.colorScheme.primary
                            selezionato -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surface
                        }
                        
                        Card(
                            onClick = {
                                if (risultato == null) {
                                    rispostaSelezionata = index
                                    risultato = index == domanda.rispostaCorretta
                                    if (risultato == true) punteggio++
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colore)
                        ) {
                            Text(
                                text = opzione,
                                modifier = Modifier.padding(Spaziature.md)
                            )
                        }
                    }
                    
                    // Spiegazione
                    if (risultato != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (risultato == true) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(Spaziature.md)) {
                                Text(
                                    text = if (risultato == true) "✓ Corretto!" else "✗ Sbagliato",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = domanda.spiegazione)
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (indice < domande.size - 1) {
                                    indice++
                                    rispostaSelezionata = null
                                    risultato = null
                                } else {
                                    completato = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (indice < domande.size - 1) "Prossimo" else "Vedi risultato")
                        }
                    }
                } else {
                    // Risultato finale
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Spaziature.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Punteggio: $punteggio/${domande.size}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "${(punteggio * 100 / domande.size)}%",
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                    
                    Button(
                        onClick = {
                            sezione = when (sezione) {
                                SezioneTestB1.LESEN -> SezioneTestB1.HOEREN
                                SezioneTestB1.HOEREN -> SezioneTestB1.SCHREIBEN
                                SezioneTestB1.SCHREIBEN -> SezioneTestB1.SPRECHEN
                                SezioneTestB1.SPRECHEN -> SezioneTestB1.LESEN
                            }
                            domande = TestB1Data.domandePerSezione(sezione)
                            indice = 0
                            rispostaSelezionata = null
                            risultato = null
                            punteggio = 0
                            completato = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Prossima sezione")
                    }
                }
            }
        }
    }
}
