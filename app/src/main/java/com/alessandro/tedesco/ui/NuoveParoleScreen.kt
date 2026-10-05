package com.alessandro.tedesco.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.WordEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuoveParoleScreen(vm: TedescoViewModel) {
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioni by vm.lezioni.collectAsStateWithLifecycle(emptyList())
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)

    var ricerca by remember { mutableStateOf("") }
    var lezioneFiltrata by remember { mutableStateOf<Int?>(null) }

    val filtrate = parole.filter { w ->
        val okTesto = ricerca.isBlank() ||
            w.german.contains(ricerca, ignoreCase = true) ||
            w.italian.contains(ricerca, ignoreCase = true)
        val okLezione = lezioneFiltrata == null || w.lesson == lezioneFiltrata
        okTesto && okLezione
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Parole") },
                actions = {
                    IconButton(onClick = { vm.sincronizza() }, enabled = !caricamento) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aggiorna ora")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = ricerca,
                onValueChange = { ricerca = it },
                label = { Text("Cerca") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = lezioneFiltrata == null,
                        onClick = { lezioneFiltrata = null },
                        label = { Text("Tutte") }
                    )
                }
                items(lezioni) { l ->
                    FilterChip(
                        selected = lezioneFiltrata == l,
                        onClick = {
                            lezioneFiltrata = if (lezioneFiltrata == l) null else l
                        },
                        label = { Text("L${l.toString().padStart(2, '0')}") }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (caricamento && parole.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
            } else if (filtrate.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nessuna parola",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tocca l'icona di aggiornamento per scaricare il vocabolario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "${filtrate.size} parole",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrate, key = { it.id }) { w ->
                        WordRow(w) { vm.archivia(w.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordRow(w: WordEntity, onArchivia: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = w.german,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { ttsHelper.speak(w.german) }) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "Ascolta pronuncia",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = w.italian,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                w.example.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onArchivia) { Text("Archivia") }
        }
    }
}