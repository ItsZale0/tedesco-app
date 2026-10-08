package com.alessandro.tedesco.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.ui.theme.Spaziature

data class TappaProgressione(
    val livello: LivelloCEFR,
    val titolo: String,
    val descrizione: String,
    val obiettivi: List<String>,
    val stato: String // "completato", "in_corso", "bloccato"
)

val TAPPE_PROGRESSIONE = listOf(
    TappaProgressione(
        livello = LivelloCEFR.A0,
        titolo = "Fondamenta",
        descrizione = "Impara le basi del tedesco: saluti, numeri, parole essenziali",
        obiettivi = listOf(
            "Presentarti e chiedere come sta qualcuno",
            "Contare fino a 20",
            "Conoscere 50 parole di base",
            "Capire frasi semplici e lente"
        ),
        stato = "completato"
    ),
    TappaProgressione(
        livello = LivelloCEFR.A1,
        titolo = "Prime conversazioni",
        descrizione = "Inizia a conversare su argomenti semplici della vita quotidiana",
        obiettivi = listOf(
            "Parlare di te, della tua famiglia e del tuo lavoro",
            "Fare domande semplici e rispondere",
            "Conoscere 200 parole",
            "Capire istruzioni semplici e chiare"
        ),
        stato = "in_corso"
    ),
    TappaProgressione(
        livello = LivelloCEFR.A2,
        titolo = "Vita quotidiana",
        descrizione = "Gestisci situazioni quotidiane: spesa, ristorante, trasporti",
        obiettivi = listOf(
            "Conversare al ristorante e negozi",
            "Chiedere e capire indicazioni",
            "Parlare del tempo e dei programmi",
            "Conoscere 500 parole",
            "Scrivere messaggi semplici"
        ),
        stato = "bloccato"
    ),
    TappaProgressione(
        livello = LivelloCEFR.B1,
        titolo = "Indipendenza",
        descrizione = "Sii indipendente in tedesco: lavoro, studio, vita sociale",
        obiettivi = listOf(
            "Partecipare a conversazioni sul lavoro",
            "Scrivere email e messaggi formali",
            "Capire testi semplici e articoli",
            "Conoscere 1000 parole",
            "Sostenere un colloquio di lavoro"
        ),
        stato = "bloccato"
    ),
    TappaProgressione(
        livello = LivelloCEFR.B2,
        titolo = "Padronanza",
        descrizione = "Padroneggia il tedesco: contesti professionali e accademici",
        obiettivi = listOf(
            "Conversare fluentemente su argomenti complessi",
            "Scrivere testi strutturati e formali",
            "Capire discorsi e presentazioni",
            "Conoscere 2000 parole",
            "Sostenere una presentazione di lavoro"
        ),
        stato = "bloccato"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressioneScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit
) {
    val livelloAttuale = LivelloCEFR.A0 // TODO: collegare al ViewModel

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Percorso di Apprendimento") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spaziature.md),
            verticalArrangement = Arrangement.spacedBy(Spaziature.md)
        ) {
            // Header con livello attuale
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = "Il tuo percorso verso B2",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spaziature.sm))
                        Text(
                            text = "Livello attuale: ${livelloAttuale.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(Spaziature.sm))
                        LinearProgressIndicator(
                            progress = when (livelloAttuale) {
                                LivelloCEFR.A0 -> 0.0f
                                LivelloCEFR.A1 -> 0.25f
                                LivelloCEFR.A2 -> 0.5f
                                LivelloCEFR.B1 -> 0.75f
                                LivelloCEFR.B2 -> 1.0f
                                else -> 0.0f
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Tappe
            items(TAPPE_PROGRESSIONE) { tappa ->
                val isAttuale = tappa.livello == livelloAttuale
                val isCompletato = tappa.stato == "completato"
                val isBloccato = tappa.stato == "bloccato"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isAttuale -> MaterialTheme.colorScheme.secondaryContainer
                            isCompletato -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tappa.livello.name} - ${tappa.titolo}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tappa.descrizione,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            when {
                                isCompletato -> Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Completato",
                                    tint = Color(0xFF4CAF50)
                                )
                                isAttuale -> Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "In corso",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                else -> Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = "Bloccato",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isAttuale || isCompletato) {
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                text = "Obiettivi:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            tappa.obiettivi.forEach { obiettivo ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = obiettivo,
                                        style = MaterialTheme.typography.bodyMedium
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
