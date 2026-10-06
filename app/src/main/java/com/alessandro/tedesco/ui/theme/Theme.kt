package com.alessandro.tedesco.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

private val TipografiaBase = Typography(
    displayLarge = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold, lineHeight = 80.sp, letterSpacing = -1.5.sp),
    displayMedium = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Bold, lineHeight = 64.sp, letterSpacing = -0.5.sp),
    displaySmall = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    headlineLarge = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold, lineHeight = 40.sp),
    headlineSmall = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
    titleSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodySmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    labelMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
)

/** Breakpoints per responsive design (in dp) */
object Breakpoints {
    const val SMALL = 360
    const val MEDIUM = 450
    const val LARGE = 600
    const val TABLET = 840
    const val DESKTOP = 1200
}

/** Classi dimensione finestra per layout adattivi */
enum class WindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}

/** Larghezza minima finestra, per non ritrovarsi con 0dp nei primi frame */
private const val LARGHEZZA_MINIMA_DP = 320

/** Ottiene la classe dimensione corrente basata sulla larghezza finestra */
@Composable
fun rememberWindowSizeClass(): WindowSize {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.coerceAtLeast(LARGHEZZA_MINIMA_DP)
    return remember(widthDp) {
        when {
            widthDp < 600 -> WindowSize.COMPACT
            widthDp < 840 -> WindowSize.MEDIUM
            else -> WindowSize.EXPANDED
        }
    }
}

/**
 * Larghezza massima del contenuto: su tablet evita righe lunghissime
 * e tiene il testo leggibile.
 */
@Composable
fun dimensioneContenuto(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> Dp.Unspecified
        WindowSize.MEDIUM -> 600.dp
        WindowSize.EXPANDED -> 720.dp
    }
}

/** Padding orizzontale dello schermo, adattivo */
@Composable
fun spaziaturaSchermo(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
}

/** Spacing verticale responsive in dp */
@Composable
fun responsiveVerticalSpacing(): Int {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> 16
        WindowSize.MEDIUM -> 20
        WindowSize.EXPANDED -> 24
    }
}

/** Numero colonne griglia responsive */
@Composable
fun responsiveGridColumns(): Int {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> 1
        WindowSize.MEDIUM -> 2
        WindowSize.EXPANDED -> 3
    }
}

/** Altezza toolbar responsive in dp */
@Composable
fun responsiveToolbarHeight(): Int {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> 56
        WindowSize.MEDIUM -> 64
        WindowSize.EXPANDED -> 72
    }
}

/** Dimensioni pulsante responsive in dp */
@Composable
fun responsiveButtonHeight(): Int {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> 48
        WindowSize.MEDIUM -> 52
        WindowSize.EXPANDED -> 56
    }
}

/** Raggio angoli responsive in dp */
@Composable
fun responsiveCornerRadius(): Int {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> 12
        WindowSize.MEDIUM -> 16
        WindowSize.EXPANDED -> 20
    }
}

/** Tipografia adattiva - scala per tablet */
@Composable
fun responsiveTypography(): Typography {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> TipografiaBase
        WindowSize.MEDIUM -> TipografiaBase.copy(
            displayLarge = TipografiaBase.displayLarge.copy(fontSize = 80.sp),
            headlineLarge = TipografiaBase.headlineLarge.copy(fontSize = 40.sp),
            titleLarge = TipografiaBase.titleLarge.copy(fontSize = 28.sp),
            bodyLarge = TipografiaBase.bodyLarge.copy(fontSize = 20.sp)
        )
        WindowSize.EXPANDED -> TipografiaBase.copy(
            displayLarge = TipografiaBase.displayLarge.copy(fontSize = 88.sp),
            headlineLarge = TipografiaBase.headlineLarge.copy(fontSize = 44.sp),
            titleLarge = TipografiaBase.titleLarge.copy(fontSize = 32.sp),
            bodyLarge = TipografiaBase.bodyLarge.copy(fontSize = 22.sp),
            bodyMedium = TipografiaBase.bodyMedium.copy(fontSize = 18.sp)
        )
    }
}

@Composable
fun TedescoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> SchemaScuro
        else -> SchemaChiaro
    }

    val typography = responsiveTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

/** Arrangement responsive per colonne */
@Composable
fun responsiveColumnArrangement(): Arrangement.Vertical {
    val windowSize = rememberWindowSizeClass()
    val spacing = responsiveVerticalSpacing()
    return Arrangement.spacedBy(spacing.dp)
}

/** Arrangement responsive per righe */
@Composable
fun responsiveRowArrangement(): Arrangement.Horizontal {
    val windowSize = rememberWindowSizeClass()
    val spacing = when (windowSize) {
        WindowSize.COMPACT -> 8
        WindowSize.MEDIUM -> 12
        WindowSize.EXPANDED -> 16
    }
    return Arrangement.spacedBy(spacing.dp)
}

/** Column responsive con padding e spacing automatici */
@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val arrangement = responsiveColumnArrangement()
    val horizontalPadding = when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
    Column(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxSize(),
        verticalArrangement = arrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/** Row responsive con spacing automatico */
@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val arrangement = responsiveRowArrangement()
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}