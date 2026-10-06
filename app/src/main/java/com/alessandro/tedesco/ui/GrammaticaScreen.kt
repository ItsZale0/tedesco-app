package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.URLEncoder
import com.alessandro.tedesco.data.GrammaticaB1
import com.alessandro.tedesco.data.CategoriaGrammatica
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammaticaScreen(vm: TedescoViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    var esercizi by remember(lezioneCorrente) { mutableStateOf(GrammaticaB1.eserciziPerLezione(lezioneCorrente, 5)) }
    var indice by remember { mutableStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var testoTraduzione by remember { mutableStateOf<String?>(null) }
    var traduzione by remember { mutableStateOf("") }
    var caricamentoTraduzione by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

                    // Domanda con traduttore
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = esercizio.domanda,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        testoTraduzione = esercizio.domanda
                                        caricamentoTraduzione = true
                                        scope.launch {
                                            try {
                                                traduzione = withContext(Dispatchers.IO) {
                                                    traduci(esercizio.domanda)
                                                }
                                            } catch (e: Exception) {
                                                traduzione = "Errore: ${e.message}"
                                            } finally {
                                                caricamentoTraduzione = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Translate,
                                        contentDescription = "Traduci",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (testoTraduzione == esercizio.domanda && traduzione.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = traduzione,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Opzioni con traduttore
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
                            Row(
                                modifier = Modifier.padding(Spaziature.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opzione,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        testoTraduzione = opzione
                                        caricamentoTraduzione = true
                                        scope.launch {
                                            try {
                                                traduzione = withContext(Dispatchers.IO) {
                                                    traduci(opzione)
                                                }
                                            } catch (e: Exception) {
                                                traduzione = "Errore: ${e.message}"
                                            } finally {
                                                caricamentoTraduzione = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Translate,
                                        contentDescription = "Traduci",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
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
                            esercizi = GrammaticaB1.eserciziPerLezione(lezioneCorrente, 5)
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

private fun traduci(testo: String): String {
    val url = "https://api.mymemory.translated.net/get?q=" +
            URLEncoder.encode(testo, "UTF-8") + "&langpair=de|it"
    val risultato = java.net.URL(url).readText()
    val json = kotlinx.serialization.json.Json.parseToJsonElement(risultato).jsonObject
    val translated = json["responseData"]?.jsonObject?.get("translatedText")?.jsonPrimitive?.content
        ?: throw IOException("Traduzione non disponibile")
    if (translated.startsWith("MYMEMORY WARNING") || translated.startsWith("QUERY LENGTH LIMIT")) {
        throw IOException("Limite di traduzione raggiunto, riprova più tardi")
    }
    return translated
}
