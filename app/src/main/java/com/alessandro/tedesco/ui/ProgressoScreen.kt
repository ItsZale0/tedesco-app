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

/**
 * Schermata di progresso UNIFICATA: un solo posto, un solo racconto.
 * Sostituisce le vecchie "Statistiche" e "Competenze" (che dicevano cose diverse).
 * Tutti i numeri derivano da CalcoloProgresso (dati reali).
 */
@Composable
fun ProgressoScreen(vm: TedescoViewModel) {
    val p by vm.progressoReale.collectAsStateWithLifecycle(null)

    ScreenScaffold("Progresso") {
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
            BarraCompetenza("Lessico", pr.competenze.lessico, MaterialTheme.colorScheme.primary)
            BarraCompetenza("Grammatica", pr.competenze.grammatica, MaterialTheme.colorScheme.tertiary)
            BarraCompetenza("Comprensione", pr.competenze.comprensione, MaterialTheme.colorScheme.secondary)
            BarraCompetenza("Produzione", pr.competenze.produzione, MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun BarraCompetenza(nome: String, valore: Float, colore: Color) {
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
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiBox("Streak", "${pr.streakGiorni}g", "giorni di fila", MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f))
                KpiBox("Record", "${pr.streakRecord}g", "miglior serie", MaterialTheme.colorScheme.tertiary,
                    Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiBox("Parole", "${pr.paroleImparate}", "su ${pr.paroleDisponibili}", MaterialTheme.colorScheme.primary,
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
