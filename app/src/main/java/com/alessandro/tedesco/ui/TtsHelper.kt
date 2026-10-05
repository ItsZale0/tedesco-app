package com.alessandro.tedesco.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int = tts.setLanguage(Locale.GERMAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Tedesco non supportato sul dispositivo
            }
        }
    }

    fun speak(text: String) {
        if (text.isNotBlank()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "german_pronunciation")
        }
    }

    fun shutdown() {
        tts.shutdown()
    }
}

@Composable
fun rememberTtsHelper(context: Context): TtsHelper {
    val helper = remember { TtsHelper(context) }
    DisposableEffect(Unit) {
        onDispose { helper.shutdown() }
    }
    return helper
}