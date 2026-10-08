package com.alessandro.tedesco.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale

class TtsHelper(context: Context, private val locale: Locale = Locale.GERMAN) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech
    private var ready = false
    var isSpeaking by mutableStateOf(false)
        private set

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int = tts.setLanguage(locale)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            if (ready) {
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                    }
                    @Deprecated("deprecated in TTS API")
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                    }
                })
            }
        }
    }

    fun speak(text: String) {
        if (text.isNotBlank() && ready) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${locale.language}_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts.stop()
        isSpeaking = false
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

@Composable
fun rememberTtsHelper(context: Context, locale: Locale = Locale.GERMAN): TtsHelper {
    val helper = remember(locale) { TtsHelper(context, locale) }
    DisposableEffect(locale) {
        onDispose { helper.shutdown() }
    }
    return helper
}