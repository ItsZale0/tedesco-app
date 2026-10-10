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
import com.alessandro.tedesco.data.CalcoloTappe
import com.alessandro.tedesco.data.local.PianoEntity
import com.alessandro.tedesco.data.local.ProgressoFeedEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

/**
 * Piano di studio verso il B2: tappe, certificazioni, risorse, progresso.
 * Tutto quello che prima arrivava su WhatsApp, ora dentro l'app.
 *
 * Le tappe mostrano uno stato dinamico calcolato in base ai progressi reali
 * dell'utente (lezione corrente, parole apprese, livello).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PianoScreen(
    onIndietro: () -> Unit,
    piano: PianoEntity?,
    progresso: ProgressoFeedEntity?,
    lezioneCorrente: Int = 1,
    paroleApprese: Int = 0,
    livelloCorrente: String = "A0"
) {
    // Calcola gli stati dinamici delle tappe
    val tappeConStato = if (piano != null) {
        val livello = parseLivello(livelloCorrente)
        CalcoloTappe.calcola(
            tappe = piano.tappe,
            progresso = CalcoloTappe.ProgressoTappe(
                lezioneCorrente = lezioneCorrente,
                paroleApprese = paroleApprese,
                livelloCorrente = livello
            )
        )
    } else emptyList()

    ScreenScaffold("Piano di studio", onBack = onIndietro) {

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

                // Tappe con stato dinamico
                if (piano != null && tappeConStato.isNotEmpty()) {
                    Text(
                        "Tappe del percorso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    tappeConStato.forEach { tappa ->
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
        "completata" -> MaterialTheme.colorScheme.secondaryContainer
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

/**
 * Converte una stringa di livello (es. "A0", "A1", "B1") in LivelloCEFR.
 * Se non riconosciuto, restituisce A0.
 */
private fun parseLivello(livello: String): com.alessandro.tedesco.data.local.LivelloCEFR {
    return when (livello.uppercase().trim()) {
        "A0" -> com.alessandro.tedesco.data.local.LivelloCEFR.A0
        "A1" -> com.alessandro.tedesco.data.local.LivelloCEFR.A1
        "A2" -> com.alessandro.tedesco.data.local.LivelloCEFR.A2
        "B1" -> com.alessandro.tedesco.data.local.LivelloCEFR.B1
        "B2" -> com.alessandro.tedesco.data.local.LivelloCEFR.B2
        "C1" -> com.alessandro.tedesco.data.local.LivelloCEFR.C1
        "C2" -> com.alessandro.tedesco.data.local.LivelloCEFR.C2
        else -> com.alessandro.tedesco.data.local.LivelloCEFR.A0
    }
}
