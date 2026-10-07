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
import androidx.compose.material3.Text
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

// Palette Material You — tonalità M3 (basate su seed blu)
private val Blu = Color(0xFF415F91)
private val BluChiaro = Color(0xFFA9C7FF)

private val SchemaChiaro = lightColorScheme(
    primary = Color(0xFF415F91),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF565F71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDAE2F9),
    onSecondaryContainer = Color(0xFF131C2B),
    tertiary = Color(0xFF705575),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFAD8FD),
    onTertiaryContainer = Color(0xFF28132F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFEFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

private val SchemaScuro = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF284777),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBEC6DC),
    onSecondary = Color(0xFF283141),
    secondaryContainer = Color(0xFF3E4759),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = Color(0xFFDDBCE0),
    onTertiary = Color(0xFF3F2844),
    tertiaryContainer = Color(0xFF573E5C),
    onTertiaryContainer = Color(0xFFFAD8FD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E)
)

private val TipografiaBase = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Normal, lineHeight = 64.sp, letterSpacing = -0.25.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Normal, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Normal, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Normal, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Normal, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

/** Spaziature standard per padding e gap */
object Spaziature {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/** Raggi standard per card, chip e bottoni */
object Raggi {
    val card = 28.dp
    val chip = 16.dp
    val bottone = 20.dp
}

/** Altezza del bottone principale, adattiva */
@Composable
fun AltezzaBottonePrincipale(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 56.dp
        else -> 60.dp
    }
}

/** Titolo coerente per tutte le TopAppBar */
@Composable
fun TitoloSchermata(testo: String) {
    Text(
        text = testo,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}

/** Breakpoints per responsive design (in dp) */
object Breakpoints {
    const val COMPACT = 0
    const val MEDIUM = 600
    const val EXPANDED = 840
    const val LARGE = 1200
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
        WindowSize.COMPACT -> 16
        WindowSize.MEDIUM -> 20
        WindowSize.EXPANDED -> 28
    }
}

/** Tipografia adattiva - scala per tablet */
@Composable
fun responsiveTypography(): Typography {
    val windowSize = rememberWindowSizeClass()
    return when (windowSize) {
        WindowSize.COMPACT -> TipografiaBase
        WindowSize.MEDIUM -> TipografiaBase.copy(
            displayLarge = TipografiaBase.displayLarge.copy(fontSize = 68.sp),
            headlineLarge = TipografiaBase.headlineLarge.copy(fontSize = 38.sp),
            titleLarge = TipografiaBase.titleLarge.copy(fontSize = 26.sp),
            bodyLarge = TipografiaBase.bodyLarge.copy(fontSize = 19.sp)
        )
        WindowSize.EXPANDED -> TipografiaBase.copy(
            displayLarge = TipografiaBase.displayLarge.copy(fontSize = 76.sp),
            headlineLarge = TipografiaBase.headlineLarge.copy(fontSize = 42.sp),
            titleLarge = TipografiaBase.titleLarge.copy(fontSize = 30.sp),
            bodyLarge = TipografiaBase.bodyLarge.copy(fontSize = 21.sp),
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
