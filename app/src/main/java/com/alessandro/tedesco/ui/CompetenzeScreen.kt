package com.alessandro.tedesco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.CalcoloCompetenze
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.Statistiche
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetenzeScreen(vm: TedescoViewModel, profilo: ProfiloUtente? = null) {
    val stats by vm.statistiche.collectAsStateWithLifecycle(null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competenze", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        val statsCorrenti = stats
        if (statsCorrenti == null) {
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Caricamento…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val punteggi = statsCorrenti.punteggiCompetenze
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spaziaturaSchermo()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.widthIn(max = dimensioneContenuto())) {
                    Spacer(Modifier.height(8.dp))

                    // Header: CEFR level badge
                    if (punteggi != null) {
                        HeaderLivello(punteggi, profilo)
                        Spacer(Modifier.height(20.dp))
                    }

                    // 4 competence cards
                    if (punteggi != null) {
                        CompetenzaCard(
                            label = "Lessico",
                            punteggio = punteggi.lessico,
                            colore = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))

                        CompetenzaCard(
                            label = "Grammatica",
                            punteggio = punteggi.grammatica,
                            colore = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(12.dp))

                        CompetenzaCard(
                            label = "Comprensione",
                            punteggio = punteggi.comprensione,
                            colore = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(12.dp))

                        CompetenzaCard(
                            label = "Produzione",
                            punteggio = punteggi.produzione,
                            colore = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    // Info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Come si calcolano i punteggi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "• Lessico: parole mature (30%)\n• Grammatica: accuratezza ripassi (30%)\n• Comprensione: accuratezza (20%)\n• Produzione: accuratezza (20%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderLivello(punteggi: CalcoloCompetenze.PunteggiCompetenze, profilo: ProfiloUtente? = null) {
    val livelloIniziale = profilo?.config?.livelloIniziale?.label
    val livelloAttuale = punteggi.livelloComplessivo.label
    val mostraIniziale = livelloIniziale != null && livelloIniziale != "A0"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Livello complessivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = livelloAttuale,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = punteggi.livelloComplessivo.descrizione,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (mostraIniziale) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Livello iniziale: $livelloIniziale",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun CompetenzaCard(
    label: String,
    punteggio: Float,
    colore: Color
) {
    val punteggioInt = punteggio.toInt().coerceIn(0, 100)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$punteggioInt%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colore
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { punteggio / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = colore,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
