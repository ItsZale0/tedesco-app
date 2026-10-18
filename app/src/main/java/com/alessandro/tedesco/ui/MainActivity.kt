package com.alessandro.tedesco.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.theme.PaletteApp
import com.alessandro.tedesco.ui.theme.TedescoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as TedescoApp

        setContent {
            val paletteId by app.profileManagerInstance.paletteFlow
                .collectAsStateWithLifecycle("uber")

            TedescoTheme(
                darkTheme = true,
                palette = PaletteApp.daId(paletteId)
            ) {
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
