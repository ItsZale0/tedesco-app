package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * V2 - schermata unica: lezione + parole + esercizi, in un flusso lineare.
 * - Selettore di lezione nella barra in alto (chip "Lezioni").
 * - Impostazioni ed extra dentro il menu nascosto (icona in alto a destra).
 * - La teoria e' markdown renderizzato (MarkdownText), mai testo grezzo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioVeloceScreen(
    vm: TedescoViewModel,
    onVaiAImpostazioni: () -> Unit = {},
    onVaiAProgresso: () -> Unit = {},
    onVaiATutor: () -> Unit = {},
    onVaiABackup: () -> Unit = {},
) {
    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val contenuto by vm.lezioneContenuto.collectAsStateWithLifecycle("")
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioniFeed by vm.lezioni.collectAsStateWithLifecycle(emptyList())
    val lezioneStato by vm.lezioneStato.collectAsStateWithLifecycle<StatoLezione>(StatoLezione.Idle)

    // Lezioni disponibili: quelle del feed + quelle che hanno parole nel vocabolario.
    val numeriLezioni = remember(lezioniFeed, parole, lezioneCorrente) {
        (lezioniFeed.map { it.numero } + parole.map { it.lesson } + lezioneCorrente)
            .filter { it > 0 }
            .distinct()
            .sorted()
    }
    val titoliLezioni = remember(lezioniFeed) { lezioniFeed.associate { it.numero to it.titolo } }
    val paroleLezione = remember(parole, lezioneCorrente) { parole.filter { it.lesson == lezioneCorrente } }

    var esercizi by remember { mutableStateOf<List<EsercizioStudio>>(emptyList()) }
    var indice by remember { mutableStateOf(0) }
    var scelta by remember { mutableStateOf<Int?>(null) }
    var corrette by remember { mutableStateOf(0) }
    var verificato by remember { mutableStateOf(false) }
    var inGenerazione by remember { mutableStateOf(false) }
    var mostraSelettore by remember { mutableStateOf(false) }
    var mostraMenu by remember { mutableStateOf(false) }

    // Cambio lezione -> nuovo set di esercizi sulle parole di quella lezione.
    LaunchedEffect(lezioneCorrente, paroleLezione.size) {
        if (esercizi.isEmpty() && paroleLezione.size >= 2) {
            inGenerazione = true
            esercizi = generaEserciziDaParole(paroleLezione)
            indice = 0
            scelta = null
            corrette = 0
            verificato = false
            inGenerazione = false
        }
    }

    // Primo avvio senza vocabolario -> sincronizza dal feed.
    LaunchedEffect(parole.isEmpty()) {
        if (parole.isEmpty()) vm.sincronizza()
    }

    // Contenuto o parole mancanti -> la lezione la genera l'AI e le parole entrano nel lessico.
    LaunchedEffect(lezioneCorrente, contenuto.isBlank(), paroleLezione.isEmpty()) {
        if ((contenuto.isBlank() || paroleLezione.isEmpty()) && lezioneStato !is StatoLezione.InCorso) {
            vm.generaLezioneDinamica(lezioneCorrente)
        }
    }

    val progressoEsercizi = if (esercizi.isEmpty()) 0f else (indice.toFloat() / esercizi.size).coerceIn(0f, 1f)
    val titoloLezione = titoliLezioni[lezioneCorrente]
        ?.takeIf { it.isNotBlank() && !it.startsWith("Lezione generata") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CompactTopBar(
                title = "Lezione $lezioneCorrente",
                actions = {
                    // ---- Selettore di lezione ----
                    Box {
                        AssistChip(
                            onClick = { mostraSelettore = true },
                            label = { Text("Lezioni") },
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                        DropdownMenu(
                            expanded = mostraSelettore,
                            onDismissRequest = { mostraSelettore = false }
                        ) {
                            numeriLezioni.forEach { numero ->
                                val titolo = titoliLezioni[numero]?.takeIf { it.isNotBlank() && !it.startsWith("Lezione generata") }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (titolo != null) "Lezione $numero - $titolo" else "Lezione $numero",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        mostraSelettore = false
                                        if (numero != lezioneCorrente) {
                                            esercizi = emptyList()
                                            vm.cambiaLezione(numero)
                                        }
                                    },
                                    trailingIcon = {
                                        if (numero == lezioneCorrente) {
                                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                    // ---- Menu nascosto: impostazioni ed extra ----
                    Box {
                        IconButton(onClick = { mostraMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = mostraMenu, onDismissRequest = { mostraMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Progresso") },
                                onClick = { mostraMenu = false; onVaiAProgresso() }
                            )
                            DropdownMenuItem(
                                text = { Text("Tutor AI") },
                                onClick = { mostraMenu = false; onVaiATutor() }
                            )
                            DropdownMenuItem(
                                text = { Text("Backup") },
                                onClick = { mostraMenu = false; onVaiABackup() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Impostazioni") },
                                onClick = { mostraMenu = false; onVaiAImpostazioni() }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Spaziature.md,
                end = Spaziature.md,
                top = Spaziature.sm,
                bottom = Spaziature.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spaziature.md)
        ) {
            // ---- Header: numero lezione, titolo, avanzamento ----
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Raggi.card),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$lezioneCorrente",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(Modifier.width(Spaziature.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    titoloLezione ?: "Lezione $lezioneCorrente",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    buildString {
                                        append("${paroleLezione.size} parole")
                                        if (esercizi.isNotEmpty()) append(" - ${esercizi.size} esercizi")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (esercizi.isNotEmpty()) {
                            Spacer(Modifier.height(Spaziature.sm))
                            LinearProgressIndicator(
                                progress = { progressoEsercizi },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (indice >= esercizi.size) "Esercizi completati: $corrette/${esercizi.size}"
                                else "Esercizio ${indice + 1} di ${esercizi.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ---- Stato: generazione in corso ----
            if (lezioneStato is StatoLezione.InCorso) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(Spaziature.sm))
                        Text(
                            "Preparo la lezione $lezioneCorrente...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ---- Teoria (markdown) ----
            if (contenuto.isNotBlank()) {
                item {
                    SezioneTitolo("Teoria", "Leggi la spiegazione in italiano")
                    Spacer(Modifier.height(Spaziature.sm))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            MarkdownText(
                                markdown = contenuto,
                                color = MaterialTheme.colorScheme.onSurface,
                                stile = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            TextButton(
                                onClick = { ttsHelper.speak(contenuto.replace(Regex("[#*`>_\\-]"), " ")) }
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Ascolta")
                            }
                        }
                    }
                }
            }

            // ---- Parole chiave della lezione ----
            if (paroleLezione.isNotEmpty()) {
                item {
                    SezioneTitolo(
                        titolo = "Parole chiave",
                        sottotitolo = "${paroleLezione.size} parole di questa lezione - tocca una parola per ascoltarla"
                    )
                }
                items(paroleLezione.chunked(2), key = { it.first().id }) { riga ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                    ) {
                        riga.forEach { parola ->
                            CardParola(
                                parola = parola,
                                modifier = Modifier.weight(1f),
                                onAscolta = {
                                    ttsHelper.speak(ParolaTedesca.conArticolo(parola.article, parola.german))
                                }
                            )
                        }
                        if (riga.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // ---- Esercizi ----
            if (inGenerazione) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(Spaziature.sm))
                        Text(
                            "Preparo gli esercizi...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (esercizi.isNotEmpty() && indice < esercizi.size) {
                item {
                    SezioneTitolo("Esercizi", "Domanda ${indice + 1} di ${esercizi.size}")
                }
                item {
                    val esercizio = esercizi[indice]
                    val sceltaCorrente = scelta
                    val esitoGiusto = verificato && sceltaCorrente == esercizio.rispostaCorretta

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Text(
                                esercizio.domanda,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(Spaziature.sm))

                            esercizio.opzioni.forEachIndexed { i, opzione ->
                                val selezionato = sceltaCorrente == i
                                val corretta = i == esercizio.rispostaCorretta
                                val colore = when {
                                    verificato && corretta -> MaterialTheme.colorScheme.primaryContainer
                                    verificato && selezionato -> MaterialTheme.colorScheme.errorContainer
                                    selezionato -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(enabled = !verificato) { scelta = i },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = colore),
                                    border = if (selezionato && !verificato) {
                                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spaziature.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (selezionato) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "ABCD".getOrElse(i) { '?' }.toString(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selezionato) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(Spaziature.sm))
                                        Text(
                                            opzione,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (verificato && corretta) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (verificato) {
                                Spacer(Modifier.height(Spaziature.sm))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (esitoGiusto) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.errorContainer
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(Spaziature.sm)) {
                                        Text(
                                            if (esitoGiusto) "Corretto!" else "Non corretto",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!esitoGiusto) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                "Risposta giusta: ${esercizio.opzioni[esercizio.rispostaCorretta]}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (esercizio.spiegazione.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            MarkdownText(
                                                markdown = esercizio.spiegazione,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                stile = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(Spaziature.md))

                            if (!verificato) {
                                Button(
                                    onClick = {
                                        verificato = true
                                        if (sceltaCorrente == esercizio.rispostaCorretta) corrette++
                                    },
                                    enabled = sceltaCorrente != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(AltezzaBottonePrincipale()),
                                    shape = RoundedCornerShape(Raggi.bottone)
                                ) {
                                    Text("Verifica risposta")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        indice++
                                        scelta = null
                                        verificato = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(AltezzaBottonePrincipale()),
                                    shape = RoundedCornerShape(Raggi.bottone)
                                ) {
                                    Text(if (indice < esercizi.size - 1) "Prossima domanda" else "Vedi risultato")
                                }
                            }
                        }
                    }
                }
            }

            // ---- Risultato finale ----
            if (esercizi.isNotEmpty() && indice >= esercizi.size) {
                item {
                    val percentuale = corrette * 100 / esercizi.size
                    val messaggio = when {
                        percentuale >= 80 -> "Ottimo lavoro, hai padroneggiato queste parole."
                        percentuale >= 60 -> "Buon risultato. Rivedi gli errori e vai avanti."
                        else -> "Serve un altro giro su questa lezione."
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sessione completata",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                "$corrette / ${esercizi.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$percentuale% di risposte corrette",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                messaggio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            Button(
                                onClick = {
                                    esercizi = generaEserciziDaParole(paroleLezione)
                                    indice = 0
                                    scelta = null
                                    corrette = 0
                                    verificato = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = RoundedCornerShape(Raggi.bottone)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(Spaziature.sm))
                                Text("Ripeti gli esercizi")
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            Button(
                                onClick = {
                                    esercizi = emptyList()
                                    indice = 0
                                    scelta = null
                                    corrette = 0
                                    verificato = false
                                    vm.avanzaLezione()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = RoundedCornerShape(Raggi.bottone)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(Spaziature.sm))
                                Text("Lezione successiva")
                            }
                        }
                    }
                }
            }

            // ---- Stato vuoto ----
            if (contenuto.isBlank() && paroleLezione.isEmpty() && lezioneStato !is StatoLezione.InCorso) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Nessun contenuto per la lezione $lezioneCorrente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                "La lezione non e' ancora disponibile: puoi generarla o sincronizzare il corso.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            Button(
                                onClick = { vm.generaLezioneDinamica(lezioneCorrente) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = RoundedCornerShape(Raggi.bottone)
                            ) {
                                Text("Genera la lezione")
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            TextButton(onClick = { vm.sincronizza() }) {
                                Text("Sincronizza il corso")
                            }
                        }
                    }
                }
            }

            if (esercizi.isEmpty() && paroleLezione.isNotEmpty() && paroleLezione.size < 2) {
                item {
                    Text(
                        "Servono almeno 2 parole nella lezione per generare gli esercizi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SezioneTitolo(titolo: String, sottotitolo: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            titolo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (sottotitolo.isNotBlank()) {
            Text(
                sottotitolo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardParola(
    parola: WordEntity,
    modifier: Modifier = Modifier,
    onAscolta: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onAscolta() },
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spaziature.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ParolaTedesca.conArticolo(parola.article, parola.german),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                parola.italian,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (parola.example.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    parola.example,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Esercizi sulla lezione: significato della parola (con articolo) e frase d'esempio
 * come spiegazione. Sempre 4 opzioni quando il vocabolario della lezione lo consente.
 */
private fun generaEserciziDaParole(parole: List<WordEntity>): List<EsercizioStudio> {
    val utilizzabili = parole.filter { it.german.isNotBlank() && it.italian.isNotBlank() }
    if (utilizzabili.size < 2) return emptyList()

    val mescolate = utilizzabili.shuffled()
    return mescolate.take(5).map { parola ->
        val distrattori = utilizzabili
            .filter { it.id != parola.id && it.italian != parola.italian }
            .shuffled()
            .take(3)
            .map { it.italian }
        val opzioni = (distrattori + parola.italian).shuffled()
        EsercizioStudio(
            domanda = "Cosa significa \"${ParolaTedesca.conArticolo(parola.article, parola.german)}\"?",
            opzioni = opzioni,
            rispostaCorretta = opzioni.indexOf(parola.italian),
            spiegazione = if (parola.example.isNotBlank()) "Esempio: ${parola.example}" else ""
        )
    }
}

data class EsercizioStudio(
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int,
    val spiegazione: String = ""
)
