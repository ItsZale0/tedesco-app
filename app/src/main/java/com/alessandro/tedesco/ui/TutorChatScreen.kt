package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.launch

data class MessaggioTutor(
    val testo: String,
    val daUtente: Boolean,
    val errore: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatScreen(vm: TedescoViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val apiKey by vm.tutorApiKey.collectAsStateWithLifecycle("")
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messaggi by remember {
        mutableStateOf(
            listOf(
                MessaggioTutor(
                    testo = "Ciao! Sono il tuo tutor di tedesco. Chiedimi quello che vuoi: grammatica, esempi, esercizi, dubbi. Rispondo in italiano.",
                    daUtente = false
                )
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var caricamento by remember { mutableStateOf(false) }

    fun invia(testo: String) {
        val t = testo.trim()
        if (t.isBlank() || caricamento) return
        messaggi = messaggi + MessaggioTutor(t, daUtente = true)
        input = ""
        caricamento = true

        scope.launch {
            val cronologia = messaggi
                .filterNot { it.errore }
                .map { if (it.daUtente) "utente" to it.testo else "tutor" to it.testo }

            val risultato = vm.chiediAlTutor(cronologia, livello, lezioneCorrente)
            messaggi = messaggi + risultato.fold(
                onSuccess = { MessaggioTutor(it, daUtente = false) },
                onFailure = { MessaggioTutor(it.message ?: "Errore sconosciuto", daUtente = false, errore = true) }
            )
            caricamento = false
        }
    }

    LaunchedEffect(messaggi.size) {
        if (messaggi.isNotEmpty()) listState.animateScrollToItem(messaggi.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Tutor di tedesco", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Livello $livello · lezione $lezioneCorrente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (apiKey.isBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spaziaturaSchermo(), vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Per usare il tutor serve una chiave API OpenRouter. " +
                            "Vai in Profilo → Tutor AI e inseriscila.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = spaziaturaSchermo()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messaggi) { msg -> MessaggioBubble(msg) }
                if (caricamento) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Sto scrivendo…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Chiedi qualcosa…") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        enabled = !caricamento
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { invia(input) },
                        enabled = input.isNotBlank() && !caricamento
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessaggioBubble(msg: MessaggioTutor) {
    val isUser = msg.daUtente
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> MaterialTheme.colorScheme.primary
                    msg.errore -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            val colore = when {
                isUser -> MaterialTheme.colorScheme.onPrimary
                msg.errore -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            if (isUser) {
                Text(
                    text = msg.testo,
                    modifier = Modifier.padding(12.dp),
                    color = colore,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(modifier = Modifier.padding(12.dp)) {
                    MarkdownText(markdown = msg.testo, color = colore)
                }
            }
        }
    }
}
