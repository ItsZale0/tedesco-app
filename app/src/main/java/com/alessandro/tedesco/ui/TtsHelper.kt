package com.alessandro.tedesco.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.ViewModel
import java.util.Locale

class TtsHelper(context: Context) {
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.GERMAN)
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