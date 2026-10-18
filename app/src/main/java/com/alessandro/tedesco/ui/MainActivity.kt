package com.alessandro.tedesco.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.alessandro.tedesco.data.BackupEsecutore
import com.alessandro.tedesco.data.BackupSettings
import com.alessandro.tedesco.BuildConfig
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.theme.PaletteApp
import com.alessandro.tedesco.ui.theme.TedescoTheme
import com.alessandro.tedesco.ui.TedescoViewModel
import com.alessandro.tedesco.ui.StudioVeloceScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as TedescoApp

        setContent {
            // La palette arriva dal DataStore del profilo attivo e reagisce ai cambi
            val paletteId by app.profileManagerInstance.paletteFlow
                .collectAsStateWithLifecycle("uber")

            // BACKUP AUTOMATICO ogni 5 minuti mentre l'app e' in primo piano.
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(Unit) {
                val settings = BackupSettings(applicationContext)
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    while (true) {
                        delay(5 * 60 * 1000L) // 5 minuti
                        if (settings.autoAbilitato()) {
                            BackupEsecutore.esegui(
                                context = applicationContext,
                                versioneApp = BuildConfig.VERSION_NAME,
                                forza = false
                            )
                        }
                    }
                }
            }

            // Creazione del ViewModel (single source of truth per tutto il profilo)
            val vm = TedescoViewModel(app)

            TedescoTheme(
                darkTheme = true,
                palette = PaletteApp.daId(paletteId)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(vm = vm)
                }
            }
        }
    }
}
