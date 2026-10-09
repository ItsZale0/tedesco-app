package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

private data class ElementoAltro(
    val titolo: String,
    val descrizione: String,
    val icona: ImageVector,
    val onClick: () -> Unit,
    val usaTertiary: Boolean = false
)

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
    profilo: ProfiloUtente? = null,
    onVaiAPiano: () -> Unit = {},
    onVaiASessioni: () -> Unit = {},
    onVaiARoleplay: () -> Unit = {},
    onVaiAVoiceChat: () -> Unit = {},
    onVaiAContestoMedico: () -> Unit = {},
    onVaiAProgressione: () -> Unit = {},
    onVaiAProduzioneScritta: () -> Unit = {},
    onVaiAAscolto: () -> Unit = {},
    onVaiASpeaking: () -> Unit = {}
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
                    descrizione = "Test B1 + test adattivi personalizzati",
                    icona = Icons.Filled.Quiz,
                    onClick = onVaiATestB1
                ),
                ElementoAltro(
                    titolo = "Lettura e comprensione",
                    descrizione = "Quiz di lettura e vocabolario",
                    icona = Icons.Filled.Quiz,
                    onClick = onVaiAQuizComprensione
                ),
                ElementoAltro(
                    titolo = "Scrittura e grammatica",
                    descrizione = "Esercizi guidati e quiz di completamento",
                    icona = Icons.Filled.School,
                    onClick = onVaiAProduzioneScritta
                ),
                ElementoAltro(
                    titolo = "Ascolto",
                    descrizione = "Esercizi di ascolto (Hörverstehen) con voce tedesca",
                    icona = Icons.Filled.Hearing,
                    onClick = onVaiAAscolto
                )
            )
        ),
        CategoriaAltro(
            nome = "Pratica con AI",
            elementi = listOf(
                ElementoAltro(
                    titolo = "Tutor Tedesco",
                    descrizione = "Chat, correzione, roleplay e conversazione vocale",
                    icona = Icons.AutoMirrored.Filled.Chat,
                    onClick = onVaiATutor,
                    usaTertiary = true
                ),
                ElementoAltro(
                    titolo = "Speaking con AI",
                    descrizione = "Parla in tedesco con il microfono, l'AI ti corregge",
                    icona = Icons.Filled.Mic,
                    onClick = onVaiASpeaking,
                    usaTertiary = true
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
            nome = "Piano e progressi",
            elementi = buildList {
                add(ElementoAltro(
                    titolo = "Piano di studio",
                    descrizione = "Tappe, certificazioni e progresso verso il B2",
                    icona = Icons.Filled.PlayArrow,
                    onClick = onVaiAPiano,
                    usaTertiary = true
                ))
                add(ElementoAltro(
                    titolo = "Percorso di apprendimento",
                    descrizione = "Vedi i tuoi obiettivi e progressi livello per livello",
                    icona = Icons.Filled.BarChart,
                    onClick = onVaiAProgressione
                ))
                add(ElementoAltro(
                    titolo = "Sessioni",
                    descrizione = "Sessione guidata, test, roleplay, ripasso",
                    icona = Icons.Filled.PlayArrow,
                    onClick = onVaiASessioni
                ))
                add(ElementoAltro(
                    titolo = "Statistiche",
                    descrizione = "Progressi e statistiche",
                    icona = Icons.Filled.BarChart,
                    onClick = onVaiAStats
                ))
                add(ElementoAltro(
                    titolo = "Competenze",
                    descrizione = "Punteggi e livello complessivo",
                    icona = Icons.Filled.School,
                    onClick = onVaiACompetenze
                ))
                // Contesto medico solo per profili che lo richiedono (Alessandro)
                if (profilo?.config?.mostraContestoMedico == true) {
                    add(ElementoAltro(
                        titolo = "Contesto medico",
                        descrizione = "Lessico e scenari per fisioterapia",
                        icona = Icons.Filled.School,
                        onClick = onVaiAContestoMedico
                    ))
                }
            }
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
                    Text(
                        text = categoria.nome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

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
            Column(modifier = Modifier.weight(1f)) {
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
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }
    }
}
