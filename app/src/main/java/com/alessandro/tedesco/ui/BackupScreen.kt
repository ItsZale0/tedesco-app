package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.BackupCloud
import com.alessandro.tedesco.data.remote.BackupStato
import com.alessandro.tedesco.data.remote.BackupViewModel
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup cloud su GitHub. Carica tutti i profili (parole, ripassi, test,
 * progressi) su un repo privato e li ripristina.
 */
@Composable
fun BackupScreen(vm: BackupViewModel, onIndietro: () -> Unit) {
    val token by vm.token.collectAsStateWithLifecycle("")
    val repo by vm.repo.collectAsStateWithLifecycle("")
    val ultimo by vm.ultimoBackup.collectAsStateWithLifecycle(0L)
    val stato by vm.stato.collectAsStateWithLifecycle()
    val daRipristinare by vm.daRipristinare.collectAsStateWithLifecycle(null)

    var tokenInput by remember(token) { mutableStateOf(token) }
    var repoInput by remember(repo) { mutableStateOf(repo) }
    var confermaRipristino by remember { mutableStateOf(false) }

    ScreenScaffold("Backup cloud", onBack = onIndietro) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = dimensioneContenuto()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // --- Cosa viene salvato ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Raggi.card),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Cosa viene salvato", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "TUTTI i profili con parole, ripassi, log, test, progressi, " +
                            "conversazioni e correzioni. Il backup va su un repo GitHub privato.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (ultimo > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ultimo backup: " + SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ITALIAN)
                                .format(Date(ultimo)),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- Configurazione ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Raggi.card),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Configurazione", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        label = { Text("Repo di backup (utente/nome)") },
                        placeholder = { Text("ItsZale0/tedesco-backup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Token GitHub") },
                        placeholder = { Text("github_pat_… o ghp_…") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    TutorialGrigioBackup(
                        "Crea un token su github.com/settings/tokens con permesso " +
                            "\"Contents: Read and write\" sul repo di backup. Il token resta solo sul telefono."
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            vm.salvaRepo(repoInput)
                            vm.salvaToken(tokenInput)
                            vm.provaConnessione()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salva e prova connessione") }
                }
            }

            // --- Stato ---
            when (val s = stato) {
                is BackupStato.InCorso -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Raggi.card),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Operazione in corso…")
                    }
                }
                is BackupStato.Ok -> MessaggioStato(s.messaggio, errore = false)
                is BackupStato.Errore -> MessaggioStato(s.messaggio, errore = true)
                BackupStato.Idle -> {}
            }

            // --- Azioni ---
            Button(
                onClick = { vm.eseguiBackup(com.alessandro.tedesco.BuildConfig.VERSION_NAME) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = token.isNotBlank() && stato !is BackupStato.InCorso
            ) { Text("Carica backup ora") }

            OutlinedButton(
                onClick = { vm.preparaRipristino() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = token.isNotBlank() && stato !is BackupStato.InCorso
            ) { Text("Ripristina dal backup") }

            Spacer(Modifier.height(24.dp))
        }
    }

    // --- Conferma ripristino (distruttivo) ---
    val riep = daRipristinare
    if (riep != null && confermaRipristino) {
        AlertDialog(
            onDismissRequest = { confermaRipristino = false; vm.annullaRipristino() },
            title = { Text("Sovrascrivere i dati locali?") },
            text = {
                Column {
                    Text("Il backup contiene:")
                    Spacer(Modifier.height(8.dp))
                    Text("• ${riep.numProfili} profili")
                    Text("• ${riep.numParole} parole")
                    Text("• ${riep.numRipassi} ripassi registrati")
                    Text("• ${riep.numRisposte} correzioni")
                    Spacer(Modifier.height(8.dp))
                    Text("Data: " + SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ITALIAN)
                        .format(Date(riep.creatoIl)))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "I dati attuali sul telefono verranno SOSTITUITI. " +
                            "Se hai fatto modifiche dopo questo backup, andranno perse.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    confermaRipristino = false
                    vm.confermaRipristino()
                }) { Text("Sì, ripristina") }
            },
            dismissButton = {
                TextButton(onClick = { confermaRipristino = false; vm.annullaRipristino() }) {
                    Text("Annulla")
                }
            }
        )
    } else if (riep != null) {
        AlertDialog(
            onDismissRequest = { vm.annullaRipristino() },
            title = { Text("Backup trovato") },
            text = {
                Column {
                    Text("• ${riep.numProfili} profili")
                    Text("• ${riep.numParole} parole")
                    Text("• ${riep.numRipassi} ripassi")
                    Spacer(Modifier.height(8.dp))
                    Text("Vuoi ripristinarlo sul telefono?")
                }
            },
            confirmButton = {
                TextButton(onClick = { confermaRipristino = true }) { Text("Continua") }
            },
            dismissButton = {
                TextButton(onClick = { vm.annullaRipristino() }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun MessaggioStato(messaggio: String, errore: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Raggi.card),
        colors = CardDefaults.cardColors(
            containerColor = if (errore) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            messaggio,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (errore) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TutorialGrigioBackup(testo: String) {
    var aperto by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        TextButton(onClick = { aperto = !aperto }) {
            Text(if (aperto) "Come creo il token? ▲" else "Come creo il token? ▼",
                style = MaterialTheme.typography.bodySmall)
        }
        if (aperto) {
            Text(
                testo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
