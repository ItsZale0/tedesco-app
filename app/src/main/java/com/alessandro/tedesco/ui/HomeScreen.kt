package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.CalcoloPercorsoAdattivo
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun PassoPercorsoItem(
    passo: CalcoloPercorsoGiornaliero.PassoPercorso,
    onCompleta: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (passo.completato) Icons.Filled.CheckCircle
            else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (passo.completato) "Completato" else "Da fare",
            tint = if (passo.completato) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = passo.titolo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (passo.completato) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = passo.descrizione,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!passo.completato) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Fatto",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: TedescoViewModel,
    onIniziaRipasso: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onCompletaPasso: (String) -> Unit = {}
) {
    val daRipassare by vm.daRipassare.collectAsStateWithLifecycle(0)
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioni by vm.lezioni.collectAsStateWithLifecycle(emptyList())
    val ultimoSync by vm.ultimoSync.collectAsStateWithLifecycle(null)
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val versioneApp = vm.versioneApp

    val oggi = remember { SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TitoloSchermata("Oggi") },
                actions = {
                    IconButton(onClick = { vm.sincronizza() }, enabled = !caricamento) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aggiorna ora")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spaziaturaSchermo()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto())
            ) {
                Spacer(Modifier.height(Spaziature.md))

                // Saluto con nome profilo e data
                Text(
                    text = "Ciao ${profilo?.config?.nomeVisualizzato ?: ""},",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = oggi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(Spaziature.lg))

                // Gamification: Streak e Badge
                val streakGiorni = profilo?.stato?.streakGiorni ?: 0
                val streakRecord = profilo?.stato?.streakRecord ?: 0
                val badgeSbloccati = profilo?.stato?.badgeSbloccati ?: emptyList()
                val puntiTotali = profilo?.stato?.puntiTotali ?: 0

                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(Spaziature.md)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥 Streak: $streakGiorni giorni",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🏆 $puntiTotali punti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (streakRecord > 0) {
                            Text(
                                text = "Record: $streakRecord giorni",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        if (badgeSbloccati.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Badge: ${badgeSbloccati.size} sbloccati",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Card principale: parole da ripassare
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spaziature.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = daRipassare.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(Spaziature.xs))
                        Text(
                            text = if (daRipassare == 0) "Nessun ripasso in scadenza"
                            else if (daRipassare == 1) "parola da ripassare"
                            else "parole da ripassare",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(Spaziature.lg))

                        Button(
                            onClick = {
                                vm.caricaSessione()
                                onIniziaRipasso()
                            },
                            enabled = !caricamento,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AltezzaBottonePrincipale()),
                            shape = RoundedCornerShape(Raggi.bottone)
                        ) {
                            Text(
                                text = if (daRipassare == 0) "Ripassa comunque" else "Inizia il ripasso",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spaziature.lg))

                // Percorso giornaliero: passi sequenziali e interattivi
                val percorsoGiorno by vm.percorsoGiornaliero.collectAsStateWithLifecycle(null)
                percorsoGiorno?.let { pg ->
                    GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.lg)
                        ) {
                            // Header con data e progresso
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Percorso di oggi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "${pg.passiCompletati}/${pg.passiTotali}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(Modifier.height(Spaziature.xs))
                            Text(
                                text = pg.data,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.height(Spaziature.sm))

                            // Progress bar
                            LinearProgressIndicator(
                                progress = { pg.progressoGiornata },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.height(Spaziature.sm))

                            // Messaggio motivazionale
                            Text(
                                text = pg.messaggioMotivazionale,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.height(Spaziature.xs))
                            Text(
                                text = "Prossimo obiettivo: ${pg.prossimoObiettivo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.height(Spaziature.sm))

                            // Lista passi
                            pg.passi.forEach { passo ->
                                PassoPercorsoItem(
                                    passo = passo,
                                    onCompleta = { onCompletaPasso(passo.id) }
                                )
                            }

                            // Footer con tempo totale
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                text = "Tempo stimato: ${pg.minutiTotali} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(Spaziature.lg))
                }

                // Riga di 3 riquadri compatti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                ) {
                    RiquadroCompact(
                        label = "Parole",
                        valore = parole.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    RiquadroCompact(
                        label = "Lezioni",
                        valore = lezioni.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    RiquadroCompact(
                        label = "Da ripassare",
                        valore = daRipassare.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(Spaziature.lg))

                // Sezione 'Come procede'
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spaziature.md)
                    ) {
                        Text(
                            text = "Come procede",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Spaziature.sm))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Versione app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v$versioneApp",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(Spaziature.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vocabolario aggiornato",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = TedescoViewModel.formattaData(ultimoSync?.syncedAt ?: 0L),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spaziature.lg))

                // Indicatore di attività discreto
                if (caricamento) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            text = "Aggiorno…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(Spaziature.xxl))
            }
        }
    }
}

@Composable
private fun RiquadroCompact(
    label: String,
    valore: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spaziature.md, horizontal = Spaziature.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valore,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
