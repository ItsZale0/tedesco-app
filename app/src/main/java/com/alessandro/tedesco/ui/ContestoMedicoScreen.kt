package com.alessandro.tedesco.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.LESSICO_MEDICO
import com.alessandro.tedesco.data.SCENARI_MEDICI
import com.alessandro.tedesco.ui.theme.Spaziature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestoMedicoScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit
) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Lessico", "Scenari", "Frasi")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contesto Medico") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spaziature.md)
        ) {
            // Tab bar
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(Modifier.height(Spaziature.md))

            // Contenuto tab
            AnimatedContent(
                targetState = tab,
                label = "contenuto",
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                    fadeOut() + slideOutHorizontally()
                }
            ) { targetTab ->
                when (targetTab) {
                    0 -> TabLessico()
                    1 -> TabScenari()
                    2 -> TabFrasi()
                }
            }
        }
    }
}

@Composable
private fun TabLessico() {
    val categorie = listOf("Anatomia", "Sintomi", "Trattamento", "Attrezzatura", "Ufficio")
    var categoriaSelezionata by remember { mutableStateOf(categorie[0]) }

    Column {
        // Filtro categorie
        ScrollableTabRow(
            selectedTabIndex = categorie.indexOf(categoriaSelezionata),
            edgePadding = Spaziature.md
        ) {
            categorie.forEach { categoria ->
                Tab(
                    selected = categoria == categoriaSelezionata,
                    onClick = { categoriaSelezionata = categoria },
                    text = { Text(categoria) }
                )
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        // Lista parole
        val paroleFiltrate = LESSICO_MEDICO.filter {
            when (categoriaSelezionata) {
                "Anatomia" -> it.categoria == "anatomia"
                "Sintomi" -> it.categoria == "sintomi"
                "Trattamento" -> it.categoria == "trattamento"
                "Attrezzatura" -> it.categoria == "attrezzatura"
                "Ufficio" -> it.categoria == "ufficio"
                else -> true
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            items(paroleFiltrate) { parola ->
                var expandida by remember { mutableStateOf(false) }
                Card(
                    onClick = { expandida = !expandida },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${parola.article} ${parola.german}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = parola.italian,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                if (expandida) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null
                            )
                        }
                        AnimatedVisibility(visible = expandida) {
                            Column(modifier = Modifier.padding(top = Spaziature.sm)) {
                                Text(
                                    text = parola.example,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (parola.pronunciation.isNotEmpty()) {
                                    Text(
                                        text = "🔊 ${parola.pronunciation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabScenari() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spaziature.md)
    ) {
        items(SCENARI_MEDICI) { scenario ->
            var expandido by remember { mutableStateOf(false) }
            Card(
                onClick = { expandido = !expandido },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spaziature.md)) {
                    Text(
                        text = scenario.titolo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scenario.descrizione,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedVisibility(visible = expandido) {
                        Column(modifier = Modifier.padding(top = Spaziature.sm)) {
                            scenario.frasiChiave.forEachIndexed { index, frase ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = frase,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = scenario.traduzioni[index],
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (index < scenario.frasiChiave.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabFrasi() {
    val frasi = listOf(
        Triple("Guten Morgen, wie kann ich helfen?", "Buongiorno, come posso aiutarla?", "saluto"),
        Triple("Wo haben Sie Schmerzen?", "Dove ha dolore?", "domanda"),
        Triple("Bitte setzen Sie sich hier.", "Per favore si sieda qui.", "istruzione"),
        Triple("Ich werde Sie jetzt untersuchen.", "Ora la esaminerò.", "spiegazione"),
        Triple("Das wird ein bisschen wehtuen.", "Questo farà un po' male.", "avviso"),
        Triple("Atmen Sie tief ein.", "Respiri profondamente.", "istruzione"),
        Triple("Sehr gut, weiter so!", "Molto bene, così continua!", "incoraggiamento"),
        Triple("Haben Sie noch Fragen?", "Ha altre domande?", "chiusura"),
        Triple("Bis nächste Woche!", "Alla prossima settimana!", "saluto"),
        Triple("Gute Besserung!", "Guarisca bene!", "saluto")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
    ) {
        items(frasi) { (tedesco, italiano, categoria) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spaziature.md)) {
                    Text(
                        text = tedesco,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = italiano,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
