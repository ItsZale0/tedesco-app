package com.alessandro.tedesco.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.PlacementTest
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Test di ingresso breve (12 domande) mostrato nella home finché non viene
 * completato o saltato. Evita che un falso principiante passi settimane su
 * "Saluti e presentazioni".
 */
@Composable
fun PlacementTestCard(
    onCompletato: (LivelloCEFR) -> Unit,
    onSalta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val domande = PlacementTest.domande
    var indice by remember { mutableIntStateOf(0) }
    var corrette by remember { mutableIntStateOf(0) }
    var scelta by remember { mutableStateOf<Int?>(null) }
    var verificato by remember { mutableStateOf(false) }
    var finito by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Test di ingresso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "12 domande, 2 minuti: capiamo da dove partire.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(Spaziature.sm))

            if (!finito) {
                val domanda = domande[indice]
                LinearProgressIndicator(
                    progress = { (indice + 1f) / domande.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spaziature.sm))
                Text(
                    "Domanda ${indice + 1} di ${domande.size} - ${domanda.livello}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    domanda.domanda,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = Spaziature.sm)
                )
                domanda.opzioni.forEachIndexed { i, opzione ->
                    val selezionata = scelta == i
                    val giusta = i == domanda.corretta
                    val sfondo = when {
                        !verificato -> MaterialTheme.colorScheme.surface
                        giusta -> MaterialTheme.colorScheme.primaryContainer
                        selezionata -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { if (!verificato) scelta = i },
                        colors = CardDefaults.cardColors(containerColor = sfondo)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opzione, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            if (verificato && giusta) {
                                Icon(Icons.Filled.Check, contentDescription = "Corretta", modifier = Modifier.size(18.dp))
                            } else if (verificato && selezionata) {
                                Icon(Icons.Filled.Close, contentDescription = "Sbagliata", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Spaziature.sm))
                if (!verificato) {
                    Button(
                        onClick = {
                            verificato = true
                            if (scelta == domanda.corretta) corrette++
                        },
                        enabled = scelta != null,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Verifica") }
                } else {
                    Button(
                        onClick = {
                            if (indice < domande.size - 1) {
                                indice++
                                scelta = null
                                verificato = false
                            } else {
                                finito = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (indice < domande.size - 1) "Prossima" else "Vedi risultato") }
                }
            } else {
                val esito = PlacementTest.valuta(corrette)
                Text(
                    "Risultato: $corrette / ${esito.totale}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    esito.livello.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(esito.messaggio, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(Spaziature.md))
                Button(
                    onClick = { onCompletato(esito.livello) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Parti da ${esito.livello.codiceBreve()}") }
                TextButton(onClick = onSalta, modifier = Modifier.fillMaxWidth()) {
                    Text("Resta sul livello attuale")
                }
            }

            if (!finito) {
                TextButton(onClick = onSalta, modifier = Modifier.fillMaxWidth()) {
                    Text("Salta per ora")
                }
            }
        }
    }
}
