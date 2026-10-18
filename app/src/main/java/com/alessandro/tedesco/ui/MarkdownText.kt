package com.alessandro.tedesco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Renderer markdown UNICO dell'app (teoria lezioni, correzioni del tutor, feedback).
 *
 * Supporta: titoli (# ## ### ####), elenchi puntati (- * \u2022), elenchi numerati (1. 2.),
 * citazioni (>), separatori (---), righe vuote, **grassetto**, *corsivo*, `codice`,
 * link [testo](url) ridotti al solo testo.
 *
 * Le righe sono precalcolate una volta sola (un `remember`), così non si costruiscono
 * AnnotatedString a ogni ricomposizione.
 */
private enum class TipoRiga { TITOLO1, TITOLO2, TITOLO3, TITOLO4, PUNTATO, NUMERATO, CITAZIONE, SEPARATORE, VUOTO, PARAGRAFO }

private data class RigaMd(
    val tipo: TipoRiga,
    val testo: AnnotatedString = AnnotatedString(""),
    val numero: String = ""
)

private val REGEX_INLINE = Regex("""\*\*(.+?)\*\*|__(.+?)__|`(.+?)`|\*(.+?)\*|_(.+?)_""")
private val REGEX_LINK = Regex("""\[(.+?)\]\((.*?)\)""")
private val REGEX_TITOLO = Regex("""^(#{1,6})\s*(.*)$""")
private val REGEX_NUMERATO = Regex("""^(\d{1,2})[.)]\s+(.*)$""")
private val REGEX_SEPARATORE = Regex("""^(-{3,}|\*{3,}|_{3,})$""")

private fun inline(testo: String): AnnotatedString {
    val pulito = REGEX_LINK.replace(testo, "$1")
    val builder = AnnotatedString.Builder()
    var ultimo = 0
    for (m in REGEX_INLINE.findAll(pulito)) {
        if (m.range.first > ultimo) builder.append(pulito.substring(ultimo, m.range.first))
        val g = m.groupValues
        when {
            g[1].isNotEmpty() -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g[1]) }
            g[2].isNotEmpty() -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g[2]) }
            g[3].isNotEmpty() -> builder.withStyle(
                SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x22000000))
            ) { append(g[3]) }
            g[4].isNotEmpty() -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(g[4]) }
            g[5].isNotEmpty() -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(g[5]) }
        }
        ultimo = m.range.last + 1
    }
    if (ultimo < pulito.length) builder.append(pulito.substring(ultimo))
    return builder.toAnnotatedString()
}

private fun parseRighe(markdown: String): List<RigaMd> {
    val risultato = mutableListOf<RigaMd>()
    markdown.replace("\r\n", "\n").split("\n").forEach { grezza ->
        val riga = grezza.trim()
        when {
            riga.isEmpty() -> risultato.add(RigaMd(TipoRiga.VUOTO))
            REGEX_SEPARATORE.matches(riga) -> risultato.add(RigaMd(TipoRiga.SEPARATORE))
            REGEX_TITOLO.matches(riga) -> {
                val m = REGEX_TITOLO.find(riga)!!
                val livello = m.groupValues[1].length
                val testo = inline(m.groupValues[2])
                val tipo = when (livello) {
                    1 -> TipoRiga.TITOLO1
                    2 -> TipoRiga.TITOLO2
                    3 -> TipoRiga.TITOLO3
                    else -> TipoRiga.TITOLO4
                }
                risultato.add(RigaMd(tipo, testo))
            }
            riga.startsWith("> ") || riga == ">" -> {
                risultato.add(RigaMd(TipoRiga.CITAZIONE, inline(riga.removePrefix(">").trim())))
            }
            REGEX_NUMERATO.matches(riga) -> {
                val m = REGEX_NUMERATO.find(riga)!!
                risultato.add(RigaMd(TipoRiga.NUMERATO, inline(m.groupValues[2]), m.groupValues[1]))
            }
            riga.startsWith("- ") || riga.startsWith("* ") || riga.startsWith("\u2022 ") -> {
                risultato.add(RigaMd(TipoRiga.PUNTATO, inline(riga.drop(2).trim())))
            }
            else -> risultato.add(RigaMd(TipoRiga.PARAGRAFO, inline(riga)))
        }
    }
    // Rimuove le righe vuote consecutive e quelle in testa/fondo
    val compattate = mutableListOf<RigaMd>()
    risultato.forEach { r ->
        if (r.tipo == TipoRiga.VUOTO && (compattate.isEmpty() || compattate.last().tipo == TipoRiga.VUOTO)) return@forEach
        compattate.add(r)
    }
    while (compattate.isNotEmpty() && compattate.last().tipo == TipoRiga.VUOTO) compattate.removeAt(compattate.lastIndex)
    return compattate
}

@Composable
fun MarkdownText(
    markdown: String,
    color: Color,
    modifier: Modifier = Modifier,
    stile: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val righe = remember(markdown) { parseRighe(markdown) }
    Column(modifier = modifier) {
        righe.forEach { riga ->
            when (riga.tipo) {
                TipoRiga.TITOLO1 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                TipoRiga.TITOLO2 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                TipoRiga.TITOLO3 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
                TipoRiga.TITOLO4 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                TipoRiga.PUNTATO -> Row(modifier = Modifier.padding(start = 4.dp, top = 3.dp)) {
                    Text("\u2022", color = color.copy(alpha = 0.7f), style = stile)
                    Spacer(Modifier.width(8.dp))
                    Text(riga.testo, style = stile, color = color)
                }
                TipoRiga.NUMERATO -> Row(modifier = Modifier.padding(start = 4.dp, top = 3.dp)) {
                    Text("${riga.numero}.", color = color.copy(alpha = 0.7f), style = stile)
                    Spacer(Modifier.width(8.dp))
                    Text(riga.testo, style = stile, color = color)
                }
                TipoRiga.CITAZIONE -> Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 3.dp)
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        riga.testo,
                        style = stile,
                        color = color.copy(alpha = 0.9f),
                        fontStyle = FontStyle.Italic
                    )
                }
                TipoRiga.SEPARATORE -> HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = color.copy(alpha = 0.2f)
                )
                TipoRiga.VUOTO -> Spacer(Modifier.height(8.dp))
                TipoRiga.PARAGRAFO -> Text(
                    riga.testo,
                    style = stile,
                    color = color,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
