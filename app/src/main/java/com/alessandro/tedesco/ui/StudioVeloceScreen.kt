package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.data.remote.TutorService
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Raggi
import com.alessandro.tedesco.ui.theme.Spaziature
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Modalità Studio Veloce — una schermata unica con lezione + esercizi integrati.
 * Flusso lineare: Lezione → Parole → Esercizi → Fine.
 * Niente menu, niente navigazione complessa, niente debug continui.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioVeloceScreen(
    vm: TedescoViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Stato
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val contenuto by vm.lezioneContenuto.collectAsStateWithLifecycle("")
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioneStato by vm.lezioneStato.collectAsStateWithLifecycle<StatoLezione>(StatoLezione.Idle)
    val audioUrl by vm.lezioneAudioUrl.collectAsStateWithLifecycle("")
    val ttsHelper = rememberTtsHelper(context)
    
    // Parole della lezione corrente
    val paroleLezione = parole.filter { it.lesson == lezioneCorrente }
    
    // Esercizi generati per questa lezione
    var esercizi by remember { mutableStateOf<List<EsercizioStudio>>(emptyList()) }
    var indiceEsercizio by remember { mutableStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var eserciziCompletati by remember { mutableStateOf(0) }
    var mostraSoluzioni by remember { mutableStateOf(false) }
    
    // Genera esercizi quando entra nella schermata
    LaunchedEffect(lezioneCorrente) {
        esercizi = emptyList()
        indiceEsercizio = 0
        rispostaSelezionata = null
        eserciziCompletati = 0
        mostraSoluzioni = false
        
        if (paroleLezione.isNotEmpty()) {
            try {
                esercizi = generaEserciziDaParole(paroleLezione, lezioneCorrente)
            } catch (e: Exception) {
                esercizi = emptyList()
            }
        }
    }
    
    // Sync automatico se non ci sono parole
    LaunchedEffect(parole.isEmpty()) {
        if (parole.isEmpty()) {
            vm.sincronizza()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Lezione $lezioneCorrente",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (paroleLezione.isNotEmpty()) {
                            Text(
                                "${paroleLezione.size} parole",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spaziature.md),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Sezione 1: Contenuto lezione
            if (contenuto.isNotBlank()) {
                item {
                    Spacer(Modifier.height(Spaziature.md))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Spaziature.md)
                        ) {
                            Text(
                                "📖 Teoria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                contenuto,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Sezione 2: Parole della lezione
            if (paroleLezione.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(Spaziature.md))
                    Text(
                        "📝 Parole chiave",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spaziature.sm))
                }
                
                items(paroleLezione.chunked(2)) { riga ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                    ) {
                        riga.forEach { parola ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(Raggi.card),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spaziature.sm),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        ParolaTedesca.conArticolo(parola.article, parola.german),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        parola.italian,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        // Se la riga ha 1 elemento, aggiungi uno spaziatore
                        if (riga.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(Spaziature.sm))
                }
            }
            
            // Sezione 3: Esercizi
            if (esercizi.isNotEmpty() && indiceEsercizio < esercizi.size) {
                item {
                    Spacer(Modifier.height(Spaziature.md))
                    Text(
                        "✏️ Esercizi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spaziature.sm))
                    
                    val esercizio = esercizi[indiceEsercizio]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Spaziature.md)
                        ) {
                            Text(
                                "Domanda ${indiceEsercizio + 1}/${esercizi.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                esercizio.domanda,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            
                            val corretto = rispostaSelezionata == esercizio.rispostaCorretta
                            
                            esercizio.opzioni.forEachIndexed { index, opzione ->
                                val selezionato = rispostaSelezionata == index
                                val mostraRisposta = mostraSoluzioni || selezionato
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            mostraRisposta && corretto -> MaterialTheme.colorScheme.primaryContainer
                                            mostraRisposta && selezionato && !corretto -> MaterialTheme.colorScheme.errorContainer
                                            selezionato -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = if (selezionato) BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    ) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spaziature.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selezionato,
                                            onClick = {
                                                if (!mostraSoluzioni) {
                                                    rispostaSelezionata = index
                                                }
                                            }
                                        )
                                        Text(
                                            opzione,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = Spaziature.sm)
                                        )
                                    }
                                }
                            }
                            
                            if (mostraSoluzioni || rispostaSelezionata != null) {
                                Spacer(Modifier.height(Spaziature.sm))
                                val corretto = rispostaSelezionata == esercizio.rispostaCorretta
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (corretto)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Text(
                                        if (corretto) "✓ Corretto!" else "✗ Sbagliato. La risposta giusta è: ${esercizio.opzioni[esercizio.rispostaCorretta]}",
                                        modifier = Modifier.padding(Spaziature.sm),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(Spaziature.md))
                            
                            if (rispostaSelezionata != null && !mostraSoluzioni) {
                                Button(
                                    onClick = { mostraSoluzioni = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(Raggi.bottone)
                                ) {
                                    Text("Verifica risposta")
                                }
                            } else if (mostraSoluzioni) {
                                Button(
                                    onClick = {
                                        if (rispostaSelezionata == esercizio.rispostaCorretta) {
                                            eserciziCompletati++
                                        }
                                        indiceEsercizio++
                                        rispostaSelezionata = null
                                        mostraSoluzioni = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(AltezzaBottonePrincipale()),
                                    shape = RoundedCornerShape(Raggi.bottone)
                                ) {
                                    Text(
                                        if (indiceEsercizio < esercizi.size - 1) "Prossimo"
                                        else "Fine"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Sezione 4: Fine esercizi
            if (esercizi.isNotEmpty() && indiceEsercizio >= esercizi.size) {
                item {
                    Spacer(Modifier.height(Spaziature.md))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🎉 Sessione completata!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                "Hai completato $eserciziCompletati esercizi su ${esercizi.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            Button(
                                onClick = {
                                    esercizi = emptyList()
                                    indiceEsercizio = 0
                                    rispostaSelezionata = null
                                    eserciziCompletati = 0
                                    mostraSoluzioni = false
                                    vm.avanzaLezione()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = RoundedCornerShape(Raggi.bottone)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(Spaziature.sm))
                                Text("Lezione successiva")
                            }
                        }
                    }
                }
            }
            
            // Sezione 5: Nessun contenuto
            if (contenuto.isBlank() && paroleLezione.isEmpty()) {
                item {
                    Spacer(Modifier.height(Spaziature.md))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Raggi.card),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️ Nessun contenuto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                "La lezione $lezioneCorrente non ha contenuto o parole. Prova a sincronizzare.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(Spaziature.md))
                            Button(
                                onClick = { vm.sincronizza() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale()),
                                shape = RoundedCornerShape(Raggi.bottone)
                            ) {
                                Text("Sincronizza")
                            }
                        }
                    }
                }
            }
        }
    }

    // Menu nascosto per impostazioni
    var mostraMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (mostraMenu) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 200.dp),
                shape = RoundedCornerShape(Raggi.card),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TextButton(
                        onClick = { /* nav a impostazioni */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Impostazioni")
                    }
                    TextButton(
                        onClick = { /* nav a altro */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Altro")
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { mostraMenu = !mostraMenu },
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        }
    }
}


/**
 * Genera esercizi da parole della lezione.
 */
private fun generaEserciziDaParole(
    parole: List<com.alessandro.tedesco.data.local.WordEntity>,
    lezione: Int
): List<EsercizioStudio> {
    val esercizi = mutableListOf<EsercizioStudio>()
    val paroleMescolate = parole.shuffled()
    
    paroleMescolate.take(5).forEach { parola ->
        val distratti = paroleMescolate
            .filter { it.id != parola.id }
            .shuffled()
            .take(3)
            .map { it.italian }
        
        val opzioni = (distratti + parola.italian).shuffled()
        
        esercizi.add(
            EsercizioStudio(
                domanda = "Cosa significa \"${parola.german}\"?",
                opzioni = opzioni,
                rispostaCorretta = opzioni.indexOf(parola.italian)
            )
        )
    }
    
    return esercizi
}


data class EsercizioStudio(
    val domanda: String,
    val opzioni: List<String>,
    val rispostaCorretta: Int
)
