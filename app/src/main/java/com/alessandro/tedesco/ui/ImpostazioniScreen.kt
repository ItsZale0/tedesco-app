package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.remote.UpdateState
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpostazioniScreen(vm: TedescoViewModel, updater: UpdaterViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val profili by vm.profiliDisponibili.collectAsStateWithLifecycle(emptyList())
    val customWords by vm.enableCustomWords.collectAsStateWithLifecycle(false)
    val googleSheets by vm.enableGoogleSheets.collectAsStateWithLifecycle(false)
    val ultimoSync by vm.ultimoSync.collectAsStateWithLifecycle(null)
    val updateState by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)

    var cambiaProfilo by remember { mutableStateOf(false) }
    var esciSelezione by remember { mutableStateOf(false) }
    var confermaReset by remember { mutableStateOf(false) }
    var aggiungiParola by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profilo") }) }
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

                // --- Profilo attivo ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            "Profilo attivo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            profilo?.config?.nomeVisualizzato ?: "—",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        profilo?.config?.tipo?.descrizione?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { cambiaProfilo = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cambia profilo") }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { esciSelezione = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Torna alla scelta del profilo") }

                Spacer(Modifier.height(28.dp))

                // --- Livello ---
                Text("Livello", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Scegli il tuo livello attuale. I contenuti si adatteranno.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LivelloSelector(
                    livelloAttuale = profilo?.stato?.progresso?.livelloCorrente
                        ?: com.alessandro.tedesco.data.local.LivelloCEFR.A0,
                    onLivelloCambiato = { nuovoLivello ->
                        vm.cambiaLivello(nuovoLivello)
                    }
                )

                Spacer(Modifier.height(28.dp))

                // --- Parole personalizzate (solo se il profilo le prevede) ---
                if (customWords) {
                    Text("Parole personalizzate", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (googleSheets) {
                            "Puoi aggiungere parole tue. Restano separate dal vocabolario del corso e vengono ripassate come le altre."
                        } else {
                            "Puoi aggiungere parole tue, ripassate insieme a quelle del corso."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { aggiungiParola = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Aggiungi una parola") }

                    Spacer(Modifier.height(28.dp))
                }

                // --- Personalizzazione feed e documento (solo per profili personalizzati) ---
                if (customWords) {
                    Text("Personalizzazione", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Configura il tuo feed vocabolario e il tuo documento guida.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    var feedUrl by remember { mutableStateOf(profilo?.config?.feedUrl ?: "") }
                    var guidaDocId by remember { mutableStateOf(profilo?.config?.guidaDocId ?: "") }

                    OutlinedTextField(
                        value = feedUrl,
                        onValueChange = { feedUrl = it },
                        label = { Text("Feed URL (vocabolario JSON)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = guidaDocId,
                        onValueChange = { guidaDocId = it },
                        label = { Text("Documento Google (ID o URL)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            vm.aggiornaFeedUrl(feedUrl)
                            vm.aggiornaGuidaDocId(guidaDocId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salva personalizzazione") }

                    Spacer(Modifier.height(28.dp))
                }

                // --- Tutor AI ---
                Text("Tutor AI", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Inserisci la chiave OpenRouter per usare il chatbot tutor. " +
                        "Resta salvata solo su questo telefono, nel tuo profilo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                var chiaveTutor by remember { mutableStateOf(profilo?.config?.tutorApiKey ?: "") }
                OutlinedTextField(
                    value = chiaveTutor,
                    onValueChange = { chiaveTutor = it },
                    label = { Text("Chiave OpenRouter") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { vm.aggiornaTutorApiKey(chiaveTutor) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Salva chiave") }

                Spacer(Modifier.height(28.dp))

                // --- Aggiornamenti ---
                Text("Aggiornamenti", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "L'app controlla all'apertura. Puoi forzare il controllo qui.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { updater.checkForUpdate() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Controlla aggiornamenti ora") }

                Spacer(Modifier.height(10.dp))

                val statoUpd = when (val s = updateState) {
                    is UpdateState.Checking -> "Controllo in corso…" to false
                    is UpdateState.Available -> "Disponibile v${s.version.versionName}" to false
                    is UpdateState.Error -> "Errore: ${s.message}" to true
                    else -> "Nessun aggiornamento disponibile" to false
                }
                Text(
                    statoUpd.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (statoUpd.second) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(28.dp))

                // --- Vocabolario ---
                Text("Vocabolario", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ultimo aggiornamento: ${TedescoViewModel.formattaData(ultimoSync?.syncedAt ?: 0L)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ultimoSync?.message?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { vm.sincronizza() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Sincronizza adesso") }

                Spacer(Modifier.height(28.dp))

                // --- Come funziona il ripasso ---
                Text("Come funziona il ripasso", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Quando rispondi giusto l'intervallo si allunga: 1, 3, 7, 16, 35, 75, 150, 300 giorni. " +
                        "Quando sbagli torna a domani.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(36.dp))

                Button(
                    onClick = { confermaReset = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) { Text("Cancella i dati di questo profilo") }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Dialog cambio profilo
    if (cambiaProfilo) {
        AlertDialog(
            onDismissRequest = { cambiaProfilo = false },
            title = { Text("Cambia profilo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Scegli con quale profilo continuare. Ogni profilo ha le sue parole e i suoi progressi.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    profili.forEach { p ->
                        FilterChip(
                            selected = p.id == profilo?.id,
                            onClick = {
                                cambiaProfilo = false
                                if (p.id != profilo?.id) vm.selezionaProfilo(p.id)
                            },
                            label = { Text(p.config.nomeVisualizzato) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { cambiaProfilo = false }) { Text("Chiudi") }
            }
        )
    }

    // Dialog conferma uscita verso la selezione profilo
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
                TextButton(onClick = {
                    esciSelezione = false
                    vm.esciDalProfilo()
                }) { Text("Torna") }
            },
            dismissButton = {
                TextButton(onClick = { esciSelezione = false }) { Text("Annulla") }
            }
        )
    }

    // Dialog conferma reset
    if (confermaReset) {
        AlertDialog(
            onDismissRequest = { confermaReset = false },
            title = { Text("Cancellare i dati?") },
            text = {
                Text(
                    "Vengono rimosse parole e ripetizioni solo di questo profilo. " +
                        "Gli altri profili non vengono toccati."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confermaReset = false
                    vm.reset()
                }) { Text("Cancella") }
            },
            dismissButton = {
                TextButton(onClick = { confermaReset = false }) { Text("Annulla") }
            }
        )
    }

    // Dialog aggiungi parola
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
private fun LivelloSelector(
    livelloAttuale: LivelloCEFR,
    onLivelloCambiato: (LivelloCEFR) -> Unit
) {
    val livelli = listOf(
        LivelloCEFR.A0 to "A0 - Principiante assoluto",
        LivelloCEFR.A1 to "A1 - Principiante",
        LivelloCEFR.A2 to "A2 - Elementare",
        LivelloCEFR.B1 to "B1 - Intermedio",
        LivelloCEFR.B2 to "B2 - Intermedio superiore"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            livelli.forEach { (livello, descrizione) ->
                val selezionato = livello == livelloAttuale
                FilterChip(
                    selected = selezionato,
                    onClick = { onLivelloCambiato(livello) },
                    label = { Text(descrizione) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
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
    var tedesco by remember { mutableStateOf("") }
    var italiano by remember { mutableStateOf("") }
    var frase by remember { mutableStateOf("") }
    var articolo by remember { mutableStateOf("") }
    var pronuncia by remember { mutableStateOf("") }
    var lezione by remember { mutableStateOf("") }

    val valido = tedesco.isNotBlank() && italiano.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova parola") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tedesco,
                    onValueChange = { tedesco = it },
                    label = { Text("Tedesco *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = italiano,
                    onValueChange = { italiano = it },
                    label = { Text("Italiano *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = articolo,
                    onValueChange = { articolo = it },
                    label = { Text("Articolo (der/die/das)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pronuncia,
                    onValueChange = { pronuncia = it },
                    label = { Text("Pronuncia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = frase,
                    onValueChange = { frase = it },
                    label = { Text("Frase d'esempio") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lezione,
                    onValueChange = { lezione = it.filter { c -> c.isDigit() } },
                    label = { Text("Lezione (numero)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConferma(
                        tedesco,
                        italiano,
                        frase,
                        articolo.ifBlank { null },
                        pronuncia.ifBlank { null },
                        lezione.toIntOrNull() ?: 0
                    )
                },
                enabled = valido
            ) { Text("Aggiungi") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
