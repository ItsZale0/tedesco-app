@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.EsempioGrammatica
import com.alessandro.tedesco.data.EsercizioUnita
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.TabellaGrammatica
import com.alessandro.tedesco.data.TipoEsercizioGrammatica
import com.alessandro.tedesco.data.UnitaGrammatica
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Una unità del percorso grammaticale.
 *
 * Flusso: teoria -> esercizi -> verifica di padronanza.
 * Il risultato viene salvato una sola volta, alla fine della sessione.
 */
@Composable
fun UnitaGrammaticaScreen(
    vm: TedescoViewModel,
    unitaId: String,
    onIndietro: () -> Unit,
    onCompletata: () -> Unit
) {
    val unita = remember(unitaId) { GrammaticaSyllabus.perId(unitaId) }
    if (unita == null) {
        ScreenScaffold(title = "Unità non trovata", onBack = onIndietro) {
            Text("Questa unità non esiste o non è più disponibile.")
        }
        return
    }

    val context = LocalContext.current
    val tts = rememberTtsHelper(context)

    var scheda by rememberSaveable(unitaId) { mutableIntStateOf(0) }
    var indice by rememberSaveable(unitaId) { mutableIntStateOf(0) }
    var corrette by rememberSaveable(unitaId) { mutableIntStateOf(0) }
    var scelta by rememberSaveable(unitaId) { mutableIntStateOf(-1) }
    var testo by rememberSaveable(unitaId) { mutableStateOf("") }
    var verificato by rememberSaveable(unitaId) { mutableStateOf(false) }
    var finito by rememberSaveable(unitaId) { mutableStateOf(false) }
    var salvato by rememberSaveable(unitaId) { mutableStateOf(false) }

    val esercizio = unita.esercizi.getOrNull(indice)
    val percentuale = if (unita.esercizi.isEmpty()) 0
    else corrette * 100 / unita.esercizi.size
    val promosso = percentuale >= GrammaticaSyllabus.SOGLIA_PADRONANZA

    fun resetPratica() {
        indice = 0
        corrette = 0
        scelta = -1
        testo = ""
        verificato = false
        finito = false
        salvato = false
    }

    fun verificaRisposta() {
        val e = esercizio ?: return
        val corretto = if (e.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            GrammaticaSyllabus.rispostaSceltaCorretta(
                e,
                scelta.takeIf { it >= 0 }
            )
        } else {
            GrammaticaSyllabus.rispostaTestoCorretta(e, testo)
        }
        verificato = true
        if (corretto) corrette += 1
    }

    fun prossimaDomanda() {
        if (indice < unita.esercizi.lastIndex) {
            indice += 1
            scelta = -1
            testo = ""
            verificato = false
        } else {
            finito = true
        }
    }

    LaunchedEffect(finito) {
        if (finito && !salvato) {
            vm.salvaPadronanzaGrammatica(
                unitaId = unita.id,
                corrette = corrette,
                totali = unita.esercizi.size
            )
            salvato = true
        }
    }

    ScreenScaffold(
        title = "${unita.livello} - ${unita.titolo}",
        onBack = onIndietro
    ) {
        IntestazioneUnita(unita)

        Spacer(Modifier.height(Spaziature.md))

        Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
            FilterChip(
                selected = scheda == 0,
                onClick = { scheda = 0 },
                label = { Text("Teoria") }
            )
            FilterChip(
                selected = scheda == 1,
                onClick = {
                    if (finito) resetPratica()
                    scheda = 1
                },
                label = { Text("Esercizi") }
            )
        }

        Spacer(Modifier.height(Spaziature.md))

        if (scheda == 0) {
            TeoriaUnita(
                unita = unita,
                onAscolta = { tts.speak(it) },
                onVaiEsercizi = {
                    if (finito) resetPratica()
                    scheda = 1
                }
            )
        } else if (finito) {
            RisultatoUnita(
                unita = unita,
                corrette = corrette,
                percentuale = percentuale,
                promosso = promosso,
                onRiprova = {
                    resetPratica()
                    scheda = 1
                },
                onTornaPercorso = onCompletata,
                onRivediTeoria = { scheda = 0 }
            )
        } else if (esercizio != null) {
            EsercizioUnitaCard(
                unita = unita,
                esercizio = esercizio,
                indice = indice,
                scelta = scelta,
                testo = testo,
                verificato = verificato,
                onScelta = { scelta = it },
                onTesto = { testo = it },
                onVerifica = { verificaRisposta() },
                onAvanti = { prossimaDomanda() }
            )
        }
    }
}

