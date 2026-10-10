package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.AdaptiveSessionEngine
import com.alessandro.tedesco.data.local.SessioneEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

@Composable
private fun iconaPerTipo(tipo: String): ImageVector = when (tipo.uppercase()) {
    "SESSIONE", "SESSIONE_GUIDATA" -> Icons.Filled.School
    "TEST", "TEST_LIVELLO" -> Icons.Filled.Quiz
    "ROLEPLAY" -> Icons.AutoMirrored.Filled.Chat
    "RIPASSO" -> Icons.Filled.Refresh
    "GRAMMATICA" -> Icons.Filled.School
    "COMPRENSIONE" -> Icons.Filled.Quiz
    "PRODUZIONE" -> Icons.Filled.Quiz
    "VOCABOLARIO" -> Icons.Filled.PlayArrow
    else -> Icons.Filled.PlayArrow
}

@Composable
private fun colorePerTipo(tipo: String) = when (tipo.uppercase()) {
    "SESSIONE", "SESSIONE_GUIDATA" -> MaterialTheme.colorScheme.primaryContainer
    "TEST", "TEST_LIVELLO" -> MaterialTheme.colorScheme.secondaryContainer
    "ROLEPLAY" -> MaterialTheme.colorScheme.tertiaryContainer
    "RIPASSO" -> MaterialTheme.colorScheme.surfaceVariant
    "GRAMMATICA" -> MaterialTheme.colorScheme.primaryContainer
    "COMPRENSIONE" -> MaterialTheme.colorScheme.secondaryContainer
    "PRODUZIONE" -> MaterialTheme.colorScheme.tertiaryContainer
    "VOCABOLARIO" -> MaterialTheme.colorScheme.surfaceVariant
    else -> MaterialTheme.colorScheme.surfaceVariant
}

/**
 * Sessioni strutturate: SESSIONE, TEST, ROLEPLAY, RIPASSO.
 * I comandi che prima si mandavano su WhatsApp, ora sono pulsanti.
 *
 * Mostra le sessioni adattive generate dinamicamente dai progressi dell'utente,
 * con fallback alle sessioni statiche dal feed se il motore adattivo non è pronto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessioniScreen(
    onIndietro: () -> Unit,
    sessioni: List<SessioneEntity>,
    sessioniAdattive: AdaptiveSessionEngine.RisultatoGenerazione?,
    onAvvia: (String) -> Unit
) {
    ScreenScaffold("Sessioni", onBack = onIndietro) {

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

                // Sessioni adattive generate dinamicamente
                if (sessioniAdattive != null) {
                    // Header con messaggio motivazionale
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                sessioniAdattive.messaggioMotivazionale,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (sessioniAdattive.areeDeboli.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Aree deboli: ${sessioniAdattive.areeDeboli.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tempo totale: ~${sessioniAdattive.tempoTotaleMinuti} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Lista sessioni adattive
                    sessioniAdattive.sessioni.forEach { sessione ->
                        val tipo = sessione.tipo.name
                        Card(
                            onClick = { onAvvia(sessione.azione) },
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            sessione.titolo,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (sessione.urgente) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.tertiaryContainer
                                            ) {
                                                Text(
                                                    "Priorità",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        sessione.descrizione,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "~${sessione.durataMinuti} min",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } else if (sessioni.isEmpty()) {
                    // Fallback: nessuna sessione disponibile
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Sessioni non ancora caricate. Verranno sincronizzate al prossimo aggiornamento.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    // Fallback: sessioni statiche dal feed
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
