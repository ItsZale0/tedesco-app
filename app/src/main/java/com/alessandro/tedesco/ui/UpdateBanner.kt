package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.UpdateState
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.data.remote.VersionInfo
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Banner di aggiornamento sempre in alto: tutte le azioni reali,
 * niente stato solo leggibile da Impostazioni.
 */
@Composable
fun UpdateBanner(updater: UpdaterViewModel) {
    val state by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)
    val profilo = state

    if (profilo is UpdateState.Idle || profilo is UpdateState.UpToDate) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(Spaziature.sm))
                Text(
                    when (profilo) {
                        is UpdateState.Checking -> "Controllo aggiornamenti…"
                        is UpdateState.Available -> "Aggiornamento disponibile: v${profilo.version.versionName}"
                        is UpdateState.Downloading -> "Download v${profilo.progress}%"
                        is UpdateState.ReadyToInstall -> "Aggiornamento pronto all'installazione"
                        is UpdateState.NeedPermission -> "Serve il permesso per installare app"
                        is UpdateState.Error -> "Errore aggiornamento"
                        else -> "Aggiornamento"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
                if (profilo is UpdateState.Available) {
                    IconButton(onClick = { updater.dismiss() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Ignora",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            when (profilo) {
                is UpdateState.Checking -> {
                    LinearProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(6.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                    )
                }
                is UpdateState.Available -> {
                    Text(
                        "Versione ${profilo.version.versionName} (${profilo.version.versionCode})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Button(
                        onClick = { updater.downloadUpdate(profilo.version) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) { Text("Scarica e installa") }
                }
                is UpdateState.Downloading -> {
                    Column(
                        Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { profilo.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                        )
                        Text(
                            "${profilo.progress}% scaricato",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Text(
                        "APK scaricato: ${profilo.file.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Button(
                        onClick = { updater.installUpdate(profilo.file) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) { Text("Installa ora") }
                }
                is UpdateState.NeedPermission -> {
                    Column(Modifier.padding(top = 8.dp)) {
                        TextButton(onClick = { updater.apriImpostazioniInstallazione() }) {
                            Text("Apri impostazioni di installazione")
                        }
                    }
                }
                is UpdateState.Error -> {
                    Text(
                        profilo.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    TextButton(onClick = { updater.checkForUpdate() }) { Text("Riprova") }
                }
                else -> {}
            }
        }
    }
}
