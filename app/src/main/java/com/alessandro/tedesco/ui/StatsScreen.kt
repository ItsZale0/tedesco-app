package com.alessandro.tedesco.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.Statistiche
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: TedescoViewModel) {
    val stats by vm.statistiche.collectAsStateWithLifecycle(null)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)

    ScreenScaffold("Statistiche") {
        val statsCorrenti = stats
        if (statsCorrenti == null) {
            // Stato di caricamento: spinner centrato
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.widthIn(max = dimensioneContenuto())) {
                    Spacer(Modifier.height(8.dp))

                    // 1. Header: nome profilo + badge livello
                    HeaderProfilo(profilo, statsCorrenti)

                    Spacer(Modifier.height(20.dp))

                    // 2. Card progresso B1
                    CardProgressoB1(statsCorrenti)

                    Spacer(Modifier.height(16.dp))

                    // 3. Riga di 3 StatCard compatte
                    RigaStatCardCompatte(statsCorrenti)

                    Spacer(Modifier.height(24.dp))

                    // 4. Sezione stato delle parole
                    SezioneStatoParole(statsCorrenti)

                    Spacer(Modifier.height(24.dp))

                    // 5. Sezione ultimi 7 giorni
                    SezioneUltimi7Giorni(statsCorrenti)

                    Spacer(Modifier.height(24.dp))

                    // 5b. Grafico di progresso (30 giorni)
                    SezioneGraficoProgresso(statsCorrenti)

                    Spacer(Modifier.height(24.dp))

                    // 6. Sezione parole per lezione
                    SezioneParolePerLezione(statsCorrenti)

                    Spacer(Modifier.height(24.dp))

                    // 7. Riga finale paroleSynced / paroleCustom
                    RigaFontiParole(statsCorrenti)

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderProfilo(profilo: com.alessandro.tedesco.data.local.ProfiloUtente?, stats: Statistiche) {
    val nome = profilo?.config?.nomeVisualizzato ?: "Profilo"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nome,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stats.livelloStimato.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CardProgressoB1(stats: Statistiche) {
    val mancano = (stats.obiettivoB1 - stats.paroleApprese).coerceAtLeast(0)
    val percentuale = (stats.progressoB1 * 100).toInt()

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
                text = "Progresso verso B1",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Obiettivo: ${stats.obiettivoB1} parole",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { stats.progressoB1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stats.paroleApprese} / ${stats.obiettivoB1} parole",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$percentuale%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ti mancano $mancano parole per raggiungere B1.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RigaStatCardCompatte(stats: Statistiche) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCardCompatta(
            label = "Da ripassare",
            valore = "${stats.daRipassare}",
            colore = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        StatCardCompatta(
            label = "Mature",
            valore = "${stats.mature}",
            colore = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCardCompatta(
            label = "Accuratezza",
            valore = "${(stats.accuratezza * 100).toInt()}%",
            colore = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCardCompatta(
    label: String,
    valore: String,
    colore: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valore,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colore
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SezioneStatoParole(stats: Statistiche) {
    Text(
        text = "Stato delle parole",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ValoreColorato(
            label = "Nuove",
            valore = stats.nuove,
            colore = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        ValoreColorato(
            label = "In apprendimento",
            valore = stats.inApprendimento,
            colore = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        ValoreColorato(
            label = "Mature",
            valore = stats.mature,
            colore = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ValoreColorato(
    label: String,
    valore: Int,
    colore: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$valore",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colore
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SezioneUltimi7Giorni(stats: Statistiche) {
    Text(
        text = "Ripassi negli ultimi 7 giorni",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    val tuttiZero = stats.ultimi7giorni.all { it.ripassi == 0 }
    if (tuttiZero) {
        Text(
            text = "Nessun ripasso registrato",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    } else {
        GraficoBarreVerticali(stats.ultimi7giorni)
    }
}

@Composable
private fun GraficoBarreVerticali(dati: List<com.alessandro.tedesco.data.AttivitaGiorno>) {
    val maxVal = dati.maxOfOrNull { it.ripassi }?.coerceAtLeast(1) ?: 1

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val n = dati.size.coerceAtLeast(1)
        val spazio = 8f
        val larghezzaBarra = (size.width - spazio * (n + 1)) / n
        dati.forEachIndexed { i, attivita ->
            val altezzaBarra = (attivita.ripassi.toFloat() / maxVal) * (size.height - 24f)
            val x = spazio + i * (larghezzaBarra + spazio)
            val y = size.height - altezzaBarra - 18f

            drawRoundRect(
                color = if (i == dati.lastIndex) {
                    Color(0xFF1B5E9B)
                } else {
                    Color(0xFF9EC9F0)
                },
                topLeft = Offset(x, y),
                size = Size(larghezzaBarra.coerceAtLeast(2f), altezzaBarra.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
    // Etichette dei giorni
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dati.forEach { attivita ->
            Text(
                text = attivita.etichetta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SezioneGraficoProgresso(stats: Statistiche) {
    Text(
        text = "Progresso (30 giorni)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    if (stats.progresso.isEmpty()) {
        Text(
            text = "Nessun dato ancora",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    } else {
        GraficoLineaProgresso(stats.progresso)
    }
}

@Composable
private fun GraficoLineaProgresso(dati: List<com.alessandro.tedesco.data.ProgressPoint>) {
    val maxMature = dati.maxOfOrNull { it.paroleMature }?.coerceAtLeast(1) ?: 1
    val maxTotali = dati.maxOfOrNull { it.paroleTotali }?.coerceAtLeast(1) ?: 1

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val n = dati.size.coerceAtLeast(1)
        val stepX = size.width / (n - 1).coerceAtLeast(1)

        // Linea parole totali (griglia di sfondo)
        val pathTotali = Path()
        dati.forEachIndexed { i, p ->
            val x = i * stepX
            val y = size.height - (p.paroleTotali.toFloat() / maxTotali) * (size.height - 20f) - 10f
            if (i == 0) pathTotali.moveTo(x, y) else pathTotali.lineTo(x, y)
        }
        drawPath(
            path = pathTotali,
            color = Color(0xFF9EC9F0),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )

        // Linea parole mature
        val pathMature = Path()
        dati.forEachIndexed { i, p ->
            val x = i * stepX
            val y = size.height - (p.paroleMature.toFloat() / maxMature) * (size.height - 20f) - 10f
            if (i == 0) pathMature.moveTo(x, y) else pathMature.lineTo(x, y)
        }
        drawPath(
            path = pathMature,
            color = Color(0xFF1B5E9B),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // Punti sulle mature
        dati.forEachIndexed { i, p ->
            val x = i * stepX
            val y = size.height - (p.paroleMature.toFloat() / maxMature) * (size.height - 20f) - 10f
            drawCircle(
                color = Color(0xFF1B5E9B),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }

    // Etichette date (mostra ogni 5 giorni)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dati.forEachIndexed { i, p ->
            if (i % 5 == 0 || i == dati.lastIndex) {
                Text(
                    text = p.etichetta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Legenda
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = Color(0xFF1B5E9B), radius = 6f)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Mature",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = Color(0xFF9EC9F0), radius = 6f)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Totali",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SezioneParolePerLezione(stats: Statistiche) {
    Text(
        text = "Parole per lezione",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    if (stats.perLezione.isEmpty()) {
        Text(
            text = "Nessun dato ancora",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    } else {
        val maxTotale = stats.perLezione.maxOfOrNull { it.totale }?.coerceAtLeast(1) ?: 1
        val lezioniVisibili = if (stats.perLezione.size > 8) {
            stats.perLezione.take(8)
        } else {
            stats.perLezione
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lezioniVisibili.forEach { c ->
                RigaBarraOrizzontale(
                    lezione = c.lezione,
                    totale = c.totale,
                    maxTotale = maxTotale
                )
            }
            if (stats.perLezione.size > 8) {
                val altre = stats.perLezione.size - 8
                Text(
                    text = "e altre $altre lezioni",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun RigaBarraOrizzontale(
    lezione: Int,
    totale: Int,
    maxTotale: Int
) {
    val frazione = totale.toFloat() / maxTotale.toFloat()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "L${lezione.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        ) {
            // Sfondo barra
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            )
            // Barra riempimento
            Box(
                modifier = Modifier
                    .fillMaxWidth(frazione.coerceIn(0f, 1f))
                    .height(20.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$totale",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun RigaFontiParole(stats: Statistiche) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${stats.paroleSynced}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sincronizzate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${stats.paroleCustom}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "Personalizzate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