@Composable
private fun IntestazioneUnita(unita: UnitaGrammatica) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(Spaziature.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        unita.livello,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.width(Spaziature.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    unita.titolo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    unita.sottotitolo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    unita.obiettivo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${unita.minuti} minuti - ${unita.esercizi.size} esercizi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun TeoriaUnita(
    unita: UnitaGrammatica,
    onAscolta: (String) -> Unit,
    onVaiEsercizi: () -> Unit
) {
    Column {
        SezioneTitolo("Regola", "Leggi con calma: è il cuore della unità.")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(Spaziature.md)) {
                unita.teoria.regola.forEach { regola ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "-",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        MarkdownText(
                            markdown = regola,
                            color = MaterialTheme.colorScheme.onSurface,
                            stile = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        unita.teoria.tabelle.forEach { tabella ->
            TabellaCard(tabella)
            Spacer(Modifier.height(Spaziature.md))
        }

        SezioneTitolo("Esempi", "Tocca l'altoparlante per ascoltare.")
        unita.teoria.esempi.forEach { esempio ->
            EsempioCard(esempio = esempio, onAscolta = { onAscolta(esempio.tedesco) })
            Spacer(Modifier.height(Spaziature.sm))
        }

        if (unita.teoria.erroriTipici.isNotEmpty()) {
            Spacer(Modifier.height(Spaziature.sm))
            SezioneTitolo("Errori tipici", "Gli errori più frequenti di chi parla italiano.")
            unita.teoria.erroriTipici.forEach { errore ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(Spaziature.sm)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "X",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                errore.sbagliato,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                errore.corretto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            errore.spiegazione,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (unita.teoria.merksatz.isNotBlank()) {
            Spacer(Modifier.height(Spaziature.md))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(
                        "Merksatz",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        unita.teoria.merksatz,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(Spaziature.lg))
        Button(
            onClick = onVaiEsercizi,
            modifier = Modifier
                .fillMaxWidth()
                .height(AltezzaBottonePrincipale())
        ) {
            Text("Passa agli esercizi")
            Spacer(Modifier.width(Spaziature.sm))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(Spaziature.sm))
    }
}

@Composable
private fun TabellaCard(tabella: TabellaGrammatica) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.sm)) {
            Text(
                tabella.titolo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                tabella.intestazioni.forEach { cella ->
                    Text(
                        cella,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            tabella.righe.forEach { riga ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    riga.forEach { cella ->
                        Text(
                            cella,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (tabella.nota.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    tabella.nota,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EsempioCard(
    esempio: EsempioGrammatica,
    onAscolta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spaziature.md, top = Spaziature.sm, bottom = Spaziature.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    esempio.tedesco,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    esempio.italiano,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (esempio.nota.isNotBlank()) {
                    Text(
                        esempio.nota,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onAscolta) {
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = "Ascolta l'esempio",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EsercizioUnitaCard(
    unita: UnitaGrammatica,
    esercizio: EsercizioUnita,
    indice: Int,
    scelta: Int,
    testo: String,
    verificato: Boolean,
    onScelta: (Int) -> Unit,
    onTesto: (String) -> Unit,
    onVerifica: () -> Unit,
    onAvanti: () -> Unit
) {
    val rispostaCorretta = when (esercizio.tipo) {
        TipoEsercizioGrammatica.SCELTA_MULTIPLA ->
            esercizio.opzioni.getOrNull(esercizio.rispostaCorretta).orEmpty()

        else -> esercizio.rispostaTesto
    }
    val corretto = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
        GrammaticaSyllabus.rispostaSceltaCorretta(
            esercizio,
            scelta.takeIf { it >= 0 }
        )
    } else {
        GrammaticaSyllabus.rispostaTestoCorretta(esercizio, testo)
    }
    val etichettaInput = when (esercizio.tipo) {
        TipoEsercizioGrammatica.COMPLETAMENTO -> "Scrivi la parola mancante"
        TipoEsercizioGrammatica.TRASFORMAZIONE -> "Scrivi la frase trasformata"
        TipoEsercizioGrammatica.PRODUZIONE -> "Scrivi la traduzione"
        TipoEsercizioGrammatica.SCELTA_MULTIPLA -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Esercizio ${indice + 1} di ${unita.esercizi.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${unita.livello} - ${unita.titolo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (indice + if (verificato) 1 else 0).toFloat() / unita.esercizi.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            )
            Spacer(Modifier.height(Spaziature.md))
            Text(
                esercizio.domanda,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (esercizio.suggerimento.isNotBlank() && !verificato) {
                Spacer(Modifier.height(2.dp))
                Text(
                    esercizio.suggerimento,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spaziature.sm))

            if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                esercizio.opzioni.forEachIndexed { i, opzione ->
                    val selezionato = scelta == i
                    val corretta = i == esercizio.rispostaCorretta
                    val colore = when {
                        verificato && corretta -> MaterialTheme.colorScheme.primaryContainer
                        verificato && selezionato -> MaterialTheme.colorScheme.errorContainer
                        selezionato -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable(enabled = !verificato) { onScelta(i) },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = colore),
                        border = BorderStroke(
                            if (selezionato && !verificato) 2.dp else 1.dp,
                            if (selezionato && !verificato) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (selezionato) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "ABCD".getOrElse(i) { '?' }.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selezionato) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.width(Spaziature.sm))
                            Text(
                                opzione,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (verificato && corretta) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = testo,
                    onValueChange = onTesto,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(etichettaInput) },
                    enabled = !verificato,
                    singleLine = false,
                    minLines = 1,
                    maxLines = 3
                )
            }

            if (verificato) {
                Spacer(Modifier.height(Spaziature.sm))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (corretto) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(Modifier.padding(Spaziature.sm)) {
                        Text(
                            if (corretto) "Corretto!" else "Non corretto",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (!corretto) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Risposta corretta: $rispostaCorretta",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (esercizio.spiegazione.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                esercizio.spiegazione,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spaziature.md))

            if (!verificato) {
                Button(
                    onClick = onVerifica,
                    enabled = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                        scelta >= 0
                    } else {
                        testo.isNotBlank()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AltezzaBottonePrincipale())
                ) {
                    Text("Verifica risposta")
                }
            } else {
                Button(
                    onClick = onAvanti,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AltezzaBottonePrincipale())
                ) {
                    Text(
                        if (indice < unita.esercizi.lastIndex) "Prossima domanda"
                        else "Vedi risultato"
                    )
                }
            }
        }
    }
}

@Composable
private fun RisultatoUnita(
    unita: UnitaGrammatica,
    corrette: Int,
    percentuale: Int,
    promosso: Boolean,
    onRiprova: () -> Unit,
    onTornaPercorso: () -> Unit,
    onRivediTeoria: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (promosso) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spaziature.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (promosso) "Unità completata" else "Ripassa ancora",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                "$corrette / ${unita.esercizi.size}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "$percentuale% di risposte corrette",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                if (promosso) {
                    "Hai raggiunto la soglia dell'80%. La prossima unità è sbloccata; il ripasso tornera' tra 1, 3, 7 e 16 giorni."
                } else {
                    "Serve almeno l'80% per sbloccare la unità successiva. Rileggi la regola e riprova: gli errori sono la parte più utile."
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.md))
            Button(
                onClick = onTornaPercorso,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale())
            ) {
                Text("Torna al percorso")
            }
            Spacer(Modifier.height(Spaziature.sm))
            Button(
                onClick = onRiprova,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale())
            ) {
                Text("Riprova gli esercizi")
            }
            TextButton(onClick = onRivediTeoria) {
                Text("Rivedi la teoria")
            }
        }
    }

    Spacer(Modifier.height(Spaziature.md))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Text(
                "Cosa porti via",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spaziature.xs))
            Text(
                unita.teoria.merksatz,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(Spaziature.xs))
            Text(
                "Ripasso programmato: 1, 3, 7 e 16 giorni. La unità resta accessibile anche quando è completata.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SezioneTitolo(titolo: String, sottotitolo: String = "") {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            titolo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (sottotitolo.isNotBlank()) {
            Text(
                sottotitolo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
