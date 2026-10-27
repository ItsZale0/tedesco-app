package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.data.StatoUnita
import com.alessandro.tedesco.data.UnitaConStato
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.ConsigliEsercizi
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore

/**
 * V2 - schermata unica: lezione + parole + esercizi, in un flusso lineare.
 * - Selettore di lezione nella barra in alto (chip "Lezioni").
 * - Impostazioni ed extra dentro il menu nascosto (icona in alto a destra).
 * - La teoria è markdown renderizzato (MarkdownText), mai testo grezzo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioVeloceScreen(
    vm: TedescoViewModel,
    updater: UpdaterViewModel,
    onVaiAEsercizi: () -> Unit = {},
    onVaiAImpostazioni: () -> Unit = {},
    onVaiAProgresso: () -> Unit = {},
    onVaiATutor: () -> Unit = {},
    onVaiABackup: () -> Unit = {},
    onVaiAPercorsoGrammatica: () -> Unit = {},
    onNaviga: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)
    val scope = rememberCoroutineScope()

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

    val pr by vm.progressoReale.collectAsStateWithLifecycle(null)
    val consiglio = remember(pr) { ConsigliEsercizi.consiglio(pr) }
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val inizialeProfilo = remember(profilo) {
        (profilo?.config?.nomeVisualizzato?.trim()?.firstOrNull()?.uppercase() ?: "A")
    }
    val nomeProfilo = remember(profilo) {
        profilo?.config?.nomeVisualizzato
            ?.trim()
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
    }

    var esercizi by remember { mutableStateOf<List<EsercizioStudio>>(emptyList()) }
    var indice by remember { mutableStateOf(0) }
    var scelta by remember { mutableStateOf<Int?>(null) }
    var corrette by remember { mutableStateOf(0) }
    var verificato by remember { mutableStateOf(false) }
    var inGenerazione by remember { mutableStateOf(false) }
    var mostraSelettore by remember { mutableStateOf(false) }
    var mostraMenu by remember { mutableStateOf(false) }
    var mostraGate by remember { mutableStateOf(false) }

    // Test di ingresso: mostrato finché non viene completato o saltato.
    val placementFatto by vm.placementFatto.collectAsStateWithLifecycle(false)

    // Percorso grammaticale: la card in home deve mostrare subito il prossimo passo.
    val percorsoGram by vm.percorsoGrammatica.collectAsStateWithLifecycle(
        GrammaticaSyllabus.percorso(emptyMap())
    )
    val prossimaGram = remember(percorsoGram) {
        percorsoGram.firstOrNull { it.stato == StatoUnita.DA_RIPASSARE }
            ?: percorsoGram.firstOrNull { it.stato == StatoUnita.DISPONIBILE }
            ?: percorsoGram.firstOrNull()
    }
    val completateGram = percorsoGram.count { it.padronanza?.completata == true }
    val pianoOggi by vm.percorsoGiornaliero.collectAsStateWithLifecycle(null)

    // Salva il miglior punteggio della lezione quando la sessione è completata:
    // serve al gate morbido ("lezione successiva") e al progresso.
    LaunchedEffect(esercizi.size, indice) {
        if (esercizi.isNotEmpty() && indice >= esercizi.size) {
            vm.registraPunteggioLezione(lezioneCorrente, corrette * 100 / esercizi.size)
        }
    }

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
                title = "Studio",
                actions = {
                    // ---- Selettore di lezione ----
                    Box {
                        AssistChip(
                            onClick = { mostraSelettore = true },
                            label = { Text("Lezione $lezioneCorrente") },
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
                    // ---- Profilo ed extra: avatar tondo -> pannello dal basso ----
                    // Niente tre puntini: un solo tocco apre un pannello ordinato.
                    Surface(
                        onClick = { mostraMenu = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                inizialeProfilo,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = dimensioneContenuto())
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = spaziaturaSchermo(),
                    end = spaziaturaSchermo(),
                    top = Spaziature.sm,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
            // ---- Banner aggiornamento (sempre in alto) ----
            item {
                UpdateBanner(updater)
            }
            // ---- Hero: piano di oggi ----
            pianoOggi?.let { piano ->
                item {
                    PianoOggiHero(
                        percorso = piano,
                        saluto = nomeProfilo?.let { "Ciao, $it" } ?: "Ciao",
                        onApriPasso = { passo ->
                            if (passo.azione == "ripasso") {
                                vm.caricaSessione()
                            }
                            onNaviga(passo.azione)
                        },
                        onCompletaPasso = { vm.completaPassoGiornaliero(it) }
                    )
                }
            }

            // ---- Azioni rapide: le sezioni principali a portata di pollice ----
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                ) {
                    AzioneRapida(
                        icona = Icons.Filled.Quiz,
                        etichetta = "Esercizi",
                        onClick = onVaiAEsercizi,
                        modifier = Modifier.weight(1f)
                    )
                    AzioneRapida(
                        icona = Icons.Filled.Refresh,
                        etichetta = "Ripasso",
                        onClick = {
                            vm.caricaSessione()
                            onNaviga("ripasso")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AzioneRapida(
                        icona = Icons.Filled.SmartToy,
                        etichetta = "Tutor",
                        onClick = onVaiATutor,
                        modifier = Modifier.weight(1f)
                    )
                    AzioneRapida(
                        icona = Icons.Filled.BarChart,
                        etichetta = "Progresso",
                        onClick = onVaiAProgresso,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // ---- Lezione di oggi: intestazione ----
            item {
                LezioneHeaderCard(
                    numero = lezioneCorrente,
                    titolo = titoloLezione ?: "Lezione $lezioneCorrente",
                    parole = paroleLezione.size,
                    esercizi = esercizi.size,
                    indice = indice,
                    corrette = corrette,
                    progresso = progressoEsercizi
                )
            }

            // ---- Percorso grammaticale: il prossimo passo ----
            item {
                PercorsoGrammaticaCard(
                    prossima = prossimaGram,
                    completate = completateGram,
                    totale = percorsoGram.size,
                    onApri = onVaiAPercorsoGrammatica
                )
            }

            // ---- Test di ingresso (una volta sola, finché non viene fatto) ----
            if (!placementFatto) {
                item {
                    PlacementTestCard(
                        onCompletato = { livello -> vm.impostaLivello(livello) },
                        onSalta = { vm.saltaPlacement() }
                    )
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

            // ---- Teoria: espandibile, la home resta corta ----
            if (contenuto.isNotBlank()) {
                item {
                    TeoriaCard(
                        contenuto = contenuto,
                        onAscolta = { ttsHelper.speak(contenuto.replace(Regex("[#*`>_\\-]"), " ")) }
                    )
                }
            }

            // ---- Parole chiave della lezione ----
            if (paroleLezione.isNotEmpty()) {
                item {
                    SezioneTitolo(
                        titolo = "Parole chiave",
                        sottotitolo = "Scorri: ${paroleLezione.size} parole - tocca per ascoltare"
                    )
                }
                // Slider orizzontale: la home resta corta anche con 30 parole.
                item {
                    val statoLista = rememberLazyListState()
                    val paginaCorrente = statoLista.firstVisibleItemIndex
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(
                            state = statoLista,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = statoLista),
                            horizontalArrangement = Arrangement.spacedBy(Spaziature.sm),
                            contentPadding = PaddingValues(end = Spaziature.xxl)
                        ) {
                            items(paroleLezione, key = { it.id }) { parola ->
                                CardParola(
                                    parola = parola,
                                    modifier = Modifier.fillParentMaxWidth(0.86f),
                                    onAscolta = {
                                        ttsHelper.speak(ParolaTedesca.conArticolo(parola.article, parola.german))
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(Spaziature.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${(paginaCorrente + 1).coerceAtMost(paroleLezione.size)} / ${paroleLezione.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (paroleLezione.size > 1) {
                                Spacer(Modifier.width(Spaziature.sm))
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            statoLista.animateScrollToItem(
                                                (paginaCorrente + 1) % paroleLezione.size
                                            )
                                        }
                                    }
                                ) { Text("Prossima") }
                            }
                        }
                        if (paroleLezione.size in 2..14) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(paroleLezione.size) { i ->
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(if (i == paginaCorrente) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (i == paginaCorrente) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---- Consiglio dinamico ----
            item {
                ConsiglioCard(
                    titolo = consiglio.titolo,
                    motivo = consiglio.motivo,
                    onApri = onVaiAEsercizi
                )
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
                    SezioneTitolo("Esercizi", "Rispondi e verifica la comprensione")
                }
                item {
                    val esercizio = esercizi[indice]
                    val sceltaCorrente = scelta
                    val esitoGiusto = verificato && sceltaCorrente == esercizio.rispostaCorretta

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "DOMANDA ${indice + 1} DI ${esercizi.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$corrette corrette",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (indice + 1f) / esercizi.size },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            Text(
                                esercizio.domanda,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
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
                                        .selectable(
                                            selected = selezionato,
                                            enabled = !verificato,
                                            role = Role.RadioButton,
                                            onClick = { scelta = i }
                                        ),
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(containerColor = colore),
                                    border = if (selezionato && !verificato) {
                                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                                MaterialTheme.colorScheme.surfaceContainer
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
                                    shape = MaterialTheme.shapes.large,
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
                                    shape = MaterialTheme.shapes.large
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
                                    shape = MaterialTheme.shapes.large
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
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                "Sessione completata",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(Spaziature.xs))
                            Text(
                                "$corrette / ${esercizi.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$percentuale% di risposte corrette",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                messaggio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            OutlinedButton(
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
                                shape = MaterialTheme.shapes.large
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(Spaziature.sm))
                                Text("Ripeti gli esercizi")
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            Button(
                                onClick = {
                                    if (percentuale >= 60) {
                                        esercizi = emptyList()
                                        indice = 0
                                        scelta = null
                                        corrette = 0
                                        verificato = false
                                        vm.avanzaLezione()
                                    } else {
                                        mostraGate = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = MaterialTheme.shapes.large
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
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
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
                                "La lezione non è ancora disponibile: puoi generarla o sincronizzare il corso.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            Button(
                                onClick = { vm.generaLezioneDinamica(lezioneCorrente) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = MaterialTheme.shapes.large
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

    // Pannello "profilo ed extra": dal basso, con icone e descrizioni.
    if (mostraMenu) {
        ModalBottomSheet(
            onDismissRequest = { mostraMenu = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(modifier = Modifier.padding(bottom = Spaziature.xl)) {
                Text(
                    profilo?.config?.nomeVisualizzato?.takeIf { it.isNotBlank() } ?: "Il tuo profilo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spaziature.md, vertical = Spaziature.sm)
                )
                VocePannello(Icons.Filled.Quiz, "Esercizi", "Tutti i tipi, sempre nuovi") {
                    mostraMenu = false; onVaiAEsercizi()
                }
                VocePannello(Icons.Filled.EmojiEvents, "Progresso", "Livello, competenze, numeri") {
                    mostraMenu = false; onVaiAProgresso()
                }
                VocePannello(Icons.Filled.SmartToy, "Tutor AI", "Chiedi, correggi, esercitati") {
                    mostraMenu = false; onVaiATutor()
                }
                VocePannello(Icons.Filled.CloudUpload, "Backup", "Salva e ripristina i dati") {
                    mostraMenu = false; onVaiABackup()
                }
                VocePannello(Icons.Filled.Settings, "Impostazioni", "Profilo, chiave, aggiornamenti") {
                    mostraMenu = false; onVaiAImpostazioni()
                }
            }
        }
    }

    // Gate morbido: non blocca, ma chiede conferma se l'evidenza è debole.
    if (mostraGate) {
        AlertDialog(
            onDismissRequest = { mostraGate = false },
            title = { Text("Passare alla lezione successiva?") },
            text = {
                Text(
                    "Il punteggio di questa sessione è sotto il 60%. " +
                        "Puoi ripetere gli esercizi oppure andare avanti lo stesso: " +
                        "nessun blocco, ma il ripasso resta più efficace."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostraGate = false
                    esercizi = emptyList()
                    indice = 0
                    scelta = null
                    corrette = 0
                    verificato = false
                    vm.avanzaLezione()
                }) { Text("Vai avanti") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostraGate = false
                    esercizi = generaEserciziDaParole(paroleLezione)
                    indice = 0
                    scelta = null
                    corrette = 0
                    verificato = false
                }) { Text("Ripeti") }
            }
        )
    }
}

@Composable
private fun SezioneTitolo(titolo: String, sottotitolo: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(Spaziature.sm))
        Column(modifier = Modifier.weight(1f)) {
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
}

@Composable
private fun CardParola(
    parola: WordEntity,
    modifier: Modifier = Modifier,
    onAscolta: () -> Unit
) {
    val coloreArticolo = MaterialTheme.colorScheme.primary
    val articolo = parola.article?.takeIf { it.isNotBlank() }
    Card(
        modifier = modifier.clickable(
            onClickLabel = "Ascolta ${parola.german}",
            role = Role.Button
        ) { onAscolta() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    buildAnnotatedString {
                        if (articolo != null) {
                            withStyle(SpanStyle(color = coloreArticolo, fontWeight = FontWeight.Bold)) {
                                append(articolo)
                                append(" ")
                            }
                        }
                        append(parola.german)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Spaziature.sm))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                parola.italian,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (parola.example.isNotBlank()) {
                Spacer(Modifier.height(Spaziature.sm))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        parola.example,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hero della home: il piano di oggi con saluto, anello di avanzamento e
 * passi rapidi. Toccare una riga apre l'attività; la spunta la completa.
 */
