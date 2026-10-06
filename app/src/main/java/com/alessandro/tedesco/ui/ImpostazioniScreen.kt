package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.data.remote.UpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpostazioniScreen(vm: TedescoViewModel, updater: UpdaterViewModel) {
    val urlCorrente by vm.feedUrl.collectAsStateWithLifecycle("")
    val ultimoSync by vm.ultimoSync.collectAsStateWithLifecycle(null)

    var url by remember(urlCorrente) { mutableStateOf(urlCorrente) }
    var confermaReset by remember { mutableStateOf(false) }

    val updateState by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Impostazioni") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Sorgente del vocabolario",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Le parole le pubblico io. L'app controlla questo indirizzo ogni 15 minuti.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Indirizzo del JSON") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { vm.salvaUrl(url) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salva e aggiorna") }

            Spacer(Modifier.height(28.dp))

            // --- Auto-updater sezione ---
            Text(
                "Aggiornamenti app",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "L'app controlla automaticamente all'apertura. Puoi forzare il controllo qui.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { updater.checkForUpdate() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Controlla aggiornamenti ora") }

            Spacer(Modifier.height(8.dp))

            when (val s = updateState) {
                is UpdateState.Checking -> {
                    Text("Controllo in corso...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                is UpdateState.Available -> {
                    Text("Disponibile v${s.version.versionName}: ${s.version.changelog}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                is UpdateState.Error -> {
                    Text("Errore: ${s.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    Text("Nessun aggiornamento disponibile", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(28.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Ultimo aggiornamento vocabolario",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        TedescoViewModel.formattaData(ultimoSync?.syncedAt ?: 0L),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    ultimoSync?.message?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Come funziona il ripasso",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Quando rispondi giusto l'intervallo si allunga: 1, 3, 7, 16, 35, 75, " +
                    "150, 300 giorni. Quando sbagli torna a domani.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { confermaReset = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) { Text("Cancella tutti i dati") }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (confermaReset) {
        AlertDialog(
            onDismissRequest = { confermaReset = false },
            title = { Text("Cancellare tutto?") },
            text = {
                Text(
                    "Vengono rimosse tutte le parole e le ripetizioni. " +
                        "Poi l'app riscarica il vocabolario dal server."
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
}