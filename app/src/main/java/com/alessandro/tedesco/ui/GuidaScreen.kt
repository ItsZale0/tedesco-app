package com.alessandro.tedesco.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.SezioneEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

private const val URL_DOC = "https://docs.google.com/document/d/12yKY4Bpp6IqX7q8tgNYkFXIoAQsZR8yVd4mZhcD5I7g/edit"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidaScreen(vm: TedescoViewModel) {
    val guida by vm.guida.collectAsStateWithLifecycle(null)
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)
    val ctx = LocalContext.current
    var mostraLezione by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guida") },
                actions = {
                    IconButton(
                        onClick = { vm.sincronizza() },
                        enabled = !caricamento
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aggiorna")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            val g = guida
            when {
                caricamento && g == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                g == null || g.sezioni.isEmpty() -> {
                    EmptyGuida(Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = dimensioneContenuto())
                            .align(Alignment.TopCenter),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = spaziaturaSchermo(), end = spaziaturaSchermo(),
                            top = 8.dp, bottom = 32.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = g.titolo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(12.dp))
                            // Toggle Documento / Lezione del giorno
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilterChip(
                                    selected = !mostraLezione,
                                    onClick = { mostraLezione = false },
                                    label = { Text("Documento") }
                                )
                                FilterChip(
                                    selected = mostraLezione,
                                    onClick = { mostraLezione = true },
                                    label = { Text("Lezione del giorno") }
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            if (!mostraLezione) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_DOC))
                                            ctx.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.size(8.dp))
                                        Text("Apri in Google Docs")
                                    }
                                    Button(
                                        onClick = { vm.sincronizza() },
                                        enabled = !caricamento,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Filled.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.size(8.dp))
                                        Text("Sincronizza Guida")
                                    }
                                }
                            }
                        }
                        if (mostraLezione) {
                            item {
                                LezioneDelGiornoCard(vm)
                            }
                        } else {
                            items(g.sezioni) { sez ->
                                SezioneCard(sez)
                            }
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Sincronizzata dal tuo Google Doc",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LezioneDelGiornoCard(vm: TedescoViewModel) {
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val contenuto by vm.lezioneContenuto.collectAsStateWithLifecycle("")
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val paroleLezione = parole.filter { it.lesson == lezioneCorrente }
    var mostraSelettore by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = "Lezione $lezioneCorrente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                TextButton(
                    onClick = { mostraSelettore = !mostraSelettore },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Cambia")
                }
            }
            AnimatedVisibility(
                visible = mostraSelettore,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    LezioniDisponibili(
                        lezioneCorrente = lezioneCorrente,
                        onLezioneSelezionata = { nuovaLezione ->
                            vm.cambiaLezione(nuovaLezione)
                            mostraSelettore = false
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (contenuto.isNotBlank()) {
                MarkdownText(
                    markdown = contenuto,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = "Contenuto della lezione non ancora disponibile. Verrà aggiornato al prossimo sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (paroleLezione.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Parole di questa lezione: ${paroleLezione.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun LezioniDisponibili(
    lezioneCorrente: Int,
    onLezioneSelezionata: (Int) -> Unit
) {
    val lezioni = (1..10).toList()
    Column {
        lezioni.forEach { lezione ->
            val selezionata = lezione == lezioneCorrente
            FilterChip(
                selected = selezionata,
                onClick = { onLezioneSelezionata(lezione) },
                label = { Text("Lezione $lezione") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun MarkdownText(markdown: String, color: androidx.compose.ui.graphics.Color) {
    val lines = markdown.split("\n")
    Column {
        lines.forEach { line ->
            when {
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                line.startsWith("- ") -> {
                    Text(
                        text = "• ${line.removePrefix("- ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
                line.isNotBlank() -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SezioneCard(sez: SezioneEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 24.dp)
                ) {
                    Card(
                        modifier = Modifier.size(width = 4.dp, height = 24.dp),
                        shape = RoundedCornerShape(2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {}
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    text = sez.titolo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(14.dp))

            Text(
                text = sez.testo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )
        }
    }
}

@Composable
private fun EmptyGuida(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Guida non ancora scaricata",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Vai in Profilo, tocca \"Sincronizza adesso\". La guida arriva dal tuo Google Doc.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_DOC))
                ctx.startActivity(intent)
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text("Apri in Google Docs")
        }
    }
}
