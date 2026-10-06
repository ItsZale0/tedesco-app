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
import com.alessandro.tedesco.data.GrammaticaB1
import com.alessandro.tedesco.data.CategoriaGrammatica
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammaticaScreen(vm: TedescoViewModel) {
    var esercizi by remember { mutableStateOf(GrammaticaB1.eserciziCasuali(5)) }
    var indice by remember { mutableStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableStateOf(0) }
    var completato by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grammatica", style = MaterialTheme.typography.titleLarge) }
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
                    val esercizio = esercizi[indice]

                    // Categoria
                    AssistChip(
                        onClick = { },
                        label = { Text(esercizio.categoria.name.replace("_", " ")) }
                    )

                    // Domanda
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = esercizio.domanda,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(Spaziature.md)
                        )
                    }

                    // Opzioni
                    esercizio.opzioni.forEachIndexed { index, opzione ->
                        val selezionato = rispostaSelezionata == index
                        val colore = when {
                            risultato == null -> MaterialTheme.colorScheme.surface
                            index == esercizio.rispostaCorretta -> MaterialTheme.colorScheme.primary
                            selezionato -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            onClick = {
                                if (risultato == null) {
                                    rispostaSelezionata = index
                                    risultato = index == esercizio.rispostaCorretta
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
                                Text(text = esercizio.spiegazione)
                                Text(
                                    text = esercizio.esempio,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (indice < esercizi.size - 1) {
                                    indice++
                                    rispostaSelezionata = null
                                    risultato = null
                                } else {
                                    completato = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (indice < esercizi.size - 1) "Prossimo" else "Vedi risultato")
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
                                text = "Punteggio: $punteggio/${esercizi.size}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "${(punteggio * 100 / esercizi.size)}%",
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }

                    Button(
                        onClick = {
                            esercizi = GrammaticaB1.eserciziCasuali(5)
                            indice = 0
                            rispostaSelezionata = null
                            risultato = null
                            punteggio = 0
                            completato = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nuovi esercizi")
                    }
                }
            }
        }
    }
}
