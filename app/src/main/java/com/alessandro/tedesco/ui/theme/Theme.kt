package com.alessandro.tedesco.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Blu = Color(0xFF1B5E9B)
private val BluChiaro = Color(0xFF9EC9F0)

private val SchemaChiaro = lightColorScheme(
    primary = Blu,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = Color(0xFF00696E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9DF0F6),
    onSecondaryContainer = Color(0xFF002021),
    tertiary = Color(0xFF7C5800),
    error = Color(0xFFBA1A1A)
)

private val SchemaScuro = darkColorScheme(
    primary = Color(0xFFA3C9FF),
    onPrimary = Color(0xFF00325B),
    primaryContainer = Color(0xFF004881),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFF81D3DA),
    onSecondary = Color(0xFF003739),
    secondaryContainer = Color(0xFF004F53),
    onSecondaryContainer = Color(0xFF9DF0F6),
    tertiary = Color(0xFFF2BE48),
    error = Color(0xFFFFB4AB)
)

private val Tipografia = Typography()

@Composable
fun TedescoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** colori dinamici: attivi su Android 12+, disattivati sotto */
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> SchemaScuro
        else -> SchemaChiaro
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Tipografia,
        content = content
    )
}
