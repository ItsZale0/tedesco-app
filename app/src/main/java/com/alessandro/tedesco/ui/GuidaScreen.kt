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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.SezioneEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.data.local.GuidaEntity
import com.alessandro.tedesco.data.local.LessonEntity

private const val URL_DOC_DEFAULT = "https://docs.google.com/document/d/12yKY4Bpp6IqX7q8tgNYkFXIoAQsZR8yVd4mZhcD5I7g/edit"

/** Costruisce l'URL del documento Google dal valore salvato (ID o URL completo). */
private fun urlDocumento(valore: String?): String {
    if (valore.isNullOrBlank()) return URL_DOC_DEFAULT
    if (valore.startsWith("http")) return valore
    return "https://docs.google.com/document/d/$valore/edit"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidaScreen(vm: TedescoViewModel) {
    val guida by vm.guida.collectAsStateWithLifecycle(null)
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val ctx = LocalContext.current
    var mostraLezione by remember { mutableStateOf(false) }
    val URL_DOC = urlDocumento(profilo?.config?.guidaDocId)

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
                    EmptyGuida(Modifier.align(Alignment.Center), URL_DOC)
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
    val lezioni by vm.lezioni.collectAsStateWithLifecycle(emptyList())
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
                        lezioni = lezioni,
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
    lezioni: List<LessonEntity>,
    onLezioneSelezionata: (Int) -> Unit
) {
    Column {
        lezioni.forEach { lezione ->
            val selezionata = lezione.numero == lezioneCorrente
            FilterChip(
                selected = selezionata,
                onClick = { onLezioneSelezionata(lezione.numero) },
                label = { Text("Lezione ${lezione.numero}: ${lezione.titolo}") },
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
            val pulita = line.trimEnd()
            when {
                pulita.startsWith("### ") -> {
                    TestoMarkdown(
                        testo = pulita.removePrefix("### "),
                        stile = MaterialTheme.typography.titleMedium,
                        grassetto = true,
                        colore = color,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                pulita.startsWith("## ") -> {
                    TestoMarkdown(
                        testo = pulita.removePrefix("## "),
                        stile = MaterialTheme.typography.titleLarge,
                        grassetto = true,
                        colore = color,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                pulita.startsWith("# ") -> {
                    TestoMarkdown(
                        testo = pulita.removePrefix("# "),
                        stile = MaterialTheme.typography.headlineSmall,
                        grassetto = true,
                        colore = color,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                pulita.startsWith("- ") || pulita.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                        Text("•", color = color, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(6.dp))
                        TestoMarkdown(
                            testo = pulita.drop(2),
                            stile = MaterialTheme.typography.bodyMedium,
                            grassetto = false,
                            colore = color
                        )
                    }
                }
                pulita.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val numero = pulita.substringBefore(".").trim()
                    val testo = pulita.substringAfter(".").trim()
                    Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp)) {
                        Text("$numero.", color = color, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(6.dp))
                        TestoMarkdown(
                            testo = testo,
                            stile = MaterialTheme.typography.bodyMedium,
                            grassetto = false,
                            colore = color
                        )
                    }
                }
                pulita.isNotBlank() -> {
                    TestoMarkdown(
                        testo = pulita,
                        stile = MaterialTheme.typography.bodyMedium,
                        grassetto = false,
                        colore = color,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                else -> Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/** Rende una riga interpretando **grassetto** e `codice`. */
@Composable
private fun TestoMarkdown(
    testo: String,
    stile: androidx.compose.ui.text.TextStyle,
    grassetto: Boolean,
    colore: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val annotated = remember(testo) { buildAnnotatedStringDaMarkdown(testo) }
    Text(
        text = annotated,
        style = stile,
        color = colore,
        fontWeight = if (grassetto) FontWeight.Bold else null,
        modifier = modifier
    )
}

private fun buildAnnotatedStringDaMarkdown(testo: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    val pattern = Regex("\\*\\*(.+?)\\*\\*|`(.+?)`")
    var ultimo = 0
    for (match in pattern.findAll(testo)) {
        if (match.range.first > ultimo) {
            builder.append(testo.substring(ultimo, match.range.first))
        }
        val grassetto = match.groupValues[1]
        val codice = match.groupValues[2]
        if (grassetto.isNotEmpty()) {
            builder.withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                append(grassetto)
            }
        } else if (codice.isNotEmpty()) {
            builder.withStyle(
                androidx.compose.ui.text.SpanStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    background = androidx.compose.ui.graphics.Color(0x22000000)
                )
            ) { append(codice) }
        }
        ultimo = match.range.last + 1
    }
    if (ultimo < testo.length) builder.append(testo.substring(ultimo))
    return builder.toAnnotatedString()
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
private fun EmptyGuida(modifier: Modifier = Modifier, urlDoc: String = URL_DOC_DEFAULT) {
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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlDoc))
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
