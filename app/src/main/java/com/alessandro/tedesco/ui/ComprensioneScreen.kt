package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.QuizData
import com.alessandro.tedesco.data.local.DomandaTest
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprensioneScreen(vm: TedescoViewModel) {
    val domande = remember { QuizData.domandeComprensione }
    var indice by remember { mutableIntStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableIntStateOf(0) }
    var completato by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comprensione", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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

                    // Progress
                    Text(
                        text = "Domanda ${indice + 1}/${domande.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    if (risultato == true) {
                                        punteggio++
                                    }
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
                    val punteggioPct = if (domande.isNotEmpty()) (punteggio * 100 / domande.size) else 0
                    val errori = domande.size - punteggio

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
                                text = "$punteggioPct%",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Errori: $errori",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Salva risultato
                    LaunchedEffect(completato) {
                        if (completato) {
                            vm.salvaTestComprensione(
                                punteggio = punteggioPct.toFloat(),
                                errori = errori,
                                totale = domande.size
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            indice = 0
                            rispostaSelezionata = null
                            risultato = null
                            punteggio = 0
                            completato = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nuovo quiz")
                    }
                }
            }
        }
    }
}
