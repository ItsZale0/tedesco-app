package com.alessandro.tedesco.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: TedescoViewModel) {
    val parole: List<WordEntity> by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioni: List<Int> by vm.lezioni.collectAsStateWithLifecycle(emptyList())
    val daRipassare: Int by vm.daRipassare.collectAsStateWithLifecycle(0)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistiche", style = MaterialTheme.typography.titleLarge) }) }
    ) { inner ->
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

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Parole", "${parole.size}", Modifier.weight(1f))
                    StatCard("Lezioni", "${lezioni.size}", Modifier.weight(1f))
                    StatCard("Oggi", "$daRipassare", Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                Text("Parole per lezione", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                val perLezione = lezioni.map { l: Int -> l to parole.count { w: WordEntity -> w.lesson == l } }
                if (perLezione.isEmpty()) {
                    Text(
                        "Nessun dato ancora",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    BarChartGrafico(
                        dati = perLezione,
                        color = MaterialTheme.colorScheme.primary,
                        colorSecondario = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, valore: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valore,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Grafico a barre disegnato con Compose Canvas: nessuna libreria a pagamento. */
@Composable
private fun BarChartGrafico(
    dati: List<Pair<Int, Int>>,
    color: Color,
    colorSecondario: Color
) {
    val maxVal = dati.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val n = dati.size.coerceAtLeast(1)
        val spazio = 8f
        val larghezzaBarra = (size.width - spazio * (n + 1)) / n

        dati.forEachIndexed { i, (lezione, conteggio) ->
            val altezzaBarra = (conteggio.toFloat() / maxVal) * (size.height - 24f)
            val x = spazio + i * (larghezzaBarra + spazio)
            val y = size.height - altezzaBarra - 18f

            drawRoundRect(
                color = if (i == dati.lastIndex) color else colorSecondario,
                topLeft = Offset(x, y),
                size = Size(larghezzaBarra.coerceAtLeast(2f), altezzaBarra.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }

    // etichette delle lezioni
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dati.forEach { (lezione, _) ->
            Text(
                text = "L${lezione.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
