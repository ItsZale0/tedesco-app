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
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import android.media.MediaPlayer
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import androidx.compose.material3.OutlinedButton

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

    ScreenScaffold("Guida", actions = {
        IconButton(
            onClick = { vm.sincronizza() },
            enabled = !caricamento
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Aggiorna")
        }
    }, scrollable = false) {
        Box(
            modifier = Modifier
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
                                    label = { Text("Guida completa") }
                                )
                                FilterChip(
                                    selected = mostraLezione,
                                    onClick = { mostraLezione = true },
                                    label = { Text("Lezione di oggi") }
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
    val lezioneCorrenteRaw by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val lezioni by vm.lezioni.collectAsStateWithLifecycle(emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current
    val contenuto by vm.lezioneContenuto.collectAsStateWithLifecycle("")
    val audioUrl by vm.lezioneAudioUrl.collectAsStateWithLifecycle("")
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioneStato by vm.lezioneStato.collectAsStateWithLifecycle<StatoLezione>(StatoLezione.Idle)
    val ttsHelper = rememberTtsHelper(context)
    val giorniProssima by vm.giorniProssimaLezione.collectAsStateWithLifecycle(2)
    LaunchedEffect(Unit) { vm.inizializzaDataLezione() }

    // LEZIONI REALI: una lezione è REALE se ha parole NEL VOCABOLARIO oppure
    // ha contenuto DAL FEED. Il filtro unico è sulla parole + contenuto.
    // Nel feed Alessando: lezione 4 ha 48 parole ma manca in `lessons[]`,
    // lezione 5 esiste ma ha 0 parole e 0 contenuto. Senza questo filtro
    // la UI mostra lezioni vuote o parole orfane (es. lezione 4 scomparsa).
        // LEZIONI REALI: una lezione è REALE se ha parole NEL VOCABOLARIO oppure
    // ha contenuto DAL FEED. Il filtro unico è sulla parole + contenuto.
    // Nel feed Alessand: lezione 4 ha 48 parole ma manca in `lessons[]`,
    // lezione 5 esiste ma ha 0 parole e 0 contenuto. Senza questo filtro
    // la UI mostra lezioni vuote o parole orfane (es. lezione 4 scomparsa).
    // LEZIONI REALI: una lezione è REALE se ha parole NEL VOCABOLARIO oppure
    // ha contenuto DAL FEED.
        // LEZIONI REALI: una lezione è REALE se ha parole NEL VOCABOLARIO oppure
    // ha contenuto DAL FEED (es. lezione 4 ha parole ma manca in `lessons[]`;
    // lezione 5 ha contenuto ma 0 parole).
    val lezioniReali = buildList {
            lezioni.forEach { add(it) }
        parole.map { it.lesson }.distinct().forEach { numero ->
            add(LessonEntity(numero, "Lezione generata", "", ""))
        }
    }.distinctBy { it.numero }
      .filter { w -> parole.any { v -> v.lesson == w.numero } || w.contenuto.isNotBlank() }
      .sortedBy { it.numero }

    // La lezione corrente: usa quella reale se possibile, altrimenti la prima.
    val lezioneCorrente = if (lezioniReali.any { it.numero == lezioneCorrenteRaw }) lezioneCorrenteRaw
        else lezioniReali.firstOrNull()?.numero ?: lezioneCorrenteRaw

    val paroleLezione = parole.filter { it.lesson == lezioneCorrente }
    var mostraSelettore by remember { mutableStateOf(false) }

    // LEZIONE DINAMICA: se il feed non ha il contenuto o non ha parole per
    // quella lezione (profilo Emma: 0 lezioni; Alessandro: lezione 5 senza
    // parole), la genera l'AI. Le parole vengono aggiunte al vocabolario.
    LaunchedEffect(lezioneCorrente, contenuto.isBlank(), paroleLezione.isEmpty()) {
        val manca = contenuto.isBlank() || paroleLezione.isEmpty()
        if (manca && lezioneStato !is StatoLezione.InCorso) {
            vm.generaLezioneDinamica(lezioneCorrente)
        }
    }

    // Banner: una lezione ogni 2 giorni
    if (giorniProssima > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Raggi.card),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Prossima lezione tra $giorniProssima giorno${if (giorniProssima == 1) "" else "i"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ripassa le parole e fai gli esercizi nel frattempo!",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    } else {
        Button(
            onClick = { vm.avanzaLezione() },
            modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
        ) { Text("Inizia la lezione ${lezioneCorrente + 1}") }
        Spacer(Modifier.height(8.dp))
    }

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
                    Column {
                        Text(
                            text = "Lezione $lezioneCorrente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        // Quanto manca alla lezione successiva: la regola e'
                        // "una lezione ogni due giorni", cosi' la progressione e' chiara.
                        Text(
                            text = if (giorniProssima > 0)
                                "Tra $giorniProssima giorno${if (giorniProssima == 1) "" else "i"}: ripassa e fai gli esercizi"
                                else "Puoi iniziare la lezione ${lezioneCorrente + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                        lezioni = lezioniReali,
                        onLezioneSelezionata = { nuovaLezione ->
                            vm.cambiaLezione(nuovaLezione)
                            mostraSelettore = false
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (audioUrl.isNotBlank()) {
                var isPlaying by remember { mutableStateOf(false) }
                var progress by remember { mutableFloatStateOf(0f) }
                var prepared by remember { mutableStateOf(false) }
                val mediaPlayer = remember { MediaPlayer() }
                LaunchedEffect(audioUrl) {
                    try {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(audioUrl)
                        mediaPlayer.setOnPreparedListener { prepared = true }
                        mediaPlayer.setOnCompletionListener { isPlaying = false; progress = 0f }
                        mediaPlayer.prepareAsync()
                    } catch (_: Exception) {}
                }
                DisposableEffect(Unit) {
                    onDispose { try { mediaPlayer.release() } catch (_: Exception) {} }
                }
                LaunchedEffect(isPlaying) {
                    while (isPlaying) {
                        try {
                            val dur = mediaPlayer.duration
                            if (dur > 0) progress = mediaPlayer.currentPosition.toFloat() / dur
                        } catch (_: Exception) {}
                        delay(400)
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ascolta la lezione",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Pronuncia lenta delle parole",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            IconButton(onClick = {
                                try {
                                    if (isPlaying) {
                                        mediaPlayer.pause()
                                        isPlaying = false
                                    } else if (prepared) {
                                        mediaPlayer.start()
                                        isPlaying = true
                                    }
                                } catch (_: Exception) {}
                            }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausa" else "Riproduci"
                                )
                            }
                        }
                        if (progress > 0f || isPlaying) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            if (contenuto.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { ttsHelper.speak(contenuto) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ascolta contenuto lezione",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                MarkdownText(
                    markdown = contenuto,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                when (val s = lezioneStato) {
                    is StatoLezione.InCorso -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Genero la lezione per il tuo livello…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    is StatoLezione.Errore -> Column {
                        Text(
                            "Non riesco a generare la lezione: ${s.messaggio}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.generaLezioneDinamica(lezioneCorrente) }) {
                            Text("Riprova")
                        }
                    }
                    else -> Column {
                        Text(
                            "Questa lezione non è nel materiale. Posso generarla io sul tuo livello: " +
                                "spiegazione, esempi e 12 parole nuove che entrano nel tuo vocabolario.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { vm.generaLezioneDinamica(lezioneCorrente) }) {
                            Text("Genera la lezione")
                        }
                    }
                }
            }
            if (paroleLezione.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Vocabolario della lezione (${paroleLezione.size} parole)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(6.dp))
                paroleLezione.forEach { w ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = com.alessandro.tedesco.data.ParolaTedesca.conArticolo(w.article, w.german),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = w.italian,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: aggiungi le parole alla lista fiscale */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aggiungi al dizionario")
                    }
                    OutlinedButton(
                        onClick = { /* TODO: metti in evidenza le parole rimanenti */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ripassa le parole")
                    }
                }
            } else if (contenuto.isEmpty()) {
                Text(
                    "Nessuna parola per questa lezione. Premi \"Genera la lezione\" per creare il vocabolario.",
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
private fun SezioneCard(sez: SezioneEntity) {
    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = sez.testo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { ttsHelper.speak("${sez.titolo}. ${sez.testo}") }) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Ascolta sezione",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