@Composable
private fun PianoOggiHero(
    percorso: CalcoloPercorsoGiornaliero.PercorsoGiornaliero,
    saluto: String,
    onApriPasso: (CalcoloPercorsoGiornaliero.PassoPercorso) -> Unit,
    onCompletaPasso: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "IL TUO PIANO DI OGGI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        saluto,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "20 minuti, un passo alla volta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.width(Spaziature.sm))
                ProgressoOggiRing(
                    progresso = percorso.progressoGiornata,
                    completati = percorso.passiCompletati,
                    totali = percorso.passiTotali
                )
            }
            Spacer(Modifier.height(Spaziature.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${percorso.passiCompletati}/${percorso.passiTotali} passi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${percorso.minutiTotali} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Spacer(Modifier.width(Spaziature.sm))
                Text(
                    percorso.data,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { percorso.progressoGiornata },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(Spaziature.sm))
            percorso.passi.forEach { passo ->
                PassoPianoRiga(
                    passo = passo,
                    onApri = { onApriPasso(passo) },
                    onCompleta = { onCompletaPasso(passo.id) }
                )
                Spacer(Modifier.height(6.dp))
            }
            if (percorso.percorsoCompletato) {
                Text(
                    "Piano completato. Se vuoi, fai un ripasso libero.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ProgressoOggiRing(
    progresso: Float,
    completati: Int,
    totali: Int
) {
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progresso.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(progresso * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "$completati/$totali",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PassoPianoRiga(
    passo: CalcoloPercorsoGiornaliero.PassoPercorso,
    onApri: () -> Unit,
    onCompleta: () -> Unit
) {
    val forma = MaterialTheme.shapes.medium
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .clickable(
                onClickLabel = "Apri ${passo.titolo}",
                role = Role.Button,
                onClick = onApri
            ),
        shape = forma,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (passo.completato) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (passo.completato) Icons.Filled.Check else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = if (passo.completato) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    passo.titolo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (passo.completato) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    passo.descrizione,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (passo.completato) {
                Text(
                    "Fatto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = onCompleta) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Segna ${passo.titolo} come fatto",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AzioneRapida(
    icona: ImageVector,
    etichetta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClickLabel = "Apri $etichetta",
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icona,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            etichetta,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LezioneHeaderCard(
    numero: Int,
    titolo: String,
    parole: Int,
    esercizi: Int,
    indice: Int,
    corrette: Int,
    progresso: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$numero",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(Spaziature.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        "LEZIONE $numero",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        titolo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        buildString {
                            append("$parole parole")
                            if (esercizi > 0) append(" · $esercizi esercizi")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (esercizi > 0) {
                Spacer(Modifier.height(Spaziature.sm))
                LinearProgressIndicator(
                    progress = { progresso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (indice >= esercizi) "Esercizi completati: $corrette/$esercizi"
                    else "Esercizio ${indice + 1} di $esercizi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PercorsoGrammaticaCard(
    prossima: UnitaConStato?,
    completate: Int,
    totale: Int,
    onApri: () -> Unit
) {
    val progresso = if (totale == 0) 0f else completate.toFloat() / totale
    val etichetta = when (prossima?.stato) {
        StatoUnita.DA_RIPASSARE -> "Ripasso in scadenza"
        StatoUnita.DISPONIBILE -> "Prossimo passo"
        StatoUnita.COMPLETATA -> "Percorso completato"
        StatoUnita.BLOCCATA -> "Unità bloccata"
        null -> "Percorso grammaticale"
    }
    val azione = when (prossima?.stato) {
        StatoUnita.DA_RIPASSARE -> "Ripassa ora"
        StatoUnita.DISPONIBILE -> "Continua il percorso"
        StatoUnita.COMPLETATA -> "Ripassa il percorso"
        StatoUnita.BLOCCATA -> "Apri il percorso"
        null -> "Apri il percorso"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(Spaziature.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        "PERCORSO GRAMMATICALE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                    )
                    Text(
                        prossima?.unita?.titolo ?: "Tutto il percorso",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                if (prossima != null) {
                    "${etichetta} · ${prossima.unita.livello} · ${prossima.unita.minuti} min"
                } else {
                    "Teoria, esempi ed esercizi in ordine"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(Spaziature.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$completate / $totale unità",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "A0 → B1",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(Spaziature.md))
            Button(
                onClick = onApri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale()),
                shape = MaterialTheme.shapes.large
            ) {
                Text(azione)
                Spacer(Modifier.width(Spaziature.sm))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TeoriaCard(
    contenuto: String,
    onAscolta: () -> Unit
) {
    var aperta by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = if (aperta) "Chiudi la teoria" else "Apri la teoria",
                        role = Role.Button
                    ) { aperta = !aperta }
                    .padding(Spaziature.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(Spaziature.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Teoria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (aperta) "Tocca per chiudere" else "Tocca per leggere la spiegazione",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (aperta) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (aperta) "Chiudi la teoria" else "Apri la teoria",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (aperta) {
                Column(
                    modifier = Modifier.padding(
                        start = Spaziature.md,
                        end = Spaziature.md,
                        bottom = Spaziature.md
                    )
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(Spaziature.sm))
                    MarkdownText(
                        markdown = contenuto,
                        color = MaterialTheme.colorScheme.onSurface,
                        stile = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Spaziature.sm))
                    TextButton(onClick = onAscolta) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Ascolta")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsiglioCard(
    titolo: String,
    motivo: String,
    onApri: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(Spaziature.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    "CONSIGLIO PER TE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    titolo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    motivo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Spaziature.xs))
            TextButton(onClick = onApri) {
                Text("Apri")
            }
        }
    }
}

/** Una riga del pannello dal basso: icona tonda + titolo + descrizione + freccia. */
@Composable
private fun VocePannello(
    icona: ImageVector,
    titolo: String,
    descrizione: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "Apri $titolo",
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = Spaziature.md, vertical = Spaziature.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icona,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(titolo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                descrizione,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
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
