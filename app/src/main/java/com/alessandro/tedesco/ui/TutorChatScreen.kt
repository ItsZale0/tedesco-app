package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.GrammaticaB1
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.launch

data class MessaggioTutor(
    val testo: String,
    val daUtente: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatScreen(vm: TedescoViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label ?: "A0"
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messaggi by remember {
        mutableStateOf(
            listOf(
                MessaggioTutor(
                    testo = "Ciao! 👋 Sono il tuo tutor di tedesco. Posso aiutarti con:\n\n• Spiegazioni grammaticali\n• Esercizi di vocabolario\n• Traduzioni\n• Consigli di studio\n\nCosa vuoi imparare oggi?",
                    daUtente = false
                )
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var caricamento by remember { mutableStateOf(false) }

    fun inviaMessaggio(testo: String) {
        if (testo.isBlank()) return
        messaggi = messaggi + MessaggioTutor(testo, daUtente = true)
        input = ""
        caricamento = true

        scope.launch {
            val risposta = generaRisposta(testo, livello, parole)
            messaggi = messaggi + MessaggioTutor(risposta, daUtente = false)
            caricamento = false
        }
    }

    LaunchedEffect(messaggi.size) {
        if (messaggi.isNotEmpty()) {
            listState.animateScrollToItem(messaggi.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Tutor Tedesco", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Livello $livello",
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
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = spaziaturaSchermo()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messaggi) { msg ->
                    MessaggioBubble(msg)
                }
                if (caricamento) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            // Input
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Scrivi un messaggio...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { inviaMessaggio(input) },
                        enabled = input.isNotBlank() && !caricamento
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Invia",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
        if (!isUser) {
            Icon(
                Icons.Filled.School,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Card(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.testo,
                modifier = Modifier.padding(12.dp),
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (isUser) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun generaRisposta(input: String, livello: String, parole: List<com.alessandro.tedesco.data.local.WordEntity>): String {
    val inputLower = input.lowercase()

    // Saluti
    if (inputLower.contains("ciao") || inputLower.contains("hallo") || inputLower.contains("buongiorno")) {
        return "Ciao! 😊 Come posso aiutarti oggi? Posso spiegarti la grammatica, fare esercizi con te, o aiutarti con il vocabolario."
    }

    // Grammatica
    if (inputLower.contains("grammatica") || inputLower.contains("verbo") || inputLower.contains("verbi")) {
        return when {
            inputLower.contains("sein") || inputLower.contains("haben") -> {
                "**sein** vs **haben**:\n\n• **sein** (essere) → identità, professione, nazionalità, età\n  - Ich bin Student. (Sono studente.)\n  - Ich bin 20 Jahre alt. (Ho 20 anni.)\n\n• **haben** (avere) → possesso, relazioni\n  - Ich habe ein Buch. (Ho un libro.)\n  - Ich habe eine Schwester. (Ho una sorella.)"
            }
            inputLower.contains("articolo") || inputLower.contains("articoli") -> {
                "**Articoli**:\n\n• **der** → maschile (der Mann, il padre)\n• **die** → femminile (die Frau, la donna)\n• **das** → neutro (das Kind, il bambino)\n\nRicorda: in tedesco TUTTI i sostantivi hanno la maiuscola!"
            }
            inputLower.contains("perfekt") || inputLower.contains("passato") -> {
                "**Perfekt** (passato):\n\n• **haben** + Partizip II → la maggior parte dei verbi\n  - Ich habe geschlafen. (Ho dormito.)\n\n• **sein** + Partizip II → verbi di movimento e cambiamento\n  - Ich bin gegangen. (Sono andato.)\n  - Ich bin aufgewacht. (Mi sono svegliato.)"
            }
            else -> {
                "Posso spiegarti:\n\n• **sein** vs **haben**\n• Articoli (der/die/das)\n• Perfekt (passato)\n• Verbi modali (können, müssen, wollen)\n• Preposizioni\n\nCosa vuoi sapere?"
            }
        }
    }

    // Esercizi
    if (inputLower.contains("esercizio") || inputLower.contains("esercizi") || inputLower.contains("test")) {
        val esercizio = GrammaticaB1.eserciziPerLivello(livello, 1).firstOrNull()
        return if (esercizio != null) {
            "Ecco un esercizio per te:\n\n**${esercizio.domanda}**\n\nOpzioni:\n${esercizio.opzioni.mapIndexed { i, o -> "${i + 1}. $o" }.joinToString("\n")}\n\nRispondi con il numero dell'opzione corretta!"
        } else {
            "Non ho esercizi disponibili per il tuo livello. Prova a cambiare livello in Profilo!"
        }
    }

    // Traduzione
    if (inputLower.contains("traduci") || inputLower.contains("traduzione") || inputLower.contains("come si dice")) {
        return "Per traduzioni usa la sezione **Traduttore** nel menu Altro. Lì puoi tradurre qualsiasi parola o frase!"
    }

    // Vocabolario
    if (inputLower.contains("parola") || inputLower.contains("parole") || inputLower.contains("vocabolario")) {
        val paroleLivello = parole.filter { it.level == livello }
        return if (paroleLivello.isNotEmpty()) {
            "Ecco alcune parole di livello $livello:\n\n${paroleLivello.take(5).joinToString("\n") { "• **${it.german}** = ${it.italian}" }}\n\nVuoi che ti faccia un esercizio con queste parole?"
        } else {
            "Non ho parole di livello $livello per ora. Prova a sincronizzare il vocabolario!"
        }
    }

    // Consigli
    if (inputLower.contains("consiglio") || inputLower.contains("come studiare") || inputLower.contains("metodo")) {
        return "Ecco i miei consigli per studiare tedesco:\n\n📚 **Studio quotidiano**: 20-30 minuti al giorno\n🔄 **Ripasso**: usa la ripetizione spaziata\n🗣️ **Parla**: anche da solo, ad alta voce\n📱 **App**: usa Busuu + questa app\n🎵 **Ascolta**: musica e podcast in tedesco\n\nLa costanza è più importante dell'intensità!"
    }

    // Default
    return "Non ho capito bene. Posso aiutarti con:\n\n• **Grammatica** (sein/haben, articoli, perfekt)\n• **Esercizi** (facciamo pratica)\n• **Vocabolario** (parole del tuo livello)\n• **Consigli** (come studiare)\n\nCosa preferisci?"
}
