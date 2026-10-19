package com.alessandro.tedesco.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Motore di sintesi vocale CONDIVISO da tutta l'app.
 *
 * Prima ogni schermata creava il proprio [TextToSpeech] (15 punti di chiamata):
 * ogni istanza apre un collegamento al servizio di sintesi, carica i dati della
 * lingua e trattiene il Context (che era quello dell'Activity, non
 * dell'application). Entrando e uscendo dalle schermate si accumulavano motori e
 * memoria: e' la voce piu' pesante del processo.
 *
 * Ora il motore e' UNO SOLO:
 * - creato alla prima riproduzione (prima nessuna risorsa impegnata);
 * - legato all'application context, quindi non puo' trattenere l'Activity;
 * - riusato da tutte le schermate, che cambiano solo la lingua con setLanguage().
 */
object TtsCondiviso {

    private var motore: TextToSpeech? = null
    private var pronto = false
    private var linguaCorrente: Locale = Locale.GERMAN
    private var inCoda: String? = null

    /** Stato osservabile: true mentre il motore sta parlando. */
    var parlando by mutableStateOf(false)
        private set

    /** Riproduce [testo]; crea il motore se non esiste ancora. */
    fun parla(context: Context, testo: String, locale: Locale = Locale.GERMAN) {
        if (testo.isBlank()) return
        val attuale = motore
        if (attuale != null && pronto) {
            if (locale != linguaCorrente) {
                val esito = attuale.setLanguage(locale)
                pronto = esito != TextToSpeech.LANG_MISSING_DATA &&
                    esito != TextToSpeech.LANG_NOT_SUPPORTED
                linguaCorrente = locale
            }
            if (pronto) {
                riproduci(attuale, testo)
                return
            }
        }
        crea(context, testo, locale)
    }

    /** Ferma la riproduzione corrente (il motore resta caldo, pronto al riuso). */
    fun ferma() {
        motore?.stop()
        parlando = false
    }

    /** Chiude il motore: da usare solo quando l'app va in background o muore. */
    fun chiudi() {
        motore?.stop()
        motore?.shutdown()
        motore = null
        pronto = false
        inCoda = null
        parlando = false
    }

    private fun crea(context: Context, testo: String, locale: Locale) {
        chiudi()
        inCoda = testo
        linguaCorrente = locale
        // applicationContext: il motore non trattiene mai l'Activity.
        motore = TextToSpeech(context.applicationContext) { stato ->
            if (stato == TextToSpeech.SUCCESS) {
                val esito = motore?.setLanguage(locale)
                pronto = esito != TextToSpeech.LANG_MISSING_DATA &&
                    esito != TextToSpeech.LANG_NOT_SUPPORTED
                motore?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { parlando = true }
                    override fun onDone(utteranceId: String?) { parlando = false }
                    @Deprecated("deprecated in TTS API")
                    override fun onError(utteranceId: String?) { parlando = false }
                })
                // Se l'utente ha chiesto di parlare prima che il motore fosse
                // pronto, la richiesta e' rimasta in coda: la eseguiamo ora.
                val daDire = inCoda
                inCoda = null
                if (pronto && daDire != null) riproduci(motore!!, daDire)
            } else {
                pronto = false
                inCoda = null
            }
        }
    }

    private fun riproduci(tts: TextToSpeech, testo: String) {
        tts.speak(testo, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }
}

/**
 * Wrapper usato dalle schermate. Non possiede piu' un motore proprio: delega a
 * [TtsCondiviso], cosi' tutte le schermate usano la stessa istanza.
 */
class TtsHelper(context: Context, private val locale: Locale = Locale.GERMAN) {

    private val appContext: Context = context.applicationContext

    val isSpeaking: Boolean
        get() = TtsCondiviso.parlando

    fun speak(text: String) = TtsCondiviso.parla(appContext, text, locale)

    fun stop() = TtsCondiviso.ferma()

    /**
     * Non spegne piu' il motore: il motore e' condiviso e resta a disposizione
     * delle altre schermate. Prima ogni uscita da una schermata distruggeva un
     * TextToSpeech e la successiva lo ricreava da zero.
     */
    fun shutdown() = TtsCondiviso.ferma()
}

@Composable
fun rememberTtsHelper(context: Context, locale: Locale = Locale.GERMAN): TtsHelper {
    return remember(locale) { TtsHelper(context, locale) }
}
