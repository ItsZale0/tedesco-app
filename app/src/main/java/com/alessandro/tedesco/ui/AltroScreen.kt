package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.BarChart

/**
 * Rappresenta una voce nella schermata Altro.
 */
private data class ElementoAltro(
    val titolo: String,
    val descrizione: String,
    val icona: ImageVector,
    val onClick: () -> Unit,
    val usaTertiary: Boolean = false
)

/**
 * Raggruppa di categorie per la schermata Altro.
 */
private data class CategoriaAltro(
    val nome: String,
    val elementi: List<ElementoAltro>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltroScreen(
    onVaiAGrammatica: () -> Unit,
    onVaiATest: () -> Unit,
    onVaiATestB1: () -> Unit = {},
    onVaiAStats: () -> Unit,
    onVaiATraduttore: () -> Unit,
    onVaiATutor: () -> Unit,
    onVaiAFeedback: () -> Unit = {},
    onVaiACompetenze: () -> Unit = {},
    onVaiAQuizComprensione: () -> Unit = {},
    onVaiAQuizProduzione: () -> Unit = {},
    onVaiAPiano: () -> Unit = {},
    onVaiASessioni: () -> Unit = {},
    onVaiARoleplay: () -> Unit = {},
    onVaiAVoiceChat: () -> Unit = {},
    onVaiAContestoMedico: () -> Unit = {},
    onVaiAProgressione: () -> Unit = {},
    onVaiAProduzioneScritta: () -> Unit = {},
    onVaiAAscolto: () -> Unit = {}
) {
    val categorie = listOf(
        CategoriaAltro(
            nome = "Apprendimento",
            elementi = listOf(
                ElementoAltro(
                    titolo = "Grammatica",
                    descrizione = "Esercizi di grammatica per il tuo livello",
                    icona = Icons.Filled.School,
                    onClick = onVaiAGrammatica
                ),
                ElementoAltro(
                    titolo = "Test",
                    descrizione = "Verifica delle tue competenze",
                    icona = Icons.Filled.Quiz,
                    onClick = onVaiATestB1
                ),
                ElementoAltro(
                    titolo = "Test adattivi",
                    descrizione = "Domande generate in base al tuo livello e ai tuoi errori",
                    icona = Icons.Filled.Quiz,
                    onClick = onVaiATest
                ),
                ElementoAltro(
                    titolo = "Quiz Comprensione",
                    descrizione = "Verifica la comprensione del testo",
                    icona = Icons.Filled.Quiz,
                    onClick = onVaiAQuizComprensione
                ),
                ElementoAltro(
                    titolo = "Quiz Produzione",
                    descrizione = "Verifica la produzione scritta",
                    icona = Icons.Filled.Quiz,
                    onClick = onVaiAQuizProduzione
                ),
                ElementoAltro(
                    titolo = "Ascolto",
                    descrizione = "Esercizi di ascolto (Hörverstehen) con TTS",
                    icona = Icons.Filled.Hearing,
                    onClick = onVaiAAscolto
                )
            )
        ),
        CategoriaAltro(
            nome = "Pratica",
            elementi = listOf(
                ElementoAltro(
                    titolo = "Tutor Tedesco",
                    descrizione = "Chatbot che ti aiuta con grammatica ed esercizi",
                    icona = Icons.Filled.School,
                    onClick = onVaiATutor,
                    usaTertiary = true
                ),
                ElementoAltro(
                    titolo = "Correzione risposte",
                    descrizione = "Invia le tue risposte in tedesco e ricevi la correzione",
                    icona = Icons.AutoMirrored.Filled.Chat,
                    onClick = onVaiAFeedback
                ),
                ElementoAltro(
                    titolo = "Gioco di ruolo",
                    descrizione = "Conversazioni simulate in tedesco",
                    icona = Icons.AutoMirrored.Filled.Chat,
                    onClick = onVaiARoleplay,
                    usaTertiary = true
                ),
                ElementoAltro(
                    titolo = "Conversazione vocale",
                    descrizione = "Parla con l'AI e ricevi correzione alla finale",
                    icona = Icons.Filled.Mic,
                    onClick = onVaiAVoiceChat
                ),
                ElementoAltro(
                    titolo = "Traduttore",
                    descrizione = "Traduci parole e frasi",
                    icona = Icons.Filled.Translate,
                    onClick = onVaiATraduttore
                )
            )
        ),
        CategoriaAltro(
            nome = "Piano e Progressi",
            elementi = listOf(
                ElementoAltro(
                    titolo = "Piano di studio",
                    descrizione = "Tappe, certificazioni e progresso verso il B2",
                    icona = Icons.Filled.School,
                    onClick = onVaiAPiano,
                    usaTertiary = true
                ),
                ElementoAltro(
                    titolo = "Sessioni",
                    descrizione = "Sessione guidata, test, roleplay, ripasso",
                    icona = Icons.Filled.PlayArrow,
                    onClick = onVaiASessioni
                ),
                ElementoAltro(
                    titolo = "Statistiche",
                    descrizione = "Progressi e statistiche",
                    icona = Icons.Filled.BarChart,
                    onClick = onVaiAStats
                ),
                ElementoAltro(
                    titolo = "Competenze",
                    descrizione = "Punteggi e livello complessivo",
                    icona = Icons.Filled.School,
                    onClick = onVaiACompetenze
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Altro", style = MaterialTheme.typography.titleLarge) }
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
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                categorie.forEach { categoria ->
                    // Titolo categoria
                    Text(
                        text = categoria.nome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    // Card della categoria
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column {
                            categoria.elementi.forEachIndexed { index, elemento ->
                                VoceAltro(elemento = elemento)
                                if (index < categoria.elementi.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                Card(
                    onClick = onVaiAContestoMedico,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏥",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Contesto Medico",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Lessico e scenari per fisioterapia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                Card(
                    onClick = onVaiAProgressione,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📈",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Percorso di Apprendimento",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Vedi i tuoi obiettivi e progressi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    onClick = onVaiAProduzioneScritta,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✏️",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Produzione Scritta",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Esercizi di scrittura guidata",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun VoceAltro(elemento: ElementoAltro) {
    val containerColor = if (elemento.usaTertiary) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (elemento.usaTertiary) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = elemento.onClick,
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                elemento.icona,
                contentDescription = null,
                tint = if (elemento.usaTertiary) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    elemento.titolo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (elemento.usaTertiary) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    elemento.descrizione,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
        }
    }
}
