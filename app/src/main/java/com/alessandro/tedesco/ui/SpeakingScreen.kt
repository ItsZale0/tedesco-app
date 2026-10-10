package com.alessandro.tedesco.ui

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.ConversazioneEntry
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.launch

data class MessaggioSpeaking(
    val text: String,
    val daUtente: Boolean,
    val correzione: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit,
    profilo: ProfiloUtente? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val apiKey by vm.tutorApiKey.collectAsStateWithLifecycle("")
    val modelloTutor by vm.modelloTutor.collectAsStateWithLifecycle(null)
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"

    // Carica conversazione persistente dal profilo
    var messaggi by remember {
        val p = profilo
        mutableStateOf(
            p?.stato?.conversazioni
                ?.filter { it.tipo == "speaking" }
                ?.map { MessaggioSpeaking(it.testo, it.ruolo == "utente", it.correzione) }
                ?: listOf(
                    MessaggioSpeaking(
                        text = "Ciao! Sono il tuo tutor di tedesco. Parla con me in tedesco: ti ascolto, trascrivo e correggio. Prova a presentarti o a dire cosa fai oggi.",
                        daUtente = false
                    )
                )
        )
    }
    var inputTesto by remember { mutableStateOf("") }
    var ascoltoAttivo by remember { mutableStateOf(false) }
    var caricamento by remember { mutableStateOf(false) }
    var errorePermesso by remember { mutableStateOf(false) }

    // SpeechRecognizer
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    // Salva conversazione nel profilo quando cambiano i messaggi
    LaunchedEffect(messaggi, profilo) {
        val p = profilo
        if (p != null) {
            val conversazioni = messaggi.mapIndexed { index, msg ->
                ConversazioneEntry(
                    id = "speaking_${System.currentTimeMillis()}_$index",
                    tipo = "speaking",
                    ruolo = if (msg.daUtente) "utente" else "tutor",
                    testo = msg.text,
                    timestamp = System.currentTimeMillis() - (messaggi.size - index) * 1000,
                    correzione = msg.correzione
                )
            }
            vm.saveConversazioni(p.id, "speaking", conversazioni)
        }
    }

    fun avviaAscolto() {
        errorePermesso = false
        ascoltoAttivo = true
        inputTesto = ""

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                ascoltoAttivo = false
            }
            override fun onError(error: Int) {
                ascoltoAttivo = false
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    inputTesto = "Non ho capito. Riprova."
                } else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    errorePermesso = true
                }
            }
            override fun onResults(results: Bundle?) {
                ascoltoAttivo = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputTesto = matches[0]
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputTesto = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    fun fermaAscolto() {
        ascoltoAttivo = false
        speechRecognizer.stopListening()
    }

    fun invia(testo: String) {
        val t = testo.trim()
        if (t.isBlank() || caricamento) return
        messaggi = messaggi + MessaggioSpeaking(t, daUtente = true)
        inputTesto = ""
        caricamento = true

        scope.launch {
            val cronologia = messaggi
                .filter { it.correzione == null }
                .map { if (it.daUtente) "utente" to it.text else "tutor" to it.text }

            val risultato = vm.chiediAlTutor(cronologia, livello, lezioneCorrente)
            risultato.fold(
                onSuccess = { risposta ->
                    messaggi = messaggi + MessaggioSpeaking(risposta, daUtente = false)
                },
                onFailure = { e ->
                    messaggi = messaggi + MessaggioSpeaking(
                        "Errore: ${e.message}",
                        daUtente = false
                    )
                }
            )
            caricamento = false
        }
    }

    ScreenScaffold("Speaking con AI", onBack = onIndietro) {

            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spaziaturaSchermo(), vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Parla in tedesco con il microfono. L'AI ti ascolta, trascrive e corregge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (apiKey.isBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Inserisci una chiave API Groq gratuita in Profilo → Tutor AI per usare il tutor.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (errorePermesso) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spaziaturaSchermo(), vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Permesso microfono negato. Vai in Impostazioni → App → Permessi e attiva il microfono.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Chat
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spaziaturaSchermo()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messaggi) { messaggio ->
                    SpeakingCard(messaggio = messaggio)
                }
            }

            // Input area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spaziaturaSchermo()),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Pulsante microfono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (ascoltoAttivo) {
                            FilledTonalButton(
                                onClick = { fermaAscolto() },
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    Icons.Filled.MicOff,
                                    contentDescription = "Ferma ascolto",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { avviaAscolto() },
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Mic,
                                    contentDescription = "Parla",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    if (ascoltoAttivo) {
                        Text(
                            "Ascolto in corso... parla in tedesco",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Testo riconosciuto
                    if (inputTesto.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputTesto,
                            onValueChange = { inputTesto = it },
                            label = { Text("Testo riconosciuto") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Pulsante invia
                    Button(
                        onClick = { invia(inputTesto) },
                        enabled = inputTesto.isNotBlank() && !caricamento && !ascoltoAttivo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (caricamento) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Correzione in corso...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Invia e correggi")
                        }
                    }
                }
            }

    }
}

@Composable
private fun SpeakingCard(messaggio: MessaggioSpeaking) {
    val isUser = messaggio.daUtente
    val containerColor = if (isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isUser)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (isUser) "Tu" else "Tutor",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = messaggio.text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            if (messaggio.correzione != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = messaggio.correzione,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
