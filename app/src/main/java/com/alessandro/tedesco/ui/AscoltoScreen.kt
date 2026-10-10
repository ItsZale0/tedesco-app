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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.AscoltoData
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.EsercizioAscolto
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.data.local.toEsercizioAscolto
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AscoltoScreen(vm: TedescoViewModel, profilo: ProfiloUtente? = null) {
    val livelli = remember { AscoltoData.livelliDisponibili(profilo) }
    var livelloSelezionato by remember { mutableStateOf(livelli.firstOrNull() ?: "A1") }
    var esercizi by remember { mutableStateOf(AscoltoData.eserciziPerLivello(livelloSelezionato, profilo)) }
    var indice by remember { mutableIntStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableIntStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var mostraTraduzione by remember { mutableStateOf(false) }
    val eserciziGen by vm.eserciziGenAscolto.collectAsStateWithLifecycle()
    var generaInCorso by remember { mutableStateOf(false) }
    var erroreGen by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    // Carica esercizi generati al primo entrata
    LaunchedEffect(Unit) { vm.caricaEserciziGenerati(); vm.preGeneraInBackground(TipoEsercizio.ASCOLTO) }

    // Ricostruisce la lista quando cambia livello o arrivano nuovi generati
    LaunchedEffect(livelloSelezionato, eserciziGen) {
        esercizi = AscoltoData.eserciziPerLivello(livelloSelezionato, profilo) +
            eserciziGen.filter { it.livello == livelloSelezionato }
                .map { it.toEsercizioAscolto() }
        if (indice >= esercizi.size) indice = (esercizi.size - 1).coerceAtLeast(0)
    }

    ScreenScaffold("Ascolto (Hörverstehen)", scrollable = false) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                // Selettore livello
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    livelli.forEach { livello ->
                        val selezionato = livello == livelloSelezionato
                        Card(
                            onClick = { livelloSelezionato = livello },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selezionato)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = livello,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selezionato) FontWeight.Bold else FontWeight.Normal,
                                color = if (selezionato)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!completato && esercizi.isNotEmpty()) {
                    val esercizio = esercizi[indice]

                    // Progress
                    Text(
                        text = "Esercizio ${indice + 1}/${esercizi.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Card con la frase da ascoltare
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Spaziature.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ascolta la frase in tedesco:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { ttsHelper.speak(esercizio.fraseTedesca) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Ascolta"
                                )
                                Spacer(Modifier.padding(4.dp))
                                Text("Ascolta")
                            }
                            if (mostraTraduzione) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = esercizio.traduzioneItaliana,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = { mostraTraduzione = true }
                                ) {
                                    Text("Mostra traduzione", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

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
                                    if (risultato == true) {
                                        punteggio++
                                    }
                                    vm.segnaEsercizioCompletato(TipoEsercizio.ASCOLTO, esercizio.id)
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
                                Spacer(Modifier.height(4.dp))
                                Text(text = esercizio.spiegazione)
                            }
                        }

                        val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + esercizio.id
                        var prossimoIdx = indice + 1
                        while (prossimoIdx < esercizi.size && esercizi[prossimoIdx].id in idsCompletati) prossimoIdx++
                        Button(
                            onClick = {
                                if (prossimoIdx < esercizi.size) {
                                    indice = prossimoIdx
                                    rispostaSelezionata = null
                                    risultato = null
                                    mostraTraduzione = false
                                } else {
                                    completato = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (prossimoIdx < esercizi.size) "Prossimo" else "Vedi risultato")
                        }
                    }
                } else if (completato) {
                    // Risultato finale
                    val punteggioPct = if (esercizi.isNotEmpty()) (punteggio * 100 / esercizi.size) else 0
                    val errori = esercizi.size - punteggio

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
                            vm.salvaTestAscolto(
                                punteggio = punteggioPct.toFloat(),
                                errori = errori,
                                totale = esercizi.size,
                                livello = livelloSelezionato
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
                                    vm.generaEsercizi(TipoEsercizio.ASCOLTO, quanti = 5)
                                }.onSuccess {
                                    generaInCorso = false
                                    indice = 0
                                    rispostaSelezionata = null
                                    risultato = null
                                    punteggio = 0
                                    completato = false
                                    mostraTraduzione = false
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
                            mostraTraduzione = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nuovo quiz")
                    }
                } else {
                    // Stato vuoto
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Nessun esercizio disponibile per questo livello.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
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
