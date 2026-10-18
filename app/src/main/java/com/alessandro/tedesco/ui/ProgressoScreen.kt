package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.CalcoloProgresso
import com.alessandro.tedesco.data.CertificazioniUfficiali
import com.alessandro.tedesco.data.Statistiche
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset

/**
 * Schermata di progresso UNIFICATA: un solo posto, un solo racconto.
 * Sostituisce le vecchie "Statistiche" e "Competenze" (che dicevano cose diverse).
 * Tutti i numeri derivano da CalcoloProgresso (dati reali).
 */
@Composable
fun ProgressoScreen(vm: TedescoViewModel, onIndietro: (() -> Unit)? = null) {
    val p by vm.progressoReale.collectAsStateWithLifecycle(null)

    ScreenScaffold("Progresso", onBack = onIndietro) {
        val pr = p
        if (pr == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Caricamento…", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@ScreenScaffold
        }
        Column(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                CardLivello(pr)
                CardProssimoPasso(pr)
                CardCompetenze(pr)
                CardAttivita(pr)
                CardProduzioneScritta(pr)
                CardTraguardi(pr)
                CardRiferimenti(pr)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CardLivello(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Il tuo livello", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(pr.livello.label, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(pr.livello.descrizione, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            // Verso il prossimo livello (media delle 4 competenze, criterio chiaro)
            val prossimo = pr.prossimoLivello
            if (prossimo != null) {
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Verso ${prossimo.label.substringBefore(" ")}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("${(pr.versoProssimoLivello * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { pr.versoProssimoLivello },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text("Serve il 60% in TUTTE le 4 competenze (soglia ufficiale Goethe/telc/ÖSD)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Spacer(Modifier.height(10.dp))
                Text("Hai raggiunto il tuo obiettivo: ${pr.livelloObiettivo.label}",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CardProssimoPasso(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Cosa fare adesso", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Ti manca soprattutto: ${pr.dimensioneDebole} (${pr.dimensioneDeboleValore.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(2.dp))
            Text(pr.azioneConsigliata, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CardCompetenze(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Le 4 competenze", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("Il livello sale solo quando TUTTE superano il 60% (la più debole comanda)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            BarraCompetenza("Lessico", pr.competenze.lessico)
            BarraCompetenza("Grammatica", pr.competenze.grammatica)
            BarraCompetenza("Comprensione", pr.competenze.comprensione)
            BarraCompetenza("Produzione", pr.competenze.produzione)
        }
    }
}

@Composable
private fun BarraCompetenza(nome: String, valore: Float) {
    val colore = if (valore >= 60f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val v = valore.coerceIn(0f, 100f)
    val ok = v >= CertificazioniUfficiali.SOGLIA_SUPERAMENTO
    Column(Modifier.padding(vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nome, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (ok) "${v.toInt()}% ✓" else "${v.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (ok) colore else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { v / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (ok) colore else MaterialTheme.colorScheme.outline,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CardAttivita(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("La tua attività", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("\"Verificate\" = parole dimostrate nel quiz, non solo ripassate.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiBox("Streak", "${pr.streakGiorni}g", "giorni di fila", MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f))
                KpiBox("Record", "${pr.streakRecord}g", "miglior serie", MaterialTheme.colorScheme.tertiary,
                    Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiBox("Verificate", "${pr.paroleImparate}", "su ${pr.paroleDisponibili}", MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f))
                KpiBox("Ripassi", "${pr.ripassiTotali}", "totali", MaterialTheme.colorScheme.secondary,
                    Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiBox("Accuratezza", "${(pr.accuratezza * 100).toInt()}%", "risposte giuste",
                    MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                KpiBox("Studio", "${pr.minutiStudiati}m", "stimati", MaterialTheme.colorScheme.error,
                    Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun KpiBox(label: String, valore: String, sotto: String, colore: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valore, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = colore)
            Text(sotto, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CardProduzioneScritta(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Produzione scritta", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("Testi liberi valutati sui 4 criteri ufficiali Goethe (Erfuellung, Kohaerenz, Wortschatz, Strukturen)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            if (pr.produzioniScritte == 0) {
                Text(
                    "Nessuna produzione scritta valutata. Scrivi un testo in Produzione Scritta " +
                        "e premilo Verifica: il tutor lo valuta e il punteggio entra qui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // KPI principali
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiBox("Testi", "${pr.produzioniScritte}", "valutati",
                        MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    KpiBox("Media", "${pr.mediaProduzione.toInt()}%",
                        if (pr.mediaProduzione >= 60f) "superato" else "da migliorare",
                        if (pr.mediaProduzione >= 60f) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error, Modifier.weight(1f))
                }

                // Trend
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Trend", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val trend = pr.trendProduzione
                    val trendText = when {
                        trend > 0.5f -> "↑ migliorando"
                        trend < -0.5f -> "↓ peggiorando"
                        else -> "→ stabile"
                    }
                    val trendColor = when {
                        trend > 0.5f -> MaterialTheme.colorScheme.primary
                        trend < -0.5f -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(trendText, style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold, color = trendColor)
                }

                // Correlazione con altre competenze
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Correlazione con altre competenze", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(pr.correlazioneProduzione * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }

                // Obiettivo settimanale
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Questa settimana", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${pr.produzioniSettimana}/${pr.obiettivoSettimanaleProduzione} testi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pr.produzioniSettimana >= pr.obiettivoSettimanaleProduzione)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Criterio più debole
                if (pr.criterioDeboleProduzione != "N/A") {
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Criterio più debole", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${pr.criterioDeboleProduzione} (${pr.criterioDeboleValore.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error)
                    }
                }

                // Grafico evoluzione
                if (pr.storicoProduzione.size >= 2) {
                    Spacer(Modifier.height(12.dp))
                    Text("Evoluzione", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    GraficoEvoluzione(pr)
                }

                // Ultimo Prädikat
                if (pr.ultimoPraedikatProduzione.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ultimo risultato", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pr.ultimoPraedikatProduzione,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Soglia ufficiale: 60% (Goethe/telc/ÖSD)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GraficoEvoluzione(pr: CalcoloProgresso.ProgressoReale) {
    val punteggi = pr.storicoProduzione.map { it.punteggioTotale }
    val maxP = punteggi.maxOrNull() ?: 100f
    val minP = punteggi.minOrNull() ?: 0f
    val range = (maxP - minP).coerceAtLeast(1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val w = size.width
        val h = size.height
        val step = if (punteggi.size > 1) w / (punteggi.size - 1) else w

        // Linea del grafico
        val path = androidx.compose.ui.graphics.Path()
        punteggi.forEachIndexed { i, p ->
            val x = i * step
            val y = h - ((p - minP) / range * h)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = androidx.compose.ui.graphics.Color(0xFF6750A4), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))

        // Punti
        punteggi.forEachIndexed { i, p ->
            val x = i * step
            val y = h - ((p - minP) / range * h)
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF6750A4), radius = 4f, center = androidx.compose.ui.geometry.Offset(x, y))
        }
    }
}

@Composable
private fun CardTraguardi(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Traguardi", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("${pr.traguardiRaggiunti}/${pr.traguardi.size}",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            pr.traguardi.chunked(2).forEach { coppia ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    coppia.forEach { t ->
                        Row(
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (t.raggiunto) t.icona else "○",
                                style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(0.dp))
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(t.nome, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (t.raggiunto) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (t.raggiunto) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (coppia.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CardRiferimenti(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Riferimenti ufficiali", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("Goethe-Institut / telc / ÖSD",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            RigaRif("Soglia di superamento", "60% per modulo")
            RigaRif("Vocabolario del tuo livello", "${pr.wortlisteUfficiale} parole (Wortliste)")
            RigaRif("Obiettivo ${pr.livelloObiettivo.label.substringBefore(" ")}",
                "${pr.vocabolarioAttivoUfficiale} parole attive")
            RigaRif("Ore di studio stimate", "${pr.oreGuida.first}–${pr.oreGuida.last} ore")
        }
    }
}

@Composable
private fun RigaRif(etichetta: String, valore: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etichetta, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valore, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
