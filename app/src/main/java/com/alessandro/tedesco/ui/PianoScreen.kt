package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

/**
 * Piano di studio verso il B2: tappe, certificazioni, risorse, progresso.
 * Tutto quello che prima arrivava su WhatsApp, ora dentro l'app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PianoScreen(
    onIndietro: () -> Unit,
    piano: PianoEntity?,
    progresso: ProgressoFeedEntity?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Piano di studio") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spaziaturaSchermo()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Obiettivo
                if (piano != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = piano.obiettivo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Orizzonte: ${piano.orizzonte}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Progresso
                if (progresso != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Il tuo progresso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatBox("${progresso.streakCorrente}", "Streak")
                                StatBox("${progresso.lezioneCorrente}", "Lezione")
                                StatBox("${progresso.paroleMature}", "Parole")
                            }
                        }
                    }
                }

                // Tappe
                if (piano != null && piano.tappe.isNotEmpty()) {
                    Text(
                        "Tappe del percorso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    piano.tappe.forEach { tappa ->
                        TappaCard(
                            nome = tappa.nome,
                            descrizione = tappa.descrizione,
                            lezioni = tappa.lezioni,
                            stato = tappa.stato
                        )
                    }
                }

                // Certificazioni
                if (piano != null && piano.certificazioni.isNotEmpty()) {
                    Text(
                        "Certificazioni riconosciute",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    piano.certificazioni.forEach { cert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        cert.nome,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        cert.ente,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        cert.note,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Risorse
                if (piano != null && piano.risorse.isNotEmpty()) {
                    Text(
                        "Risorse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    piano.risorse.forEach { risorsa ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        risorsa.nome,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        risorsa.nota,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatBox(valore: String, etichetta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valore,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            etichetta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TappaCard(nome: String, descrizione: String, lezioni: String, stato: String) {
    val colore = when (stato) {
        "fatto" -> MaterialTheme.colorScheme.secondaryContainer
        "in corso" -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colore)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$nome · $stato",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    descrizione,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Lezioni $lezioni",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
