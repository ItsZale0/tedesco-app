package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import com.alessandro.tedesco.data.remote.EsercizioGenerato
import com.alessandro.tedesco.data.remote.GeneratoreEsercizi
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.EsercizioAscolto
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.AscoltoData
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.data.local.toEsercizioAscolto
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AscoltoScreen(vm: TedescoViewModel, profilo: ProfiloUtente? = null, onFatto: () -> Unit = {}) {
    val livelli = remember { AscoltoData.livelliDisponibili(profilo) }
    var livelloSelezionato by remember { mutableStateOf(livelli.firstOrNull() ?: "A1") }
    var esercizi by remember { mutableStateOf<List<EsercizioAscolto>>(emptyList()) }
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

    // Batch SEMPRE fresco: mai il set statico "vecchio" (frasi fisse che non vanno
    // bene). Se la generazione fallisce si riusa l'ultimo batch AI non completato,
    // altrimenti si mostra "Riprova" — mai esercizi riciclati.
    var freschi by remember { mutableStateOf<List<EsercizioAscolto>>(emptyList()) }
    var tentativo by remember { mutableIntStateOf(0) }

    LaunchedEffect(tentativo) {
        erroreGen = null
        esercizi = emptyList()
        vm.caricaEserciziGenerati()
        val generati: List<EsercizioGenerato> = try {
            vm.generaEsercizi(TipoEsercizio.ASCOLTO, quanti = 10)
        } catch (e: Throwable) {
            // Throwable, non Exception: un errore del modello o della rete non deve
            // poter chiudere l'app.
            erroreGen = (e.localizedMessage ?: e.javaClass.simpleName)
            emptyList()
        }
        val lista: List<EsercizioAscolto> = if (generati.isNotEmpty()) {
            generati.map { esercizioGeneratoAAscolto(it) }
        } else {
            eserciziGen.filterNot { it.completato }.map { it.toEsercizioAscolto() }
        }
        freschi = lista
        esercizi = lista
        indice = 0
        rispostaSelezionata = null
        risultato = null
        completato = false
    }

    // Il selettore di livello filtra il batch; se il filtro svuota tutto (il
    // generatore usa etichette leggermente diverse) si mostra comunque il batch.
    LaunchedEffect(livelloSelezionato, freschi) {
        val filtrato = freschi.filter { it.livello.equals(livelloSelezionato, ignoreCase = true) }
        esercizi = if (filtrato.isNotEmpty()) filtrato else freschi
        if (indice >= esercizi.size) indice = (esercizi.size - 1).coerceAtLeast(0)
    }

    ScreenScaffold(
        title = "Ascolto (Hörverstehen)",
        scrollable = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
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
                                MaterialTheme.colorScheme.surfaceContainer
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

            if (!completato && esercizi.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Nessun esercizio generato", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            erroreGen ?: "Controlla la connessione e riprova.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { tentativo++ }, modifier = Modifier.fillMaxWidth()) {
                            Text("Riprova")
                        }
                    }
                }
            }

            if (!completato && esercizi.isNotEmpty() && indice in esercizi.indices) {
                val esercizio = esercizi[indice]

                // Progress
                Text(
                    text = "Esercizio ${indice + 1}/${esercizi.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Frase da ascoltare (con TTS, stessa frase sia che esista o no)
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
                            onClick = { ttsHelper.speak(esercizio.fraseTedesca ?: esercizio.domanda) },
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
                                text = esercizio.traduzioneItaliana ?: "",
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
                                if (risultato == true) punteggio++
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
                val eserciziTotali = if (esercizi.isNotEmpty()) esercizi.size else 10
                val punteggioPct = if (esercizi.isNotEmpty()) (punteggio * 100 / esercizi.size) else 0
                val errori = if (esercizi.isNotEmpty()) esercizi.size - punteggio else 0

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
                            text = "Punteggio: $punteggio/$eserciziTotali",
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

                LaunchedEffect(completato) {
                    if (completato) {
                        vm.salvaTestAscolto(
                            punteggio = punteggioPct.toFloat(),
                            errori = errori,
                            totale = eserciziTotali,
                            livello = livelloSelezionato
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onFatto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Fatto")
                }
            } else {
                // Stato vuoto (generazione finita)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Hai finito questa serie di esercizi.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    if (erroreGen?.contains("Chiave API") == true) {
        AlertDialog(
            onDismissRequest = { erroreGen = null },
            title = { Text("Chiave API necessaria") },
            text = { Text("Per generare esercizi di ascolto inserisci una chiave Groq gratuita in Profilo → Tutor AI.") },
            confirmButton = { TextButton(onClick = { erroreGen = null }) { Text("OK") } }
        )
    }
}


/** Conversione EsercizioGenerato (AI) -> EsercizioAscolto. */
fun esercizioGeneratoAAscolto(g: EsercizioGenerato): EsercizioAscolto = EsercizioAscolto(
    id = g.id,
    fraseTedesca = g.fraseTedesca ?: "",
    traduzioneItaliana = g.traduzioneItaliana ?: "",
    domanda = g.domanda,
    opzioni = g.opzioni,
    rispostaCorretta = g.rispostaCorretta.coerceIn(0, g.opzioni.lastIndex.coerceAtLeast(0)),
    spiegazione = g.spiegazione,
    livello = g.livello
)
