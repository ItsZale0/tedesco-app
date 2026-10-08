package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.SessioneEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@Composable
private fun iconaPerTipo(tipo: String): ImageVector = when (tipo.uppercase()) {
    "SESSIONE" -> Icons.Filled.School
    "TEST" -> Icons.Filled.Quiz
    "ROLEPLAY" -> Icons.Filled.Chat
    "RIPASSO" -> Icons.Filled.Refresh
    else -> Icons.Filled.PlayArrow
}

@Composable
private fun colorePerTipo(tipo: String) = when (tipo.uppercase()) {
    "SESSIONE" -> MaterialTheme.colorScheme.primaryContainer
    "TEST" -> MaterialTheme.colorScheme.secondaryContainer
    "ROLEPLAY" -> MaterialTheme.colorScheme.tertiaryContainer
    "RIPASSO" -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.surfaceVariant
}

/**
 * Sessioni strutturate: SESSIONE, TEST, ROLEPLAY, RIPASSO.
 * I comandi che prima si mandavano su WhatsApp, ora sono pulsanti.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessioniScreen(
    onIndietro: () -> Unit,
    sessioni: List<SessioneEntity>,
    onAvvia: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessioni") },
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

                Text(
                    "Scegli come studiare oggi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (sessioni.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Sessioni non ancora caricate. Verranno sincronizzate al prossimo aggiornamento.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    sessioni.forEach { sessione ->
                        val tipo = sessione.tipo.uppercase()
                        Card(
                            onClick = { onAvvia(sessione.tipo) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colorePerTipo(tipo)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    iconaPerTipo(tipo),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        sessione.titolo,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        sessione.descrizione,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${sessione.durata} minuti",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
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
