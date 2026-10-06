package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraduttoreScreen(vm: TedescoViewModel) {
    val scope = rememberCoroutineScope()
    var testo by remember { mutableStateOf("") }
    var traduzione by remember { mutableStateOf("") }
    var caricamento by remember { mutableStateOf(false) }
    var errore by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traduttore", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spaziaturaSchermo()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    "Traduci parole e frasi dal tedesco all'italiano",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = testo,
                    onValueChange = { testo = it },
                    label = { Text("Testo in tedesco") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Button(
                    onClick = {
                        if (testo.isNotBlank()) {
                            caricamento = true
                            errore = null
                            scope.launch {
                                try {
                                    val risultato = withContext(Dispatchers.IO) {
                                        traduci(testo)
                                    }
                                    traduzione = risultato
                                } catch (e: Exception) {
                                    errore = "Errore: ${e.message}"
                                } finally {
                                    caricamento = false
                                }
                            }
                        }
                    },
                    enabled = testo.isNotBlank() && !caricamento,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Translate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (caricamento) "Traduzione..." else "Traduci")
                }

                if (errore != null) {
                    Text(
                        errore!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (traduzione.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Traduzione",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                traduzione,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = {
                            vm.aggiungiParolaCustom(
                                german = testo,
                                italian = traduzione,
                                example = "",
                                article = null,
                                pronunciation = null,
                                lesson = 0,
                                tags = "tradotto"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Aggiungi al vocabolario")
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun traduci(testo: String): String {
    val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=de&tl=it&dt=t&q=" +
            URLEncoder.encode(testo, "UTF-8")
    val risultato = java.net.URL(url).readText()
    // Parsing semplice: [[["traduzione","originale",...],...],...]
    val regex = Regex("\"([^\"]+)\"")
    val matches = regex.findAll(risultato).map { it.groupValues[1] }.toList()
    return matches.take(4).joinToString("")
}
