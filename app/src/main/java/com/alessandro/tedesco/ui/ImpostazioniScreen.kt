@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.tedesco.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.remote.UpdateState
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.ui.theme.PaletteApp
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

private const val URL_VOCAB_DEFAULT =
    "https://raw.githubusercontent.com/ItsZale0/tedesco-vocab/main/vokabeln.json"
private const val GUIDA_DOC_DEFAULT = "12yKY4Bpp6IqX7q8tgNYkFXIoAQsZR8yVd4mZhcD5I7g"

private val GlassShape: Shape = RoundedCornerShape(Raggi.card)

@Composable
private fun settingsGlassColor(): Color {
    val base = MaterialTheme.colorScheme.surface
    return if (isSystemInDarkTheme()) base.copy(alpha = 0.36f) else base.copy(alpha = 0.52f)
}

private val borderColor: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

/** Superficie "liquid glass": trasparente, sottile bordo theme-tint, leggera ombra. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = settingsGlassColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor)
    ) { Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), content = { content() }) }
}

/** Sezione impostazioni a tendina: header cliccabile con chevron + contenuto espandibile. */
@Composable
private fun SettingsExpandableSection(
    title: String,
    description: String? = null,
    defaultExpanded: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(defaultExpanded) }
    Card(
        modifier = modifier.fillMaxWidth().clip(GlassShape),
        shape = GlassShape,
        colors = CardDefaults.cardColors(containerColor = settingsGlassColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    description?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (expanded) "Chiudi" else "Apri",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun <T : Any> DropdownSelector(
    label: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.find { it.first == selected }?.second ?: "-"
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = currentLabel,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = GlassShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = GlassShape,
            border = BorderStroke(1.dp, borderColor)
        ) {
            options.forEach { (value, name) ->
                DropdownMenuItem(
                    onClick = { onSelect(value); expanded = false },
                    text = {
                        Text(
                            name,
                            color = if (value == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(44.dp).clip(GlassShape),
        enabled = enabled,
        colors = colors,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) { content() }
}

@Composable
internal fun GlassTopBar(title: String) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {}
}

@Composable
fun ImpostazioniScreen(vm: TedescoViewModel, updater: UpdaterViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val profili by vm.profiliDisponibili.collectAsStateWithLifecycle(emptyList())
    val customWords by vm.enableCustomWords.collectAsStateWithLifecycle(false)
    val googleSheets by vm.enableGoogleSheets.collectAsStateWithLifecycle(false)
    val ultimoSync by vm.ultimoSync.collectAsStateWithLifecycle(null)
    val updateState by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)
    val paletteId by vm.palette.collectAsStateWithLifecycle("uber")

    var cambiaProfilo by remember { mutableStateOf(false) }
    var esciSelezione by remember { mutableStateOf(false) }
    var confermaReset by remember { mutableStateOf(false) }
    var aggiungiParola by remember { mutableStateOf(false) }

    var feedUrl by rememberSaveable { mutableStateOf(profilo?.config?.feedUrl ?: "") }
    var guidaDocId by rememberSaveable { mutableStateOf(profilo?.config?.guidaDocId ?: "") }
    var chiaveTutor by rememberSaveable { mutableStateOf(profilo?.config?.tutorApiKey ?: "") }

    Scaffold(topBar = { GlassTopBar("Impostazioni") }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spaziaturaSchermo())
        ) {
            Column(Modifier.fillMaxWidth()) {

                // --- PROFILO ATTIVO ---
                GlassCard {
                    Column {
                        Text("Profilo attivo", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            profilo?.config?.nomeVisualizzato ?: "—",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        profilo?.config?.tipo?.descrizione?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SettingsButton(onClick = { cambiaProfilo = true }) { Text("Cambia profilo") }
                Spacer(Modifier.height(8.dp))
                SettingsButton(onClick = { esciSelezione = true }) { Text("Torna alla scelta del profilo") }
                Spacer(Modifier.height(16.dp))

                // --- LIVELLO (menu a tendina) ---
                SettingsExpandableSection(
                    title = "Livello",
                    description = "Il contenuto si adatta al tuo livello CEFR."
                ) {
                    DropdownSelector(
                        label = "Livello CEFR",
                        options = listOf(
                            LivelloCEFR.A0 to "A0 – Principiante assoluto",
                            LivelloCEFR.A1 to "A1 – Principiante",
                            LivelloCEFR.A2 to "A2 – Elementare",
                            LivelloCEFR.B1 to "B1 – Intermedio",
                            LivelloCEFR.B2 to "B2 – Intermedio superiore"
                        ),
                        selected = profilo?.stato?.progresso?.livelloCorrente,
                        onSelect = { vm.cambiaLivello(it) }
                    )
                }
                Spacer(Modifier.height(16.dp))

                // --- VOCABOLARIO PERSONALIZZATO (solo profili custom) ---
                if (customWords) {
                    SettingsExpandableSection(
                        title = "Parole personalizzate",
                        description = if (googleSheets)
                            "Le tue parole sono sincronizzate da Google Sheets."
                        else "Aggiungi parole tue da ripassare con il resto."
                    ) {
                        SettingsButton(onClick = { aggiungiParola = true }) { Text("Aggiungi una parola") }
                    }
                    Spacer(Modifier.height(16.dp))

                    SettingsExpandableSection(
                        title = "Il tuo vocabolario",
                        description = "Usa un vocabolario personalizzato invece del vocabolario di default."
                    ) {
                        OutlinedTextField(
                            value = feedUrl, onValueChange = { feedUrl = it },
                            label = { Text("URL vocabolario") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("https://raw.githubusercontent.com/...") },
                            shape = GlassShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { feedUrl = URL_VOCAB_DEFAULT }) {
                            Text("Usa il vocabolario di Alessandro")
                        }
                        TutorialGrigio(
                            testo = "Come creare il TUO vocabolario su GitHub:\n\n" +
                                "1. Vai su github.com e accedi (o crea un account gratis)\n" +
                                "2. Crea un repository pubblico\n" +
                                "3. Carica un file vokabeln.json\n" +
                                "4. Copia l'URL Raw e incollalo qui\n\n" +
                                "Formato: {\"words\":[{\"id\":\"1\",\"german\":\"der Hund\",\"" +
                                "\"italian\":\"il cane\",\"level\":\"A1\",\"lesson\":1}]}"
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = guidaDocId, onValueChange = { guidaDocId = it },
                            label = { Text("Documento Google della guida") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("ID o URL del documento") },
                            shape = GlassShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { guidaDocId = GUIDA_DOC_DEFAULT }) {
                            Text("Usa la guida di Alessandro")
                        }
                        TutorialGrigio(
                            testo = "Come usare il TUO documento Google:\n\n" +
                                "1. Apri Google Docs e crea un documento\n" +
                                "2. Scrivi la guida (usa i Titoli)\n" +
                                "3. Condividi → Chiunque abbia il link → Visualizzatore\n" +
                                "4. Copia l'indirizzo e incollalo qui"
                        )
                        Spacer(Modifier.height(16.dp))
                        SettingsButton(onClick = {
                            vm.aggiornaFeedUrl(feedUrl)
                            vm.aggiornaGuidaDocId(guidaDocId)
                        }) { Text("Salva") }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // --- TUTOR AI ---
                SettingsExpandableSection(
                    title = "Tutor AI",
                    description = "Il tutor risponde alle tue domande usando modelli gratuiti OpenRouter."
                ) {
                    OutlinedTextField(
                        value = chiaveTutor, onValueChange = { chiaveTutor = it },
                        label = { Text("Chiave OpenRouter") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("sk-or-v1-...") },
                        shape = GlassShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    TutorialGrigio(
                        testo = "L'app include una chiave OpenRouter di default per il tutor AI.\n\n" +
                            "Puoi usarla così com'è oppure inserire la tua chiave personale:\n\n" +
                            "1. Vai su openrouter.ai e accedi\n" +
                            "2. Profile → Keys → Create Key\n" +
                            "3. Copia la chiave (sk-or-v1-...) e incollala qui\n\n" +
                            "L'app usa solo modelli gratuiti: non spenderai nulla."
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsButton(onClick = { vm.aggiornaTutorApiKey(chiaveTutor) }) { Text("Salva chiave") }
                }
                Spacer(Modifier.height(16.dp))

                // --- AGGIORNAMENTI ---
                SettingsExpandableSection(
                    title = "Aggiornamenti",
                    description = "L'app controlla all'apertura. Puoi forzare il controllo."
                ) {
                    SettingsButton(onClick = { updater.checkForUpdate() }) { Text("Controlla aggiornamenti ora") }
                    Spacer(Modifier.height(8.dp))
                    val statoUpd = when (val s = updateState) {
                        is UpdateState.Checking -> "Controllo in corso…" to false
                        is UpdateState.Available -> "Disponibile v${s.version.versionName}" to false
                        is UpdateState.Error -> "Errore: ${s.message}" to true
                        else -> "Nessun aggiornamento disponibile" to false
                    }
                    Text(
                        statoUpd.first, style = MaterialTheme.typography.bodySmall,
                        color = if (statoUpd.second) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))

                // --- VOCABOLARIO ---
                SettingsExpandableSection(
                    title = "Vocabolario",
                    description = "Ultimo aggiornamento: ${TedescoViewModel.formattaData(ultimoSync?.syncedAt ?: 0L)}"
                ) {
                    ultimoSync?.message?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                    }
                    SettingsButton(onClick = { vm.sincronizza() }) { Text("Sincronizza adesso") }
                }
                Spacer(Modifier.height(16.dp))

                // --- COLORE DELL'APP (menu a tendina) ---
                SettingsExpandableSection(
                    title = "Colore dell'app",
                    description = "Scegli il colore dell'interfaccia."
                ) {
                    DropdownSelector(
                        label = "Palette",
                        options = PaletteApp.entries.map { it.id to it.nome },
                        selected = paletteId,
                        onSelect = { vm.cambiaPalette(it) }
                    )
                }
                Spacer(Modifier.height(16.dp))

                // --- RIPASSO ---
                SettingsExpandableSection(
                    title = "Come funziona il ripasso",
                    description = "Spaced repetition: gli intervalli crescono con le risposte corrette."
                ) {
                    Text(
                        "Quando rispondi giusto l'intervallo si allunga: 1, 3, 7, 16, 35, 75, " +
                            "150, 300 giorni. Quando sbagli torna a domani.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))

                Spacer(Modifier.height(8.dp))

                // --- CANCELLA DATI ---
                SettingsExpandableSection(
                    title = "Area pericolosa",
                    description = "Cancella i dati di questo profilo (gli altri profili non sono toccati)."
                ) {
                    Spacer(Modifier.height(8.dp))
                    SettingsButton(
                        onClick = { confermaReset = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) { Text("Cancella i dati di questo profilo") }
                }
            }
        }
    }

    // ---- Dialoghi (stile glass) ----
    if (cambiaProfilo) {
        GlassChangeProfileDialog(
            profili = profili,
            selezionato = profilo?.id,
            onDismiss = { cambiaProfilo = false },
            onSelect = { vm.selezionaProfilo(it); cambiaProfilo = false }
        )
    }
    if (esciSelezione) {
        AlertDialog(
            onDismissRequest = { esciSelezione = false },
            title = { Text("Tornare alla scelta del profilo?") },
            text = {
                Text(
                    "I dati di ${profilo?.config?.nomeVisualizzato ?: "questo profilo"} " +
                        "restano salvati. Potrai rientrare quando vuoi."
                )
            },
            confirmButton = {
                TextButton(onClick = { esciSelezione = false; vm.esciDalProfilo() }) { Text("Torna") }
            },
            dismissButton = { TextButton(onClick = { esciSelezione = false }) { Text("Annulla") } }
        )
    }
    if (confermaReset) {
        AlertDialog(
            onDismissRequest = { confermaReset = false },
            title = { Text("Cancellare i dati?") },
            text = {
                Text(
                    "Vengono rimossi vocaboli e ripetizioni solo di questo profilo. " +
                        "Gli altri profili non vengono toccati."
                )
            },
            confirmButton = {
                TextButton(onClick = { confermaReset = false; vm.reset() }) { Text("Cancella") }
            },
            dismissButton = { TextButton(onClick = { confermaReset = false }) { Text("Annulla") } }
        )
    }
    if (aggiungiParola) {
        AggiungiParolaDialog(
            onDismiss = { aggiungiParola = false },
            onConferma = { de, italiano, frase, articolo, pronuncia, lezione ->
                vm.aggiungiParolaCustom(de, italiano, frase, articolo, pronuncia, lezione, "")
                aggiungiParola = false
            }
        )
    }
}

@Composable
private fun GlassChangeProfileDialog(
    profili: List<ProfiloUtente>,
    selezionato: String?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambia profilo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scegli con quale profilo continuare.", style = MaterialTheme.typography.bodySmall)
                profili.forEach { p ->
                    SettingsButton(onClick = { onSelect(p.id) }) {
                        Text(p.config.nomeVisualizzato)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } }
    )
}

@Composable
private fun TutorialGrigio(testo: String) {
    var aperto by remember { mutableStateOf(false) }
    Spacer(Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GlassShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            TextButton(onClick = { aperto = !aperto }) {
                Text(
                    text = if (aperto) "▾ Come si fa?" else "▸ Come si fa?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (aperto) {
                Text(
                    text = testo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AggiungiParolaDialog(
    onDismiss: () -> Unit,
    onConferma: (String, String, String, String?, String?, Int) -> Unit
) {
    var tedesco by rememberSaveable { mutableStateOf("") }
    var italiano by rememberSaveable { mutableStateOf("") }
    var frase by rememberSaveable { mutableStateOf("") }
    var articolo by rememberSaveable { mutableStateOf("") }
    var pronuncia by rememberSaveable { mutableStateOf("") }
    var lezione by rememberSaveable { mutableStateOf("") }

    val valido = tedesco.isNotBlank() && italiano.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova parola") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = tedesco, onValueChange = { tedesco = it },
                    label = { Text("Tedesco *") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = italiano, onValueChange = { italiano = it },
                    label = { Text("Italiano *") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = articolo, onValueChange = { articolo = it },
                    label = { Text("Articolo (der/die/das)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = pronuncia, onValueChange = { pronuncia = it },
                    label = { Text("Pronuncia") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = frase, onValueChange = { frase = it },
                    label = { Text("Frase d'esempio") }, modifier = Modifier.fillMaxWidth(), shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = lezione, onValueChange = { lezione = it.filter { c -> c.isDigit() } },
                    label = { Text("Lezione (numero)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConferma(tedesco, italiano, frase,
                        articolo.ifBlank { null }, pronuncia.ifBlank { null },
                        lezione.toIntOrNull() ?: 0)
                },
                enabled = valido
            ) { Text("Aggiungi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}
