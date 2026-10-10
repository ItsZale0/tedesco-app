package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.data.local.toDomandaTest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.alessandro.tedesco.data.local.DomandaTest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduzioneScreen(vm: TedescoViewModel) {
    val eserciziGen by vm.eserciziGenProduzione.collectAsStateWithLifecycle()
    val statiche = remember { QuizData.domandeProduzione }
    var elenco by remember { mutableStateOf<List<DomandaTest>>(statiche + eserciziGen.map { it.toDomandaTest() }) }
    var indice by remember { mutableIntStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableIntStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var generaInCorso by remember { mutableStateOf(false) }
    var erroreGen by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.caricaEserciziGenerati(); vm.preGeneraInBackground(TipoEsercizio.PRODUZIONE) }
    LaunchedEffect(eserciziGen, completato) {
        if (completato) {
            elenco = statiche + eserciziGen.map { it.toDomandaTest() }
        }
    }

    ScreenScaffold("Produzione") {

            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                if (!completato) {
                    val domanda = elenco[indice]

                    // Progress
                    val totaleGen = eserciziGen.size
                    Text(
                        text = "Domanda ${indice + 1}/${elenco.size} · ${statiche.size} base + $totaleGen generati",
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
                                    vm.segnaEsercizioCompletato(TipoEsercizio.PRODUZIONE, domanda.id)
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
                                val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + domanda.id
                                var next = indice + 1
                                while (next < elenco.size && elenco[next].id in idsCompletati) next++
                                if (next < elenco.size) {
                                    indice = next
                                    rispostaSelezionata = null
                                    risultato = null
                                } else {
                                    completato = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + domanda.id
                            var next = indice + 1
                            while (next < elenco.size && elenco[next].id in idsCompletati) next++
                            Text(if (next < elenco.size) "Prossimo" else "Vedi risultato")
                        }
                    }
                } else {
                    // Risultato finale
                    val punteggioPct = if (elenco.isNotEmpty()) (punteggio * 100 / elenco.size) else 0
                    val errori = elenco.size - punteggio

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
                                text = "Punteggio: $punteggio/${elenco.size}",
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
                            vm.salvaTestProduzione(
                                punteggio = punteggioPct.toFloat(),
                                errori = errori,
                                totale = elenco.size
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                generaInCorso = true
                                erroreGen = null
                                runCatching {
                                    vm.generaEsercizi(TipoEsercizio.PRODUZIONE, quanti = 5)
                                }.onSuccess {
                                    generaInCorso = false
                                    indice = 0
                                    rispostaSelezionata = null
                                    risultato = null
                                    punteggio = 0
                                    completato = false
                                }.onFailure { e ->
                                    generaInCorso = false
                                    erroreGen = e.message ?: "Errore generazione"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !generaInCorso
                    ) {
                        if (generaInCorso) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp).width(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Generazione...")
                        } else {
                            Text("Genera altri 5")
                        }
                    }
                    if (erroreGen != null) {
                        Text(
                            text = erroreGen!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
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

    // Dialogo errore chiave API per generazione esercizi infiniti
    if (erroreGen?.contains("Chiave API") == true) {
        AlertDialog(
            onDismissRequest = { erroreGen = null },
            title = { Text("Chiave API necessaria") },
            text = { Text("Per generare esercizi infiniti inserisci una chiave OpenRouter gratuita in Profilo → Tutor AI.") },
            confirmButton = { TextButton(onClick = { erroreGen = null }) { Text("OK") } }
        )
    }
}
