package com.alessandro.tedesco.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.DidatticaImmersione
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.delay

/**
 * Modalità Immersione: la didattica in 5 passi del metodo Deutsch Hub.
 *
 * 1. Mentalità (testo): il tedesco è logico.
 * 2. Input comprensibile: micro-storia con traduzioni rivelabili + quiz.
 * 3. Vocabolario in frasi: frasi nuove da annotare, parole nel contesto,
 *    scomposizione dei composti.
 * 4. Shadowing: ascolta e ripeti col TTS, anche a velocità ridotta.
 * 5. Ascolto passivo: la serie in loop, pausa proporzionale alla lunghezza,
 *    stop manuale — da usare anche prima di dormire.
 */
@Composable
fun DidatticaScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    val context = LocalContext.current
    val tts = rememberTtsHelper(context)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.codiceBreve() ?: "A1"

    val storia = remember(livello) { DidatticaImmersione.storiaAttiva(livello) }
    var giro by rememberSaveable(livello) { mutableIntStateOf(0) }
    val frasiNuove = remember(livello, giro) { DidatticaImmersione.frasiNuove(livello, giro) }
    val parole = remember(livello) { DidatticaImmersione.paroleNelContesto(livello) }
    val composti = remember(livello) { DidatticaImmersione.composti(livello) }
    val shadowing = remember(livello) { DidatticaImmersione.shadowing(livello) }
    val frasiAscolto = remember(livello) { DidatticaImmersione.frasiPassive(livello) }

    /** Indici delle traduzioni della storia già rivelate dall'utente. */
    var rivelate by remember(storia) { mutableStateOf(setOf<Int>()) }
    /** Risposte del quiz, per indice di domanda. */
    val scelte = remember(storia) { mutableStateMapOf<Int, Int>() }

    var ascoltoAttivo by remember { mutableStateOf(false) }
    var indiceAscolto by remember { mutableIntStateOf(0) }
    /** true da quando l'utente ha chiesto per la prima volta un audio. */
    var haToccatoAudio by remember { mutableStateOf(false) }
    /** true se, dopo il primo audio richiesto, la voce tedesca non è mai partita. */
    var avvisoVoce by remember { mutableStateOf(false) }

    // Il motore è condiviso e nasce pigro: dopo il primo tocco si dà 3 secondi
    // per crearsi; se dopo non è pronto, avvisiamo invece del silenzio.
    LaunchedEffect(haToccatoAudio) {
        if (!haToccatoAudio) return@LaunchedEffect
        delay(3_000)
        avvisoVoce = !TtsCondiviso.linguaPronta
    }

    fun ascoltaTesto(testo: String, velocita: Float = 1f) {
        haToccatoAudio = true
        tts.speak(testo, velocita)
    }

    DisposableEffect(Unit) {
        onDispose {
            ascoltoAttivo = false
            tts.stop()
        }
    }

    // Ascolto passivo: una frase alla volta, pausa proporzionale alla lunghezza.
    LaunchedEffect(ascoltoAttivo, indiceAscolto, frasiAscolto) {
        if (!ascoltoAttivo) return@LaunchedEffect
        if (indiceAscolto >= frasiAscolto.size) {
            ascoltoAttivo = false
            return@LaunchedEffect
        }
        val frase = frasiAscolto[indiceAscolto]
        tts.speak(frase, 0.9f)
        // Attende la fine vera della sintesi (o lo stop manuale). Il motore è
        // condiviso: subito dopo speak() parlando può non essere ancora true,
        // quindi prima si aspetta che parta, poi che tocchi.
        var attesa = 0L
        while (ascoltoAttivo && !tts.isSpeaking && attesa < 2_000L) { delay(100); attesa += 100 }
        while (ascoltoAttivo && tts.isSpeaking) delay(150)
        // Pausa tra le frasi: più lunga per le frasi più lunghe.
        val pausa = (frase.length * 55L).coerceIn(1_200L, 6_500L)
        var riposata = 0L
        while (ascoltoAttivo && riposata < pausa) { delay(100); riposata += 100 }
        if (ascoltoAttivo) indiceAscolto = indiceAscolto + 1
    }

    fun giocoNuovo() {
        giro = giro + 1
        rivelate = emptySet()
        scelte.clear()
    }

    Scaffold(
        topBar = { CompactTopBar("Modalità Immersione", onBack = onIndietro) },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = spaziaturaSchermo(), vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- Il metodo ----------
            item {
                GlassCard {
                    Text(
                        "Il metodo in 5 passi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "1. Il tedesco non è caos: è logico, lo si capisce.\n" +
                            "2. Tanto input: leggi e ascolta, produci dopo.\n" +
                            "3. Parole sempre in FRASI + composti a pezzi.\n" +
                            "4. Shadowing: ripeti in tempo reale con la voce.\n" +
                            "5. Ogni giorno: storia, ripasso, ascolto passivo.\n" +
                            "80% input qui, 20% produzione: quando hai finito, va su Produzione o Verifica.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------- Passo 2: la storia ----------
            item {
                GlassCard {
                    if (avvisoVoce) {
                        Text(
                            "La voce tedesca non sembra disponibile: il telefono non ha un motore di sintesi con l'audio in tedesco (controlla le impostazioni di sistema → Sintesi vocale).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        "Immersione · ${storia.titolo}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        storia.contesto,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    storia.frasi.forEachIndexed { i, frase ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    frase.tedesco,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (i in rivelate) {
                                    Text(
                                        frase.italiano,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { ascoltaTesto(frase.tedesco) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Ascolta la frase")
                            }
                            IconButton(
                                onClick = {
                                    rivelate = if (i in rivelate) rivelate - i else rivelate + i
                                }
                            ) {
                                Icon(
                                    if (i in rivelate) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    if (i in rivelate) "Nascondi traduzione" else "Mostra traduzione"
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // ---------- Quiz di comprensione ----------
            item {
                GlassCard {
                    Text(
                        "Hai capito?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    storia.domande.forEachIndexed { d, domanda ->
                        val scelta = scelte[d]
                        Text(
                            domanda.domanda,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        domanda.opzioni.forEachIndexed { o, opzione ->
                            val selezionata = scelta == o
                            val corretta = domanda.corretta(scelta)
                            val colore = when {
                                selezionata && corretta -> MaterialTheme.colorScheme.primary
                                selezionata && !corretta -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                ("— $opzione"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colore,
                                fontWeight = if (selezionata) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (scelta == null) scelte[d] = o
                                    }
                                    .padding(vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    Button(onClick = { giocoNuovo() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Fatto: ho capito la storia, avanza")
                    }
                }
            }

            // ---------- Passo 3a: frasi nuove da annotare ----------
            item {
                GlassCard {
                    Text(
                        "Ripasso · Cinque frasi da annotare",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Copia ogni frase nel quaderno: la parola nuova arriva dentro la frase, mai da sola.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    frasiNuove.forEach { frase ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(frase.tedesco, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    frase.italiano,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { ascoltaTesto(frase.tedesco) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Ascolta")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { giocoNuovo() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Annotato tutto: avanza") }
                }
            }

            // ---------- Passo 3b: parole nel contesto ----------
            item {
                GlassCard {
                    Text(
                        "Parole nel contesto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    parole.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${p.parola} — ${p.significato}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    p.frase,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    p.traduzione,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { ascoltaTesto(p.frase) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Ascolta la frase")
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            // ---------- Passo 3c: composti ----------
            item {
                GlassCard {
                    Text(
                        "Composti: ogni pezzo apre dieci porte",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    composti.forEach { c ->
                        Column(Modifier.fillMaxWidth()) {
                            Text(
                                "${c.composto} = ${c.scomposizione()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${c.parti.zip(c.significati).joinToString(" + ") { it.second }} → ${c.italiano}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }

            // ---------- Passo 4: shadowing ----------
            item {
                GlassCard {
                    Text(
                        "Shadowing: ascolta, poi ripeti all'unisono",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Prima ascolta a velocità ridotta e ripeti sopra la voce; poi riprova a velocità piena.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    shadowing.forEach { s ->
                        Column(Modifier.fillMaxWidth()) {
                            Text(s.tedesco, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                s.italiano,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                s.notaRitmo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                IconButton(onClick = { ascoltaTesto(s.tedesco, 0.65f) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Ascolta lentamente")
                                }
                                IconButton(onClick = { ascoltaTesto(s.tedesco) }) {
                                    Icon(Icons.Filled.PlayArrow, "Ascolta a velocità piena")
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }

            // ---------- Passo 5: ascolto passivo ----------
            item {
                GlassCard {
                    Text(
                        "Ascolto passivo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tutte le frasi del livello, una dopo l'altra, senza guardare. Ottimo mentre cammini o prima di dormire.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    val inCorso = frasiAscolto.getOrNull(indiceAscolto)
                    inCorso?.takeIf { ascoltoAttivo }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (ascoltoAttivo) {
                            Button(
                                onClick = {
                                    ascoltoAttivo = false
                                    tts.stop()
                                }
                            ) {
                                Icon(Icons.Filled.Stop, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Ferma")
                            }
                            Text(
                                "In ascolto…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Button(
                                onClick = {
                                    indiceAscolto = 0
                                    ascoltoAttivo = true
                                }
                            ) {
                                Icon(Icons.Filled.PlayArrow, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Dal primo (bonus: pre-sonno)")
                            }
                        }
                    }
                }
            }
        }
    }
}
