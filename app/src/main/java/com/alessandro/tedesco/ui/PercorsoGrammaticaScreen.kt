@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.StatoUnita
import com.alessandro.tedesco.data.UnitaConStato
import com.alessandro.tedesco.data.UnitaGrammatica
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Percorso grammaticale strutturato.
 *
 * Mostra sempre qual è il prossimo passo, separa le unità per livello e
 * rende evidente perché una unità è bloccata. Non è una lista di quiz:
 * ogni riga porta a teoria + esercizi + verifica di padronanza.
 */
@Composable
fun PercorsoGrammaticaScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit,
    onApriUnita: (String) -> Unit
) {
    val percorso by vm.percorsoGrammatica.collectAsStateWithLifecycle(
        initialValue = GrammaticaSyllabus.percorso(emptyMap())
    )

    val prossima = remember(percorso) {
        percorso.firstOrNull { it.stato == StatoUnita.DA_RIPASSARE }
            ?: percorso.firstOrNull { it.stato == StatoUnita.DISPONIBILE }
            ?: percorso.firstOrNull { it.stato == StatoUnita.COMPLETATA }
    }
    val completate = percorso.count { it.padronanza?.completata == true }
    val progresso = if (percorso.isEmpty()) 0f else completate.toFloat() / percorso.size
    val perLivello = percorso.groupBy { it.unita.livello }

    ScreenScaffold(title = "Percorso grammaticale", onBack = onIndietro) {
        // ---- Intro: obiettivo e avanzamento ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(Spaziature.md)) {
                Text(
                    "A0 -> B1",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "20 minuti al giorno. La grammatica comanda il percorso.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(Spaziature.sm))
                LinearProgressIndicator(
                    progress = { progresso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$completate di ${percorso.size} unità completate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        // ---- Prossimo passo ----
        if (prossima != null) {
            CardProssimaUnita(
                voce = prossima,
                onApri = { onApriUnita(prossima.unita.id) }
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(
                        "Percorso completato",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Hai completato tutte le unità disponibili. Continua con i ripassi.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        // ---- Unità per livello ----
        GrammaticaSyllabus.ordineLivelli.forEach { livello ->
            val unitaDelLivello = perLivello[livello] ?: return@forEach
            Text(
                livello,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Spaziature.sm, bottom = Spaziature.xs)
            )
            unitaDelLivello.forEach { voce ->
                CardUnita(
                    voce = voce,
                    onApri = { onApriUnita(voce.unita.id) }
                )
                Spacer(Modifier.height(Spaziature.sm))
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        // ---- Come funziona ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(Spaziature.md)) {
                Text(
                    "Come funziona",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(Spaziature.xs))
                PuntoAiuto("Studia la regola, guarda le tabelle e ascolta gli esempi.")
                PuntoAiuto("Fai gli esercizi: scelta multipla, completamento, trasformazione e produzione.")
                PuntoAiuto("Con almeno l'80% sblocchi la unità successiva.")
                PuntoAiuto("Il ripasso torna dopo 1, 3, 7 e 16 giorni.")
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        // ---- Roadmap: direzione, senza contenuti finti ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(Spaziature.md)) {
                Text(
                    "Prossime tappe",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Dopo le tre unità pilota, il percorso si estende a tutto l'A0-B1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spaziature.sm))
                GrammaticaSyllabus.tappeFuture.take(10).forEach { tappa ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                tappa.livello,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(Spaziature.sm))
                        Text(
                            tappa.titolo,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(Spaziature.xs))
                Text(
                    "In arrivo nelle prossime fasi.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Spazio per non finire sotto la bolla del tutor.
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun CardProssimaUnita(
    voce: UnitaConStato,
    onApri: () -> Unit
) {
    val unita = voce.unita
    val (etichetta, azione) = when (voce.stato) {
        StatoUnita.DA_RIPASSARE -> "Ripasso in scadenza" to "Ripassa"
        StatoUnita.DISPONIBILE -> "Prossima unità" to "Inizia"
        StatoUnita.COMPLETATA -> "Percorso completato" to "Ripassa"
        StatoUnita.BLOCCATA -> "Bloccata" to "Apri"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Text(
                etichetta.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(2.dp))
            Text(
                unita.titolo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                "${unita.livello} - ${unita.minuti} min - ${unita.sottotitolo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(Spaziature.sm))
            Button(
                onClick = onApri,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(azione)
                Spacer(Modifier.width(Spaziature.sm))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CardUnita(
    voce: UnitaConStato,
    onApri: () -> Unit
) {
    val unita = voce.unita
    val bloccata = voce.stato == StatoUnita.BLOCCATA
    val coloreSfondo = when (voce.stato) {
        StatoUnita.DA_RIPASSARE -> MaterialTheme.colorScheme.tertiaryContainer
        StatoUnita.COMPLETATA -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val (icona, descrizioneStato) = when (voce.stato) {
        StatoUnita.BLOCCATA -> Icons.Filled.Lock to "Bloccata"
        StatoUnita.DISPONIBILE -> Icons.Filled.PlayArrow to "Da iniziare"
        StatoUnita.COMPLETATA -> Icons.Filled.CheckCircle to "Completata"
        StatoUnita.DA_RIPASSARE -> Icons.Filled.Refresh to "Da ripassare"
    }
    val prerequisiti = remember(unita) {
        unita.prerequisiti.mapNotNull { id -> GrammaticaSyllabus.perId(id)?.titolo }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (bloccata) Modifier else Modifier.clickable { onApri() }),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = coloreSfondo),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spaziature.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (bloccata) {
                    MaterialTheme.colorScheme.surfaceContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icona,
                        contentDescription = descrizioneStato,
                        modifier = Modifier.size(20.dp),
                        tint = if (bloccata) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }
            Spacer(Modifier.width(Spaziature.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    unita.titolo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${unita.livello} - ${unita.minuti} min - $descrizioneStato",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (bloccata && prerequisiti.isNotEmpty()) {
                    Text(
                        "Sblocca completando: ${prerequisiti.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                voce.padronanza?.let { padronanza ->
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { padronanza.percentuale / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                    )
                    Text(
                        "Padronanza ${padronanza.percentuale}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!bloccata) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PuntoAiuto(testo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            "-",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            testo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
