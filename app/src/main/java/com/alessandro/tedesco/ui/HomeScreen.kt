package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.SessionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: TedescoViewModel) {
    val daRipassare by vm.daRipassare.collectAsStateWithLifecycle(0)
    val ultimoSync by vm.ultimoSync.collectAsStateWithLifecycle(null)
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)
    val sessione by vm.sessione.collectAsStateWithLifecycle(SessionState())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Tedesco") },
                actions = {
                    IconButton(
                        onClick = { vm.sincronizza() },
                        enabled = !caricamento
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aggiorna ora")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = daRipassare.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (daRipassare == 1) "parola da ripassare"
                else "parole da ripassare",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { vm.caricaSessione() },
                enabled = !caricamento && daRipassare > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Inizia il ripasso", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(24.dp))

            if (caricamento) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
                Text("Aggiorno...", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(32.dp))

            SyncStatus(ultimoSync)
        }
    }
}

@Composable
private fun SyncStatus(log: com.alessandro.tedesco.data.local.FeedLogEntity?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (log == null) "Nessun aggiornamento"
            else "Aggiornato: ${TedescoViewModel.formattaData(log.syncedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        log?.message?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}