package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TutorService
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.launch

data class MessaggioVoiceChat(
    val testo: String,
    val daUtente: Boolean,
    val corretta: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChatScreen(vm: TedescoViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val apiKey by vm.tutorApiKey.collectAsStateWithLifecycle("")
    val modelloTutor by vm.modelloTutor.collectAsStateWithLifecycle(null)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceHelper = remember { VoiceHelper(context) }
    val ttsHelper = rememberTtsHelper(context)

    var messaggi by remember {
        mutableStateOf(
            listOf(
                MessaggioVoiceChat(
                    testo = "Ciao! Sono il tuo tutor di tedesco. Parla con me in tedesco, ti risponderò e ti correggerò alla fine. Tieni premuto il microfono per parlare.",
                    daUtente = false
                )
            )
        )
    }
    var elaborazione by remember { mutableStateOf(false) }
    var correzione by remember { mutableStateOf<String?>(null) }
    var mostrandoCorrezione by remember { mutableStateOf(false) }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
            ttsHelper.shutdown()
        }
    }

    // Auto-scroll
    LaunchedEffect(messaggi.size) {
        if (messaggi.isNotEmpty()) {
            listState.animateScrollToItem(messaggi.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversazione vocale", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { /* nav.popBackStack() */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    // Stop TTS button
                    if (ttsHelper.isSpeaking) {
                        IconButton(onClick = { ttsHelper.stop() }) {
                            Icon(Icons.Filled.Stop, contentDescription = "Ferma audio")
                        }
                    }
                    // Fine conversazione
                    if (messaggi.size > 1) {
                        TextButton(onClick = {
                            scope.launch {
                                elaborazione = true
                                correzione = null
                                mostrandoCorrezione = false
                                val conversazione = messaggi.joinToString("\n") { m ->
                                    if (m.daUtente) "Studente: ${m.testo}" else "Tutor: ${m.testo}"
                                }
                                val prompt = """Analizza questa conversazione in tedesco tra un studente di livello $livello e un tutor.
                                    |Identifica gli errori grammaticali, di vocabolario e di pronuncia.
                                    |Per ogni errore: indica l'errore, la correzione e una spiegazione breve in italiano.
                                    |Alla fine, dai un punteggio da 1 a 10 e un consiglio per migliorare.
                                    |Rispondi in italiano.
                                    |
                                    |Conversazione:
                                    |$conversazione""".trimMargin()
                                try {
                                    val tutorService = TutorService()
                                    val risposta = tutorService.rispondi(
                                        apiKey = apiKey,
                                        cronologia = listOf("system" to prompt),
                                        livello = livello,
                                        lezione = lezioneCorrente,
                                        modello = modelloTutor
                                    )
                                    correzione = risposta
                                    mostrandoCorrezione = true
                                } catch (e: Exception) {
                                    correzione = "Errore durante la correzione: ${e.message}"
                                    mostrandoCorrezione = true
                                } finally {
                                    elaborazione = false
                                }
                            }
                        }) {
                            Text("Fine")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(spaziaturaSchermo()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Livello
            AssistChip(
                onClick = {},
                label = { Text("Livello: $livello") }
            )

            Spacer(Modifier.height(8.dp))

            // Messaggi
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messaggi) { msg ->
                    MessaggioBubble(msg, ttsHelper)
                }

                if (elaborazione) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sto analizzando la conversazione...")
                        }
                    }
                }
            }

            // Correzione
            if (mostrandoCorrezione && correzione != null) {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Correzione",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            correzione!!,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Push-to-talk
            VoiceInputBar(
                voiceHelper = voiceHelper,
                onResult = { testo ->
                    scope.launch {
                        messaggi = messaggi + MessaggioVoiceChat(testo, daUtente = true)
                        elaborazione = true
                        try {
                            val tutorService = TutorService()
                            val risposta = tutorService.rispondi(
                                apiKey = apiKey,
                                cronologia = messaggi.map { m ->
                                    if (m.daUtente) "user" to m.testo else "assistant" to m.testo
                                },
                                livello = livello,
                                lezione = lezioneCorrente,
                                modello = modelloTutor
                            )
                            messaggi = messaggi + MessaggioVoiceChat(risposta, daUtente = false)
                            ttsHelper.speak(risposta)
                        } catch (e: Exception) {
                            messaggi = messaggi + MessaggioVoiceChat(
                                "Errore: ${e.message}",
                                daUtente = false
                            )
                        } finally {
                            elaborazione = false
                        }
                    }
                },
                enabled = !elaborazione
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun MessaggioBubble(msg: MessaggioVoiceChat, ttsHelper: TtsHelper) {
    val alignment = if (msg.daUtente) Arrangement.End else Arrangement.Start
    val containerColor = if (msg.daUtente) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = msg.testo,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!msg.daUtente) {
                    Spacer(Modifier.height(4.dp))
                    IconButton(
                        onClick = { ttsHelper.speak(msg.testo) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "Ascolta",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}