package com.alessandro.tedesco.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.theme.TedescoTheme

class MainActivity : ComponentActivity() {
    /**
     * Il motore TTS condiviso si libera quando l'app non e' piu' in primo piano:
     * nessuna risorsa audio resta impegnata mentre l'app e' in background.
     */
    override fun onStop() {
        super.onStop()
        TtsCondiviso.chiudi()
        // Salva SUBITO e in modo sincrono: se aspettiamo una coroutine, Android
        // puo' killare il processo prima che il salvataggio finisca (dati persi).
        runBlocking(Dispatchers.IO) {
            runCatching { TedescoApp.get(this@MainActivity).profileManagerInstance.flush() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as TedescoApp

        // Android 13+: senza questo permesso le notifiche del worker di
        // auto-update (e dei promemoria) vengono scartate in silenzio.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            // Material 3: dynamic color su Android 12+, altrimenti schema baseline.
            // App pensata dark: per seguire il sistema basta togliere darkTheme = true.
            TedescoTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav()
                }
            }
        }
    }
}
