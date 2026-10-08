package com.alessandro.tedesco.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alessandro.tedesco.data.local.StileDesign

// Palette Uber — nero, bianco, grigi neutri
private val Nero = Color(0xFF000000)
private val NeroChiaro = Color(0xFF1A1A1A)
private val GrigioScuro = Color(0xFF2A2A2A)
private val GrigioMedio = Color(0xFF4A4A4A)
private val GrigioChiaro = Color(0xFF6B6B6B)
private val GrigioMoltoChiaro = Color(0xFFE0E0E0)
private val Bianco = Color(0xFFFFFFFF)
private val BiancoSporco = Color(0xFFF5F5F5)

// Accent color (verde Uber)
private val VerdeUber = Color(0xFF06C167)
private val VerdeUberScuro = Color(0xFF05A657)

private val SchemaChiaro = lightColorScheme(
    primary = Nero,
    onPrimary = Bianco,
    primaryContainer = NeroChiaro,
    onPrimaryContainer = Bianco,
    secondary = GrigioScuro,
    onSecondary = Bianco,
    secondaryContainer = GrigioMoltoChiaro,
    onSecondaryContainer = Nero,
    tertiary = VerdeUber,
    onTertiary = Bianco,
    tertiaryContainer = Color(0xFFD4F5E2),
    onTertiaryContainer = Color(0xFF003820),
    error = Color(0xFFE53935),
    onError = Bianco,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Bianco,
    onBackground = Nero,
    surface = Bianco,
    onSurface = Nero,
    surfaceVariant = BiancoSporco,
    onSurfaceVariant = GrigioMedio,
    outline = GrigioChiaro,
    outlineVariant = GrigioMoltoChiaro
)

private val SchemaScuro = darkColorScheme(
    primary = Bianco,
    onPrimary = Nero,
    primaryContainer = GrigioScuro,
    onPrimaryContainer = Bianco,
    secondary = GrigioMoltoChiaro,
    onSecondary = Nero,
    secondaryContainer = GrigioMedio,
    onSecondaryContainer = Bianco,
    tertiary = VerdeUber,
    onTertiary = Nero,
    tertiaryContainer = VerdeUberScuro,
    onTertiaryContainer = Bianco,
    error = Color(0xFFEF5350),
    onError = Nero,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Bianco,
    background = Nero,
    onBackground = Bianco,
    surface = Nero,
    onSurface = Bianco,
    surfaceVariant = NeroChiaro,
    onSurfaceVariant = GrigioMoltoChiaro,
    outline = GrigioMedio,
    outlineVariant = GrigioScuro
)

// Palette alternative stile Uber
private val BluUber = Color(0xFF276EF1)
private val ArancioUber = Color(0xFFFF6B35)
private val ViolaUber = Color(0xFF7B2FBE)
private val RossoUber = Color(0xFFE53935)

private val SchemaChiaroBlu = lightColorScheme(
    primary = BluUber,
    onPrimary = Bianco,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = GrigioScuro,
    onSecondary = Bianco,
    secondaryContainer = GrigioMoltoChiaro,
    onSecondaryContainer = Nero,
    tertiary = VerdeUber,
    onTertiary = Bianco,
    tertiaryContainer = Color(0xFFD4F5E2),
    onTertiaryContainer = Color(0xFF003820),
    error = Color(0xFFE53935),
    onError = Bianco,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Bianco,
    onBackground = Nero,
    surface = Bianco,
    onSurface = Nero,
    surfaceVariant = BiancoSporco,
    onSurfaceVariant = GrigioMedio,
    outline = GrigioChiaro,
    outlineVariant = GrigioMoltoChiaro
)

private val SchemaScuroBlu = darkColorScheme(
    primary = Color(0xFF5B8DEF),
    onPrimary = Color(0xFF001B3D),
    primaryContainer = Color(0xFF1A3A6B),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF9DC0FF),
    onSecondary = Color(0xFF001B3D),
    secondaryContainer = Color(0xFF14315C),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = VerdeUber,
    onTertiary = Nero,
    tertiaryContainer = VerdeUberScuro,
    onTertiaryContainer = Bianco,
    error = Color(0xFFEF5350),
    onError = Nero,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Bianco,
    background = Color(0xFF0A1420),
    onBackground = Color(0xFFE6EEFF),
    surface = Color(0xFF0A1420),
    onSurface = Color(0xFFE6EEFF),
    surfaceVariant = Color(0xFF16263A),
    onSurfaceVariant = Color(0xFFB8C8E0),
    outline = Color(0xFF5B8DEF),
    outlineVariant = Color(0xFF1F3A5C)
)

private val SchemaChiaroArancio = lightColorScheme(
    primary = ArancioUber,
    onPrimary = Bianco,
    primaryContainer = Color(0xFFFFE0D6),
    onPrimaryContainer = Color(0xFF3D1600),
    secondary = GrigioScuro,
    onSecondary = Bianco,
    secondaryContainer = GrigioMoltoChiaro,
    onSecondaryContainer = Nero,
    tertiary = VerdeUber,
    onTertiary = Bianco,
    tertiaryContainer = Color(0xFFD4F5E2),
    onTertiaryContainer = Color(0xFF003820),
    error = Color(0xFFE53935),
    onError = Bianco,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Bianco,
    onBackground = Nero,
    surface = Bianco,
    onSurface = Nero,
    surfaceVariant = BiancoSporco,
    onSurfaceVariant = GrigioMedio,
    outline = GrigioChiaro,
    outlineVariant = GrigioMoltoChiaro
)

private val SchemaScuroArancio = darkColorScheme(
    primary = Color(0xFFFF8A50),
    onPrimary = Color(0xFF3D1600),
    primaryContainer = Color(0xFF6B2D00),
    onPrimaryContainer = Color(0xFFFFE0D6),
    secondary = Color(0xFFFFB68C),
    onSecondary = Color(0xFF3D1600),
    secondaryContainer = Color(0xFF54230A),
    onSecondaryContainer = Color(0xFFFFE0D6),
    tertiary = VerdeUber,
    onTertiary = Nero,
    tertiaryContainer = VerdeUberScuro,
    onTertiaryContainer = Bianco,
    error = Color(0xFFEF5350),
    onError = Nero,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Bianco,
    background = Color(0xFF1A0E06),
    onBackground = Color(0xFFFFEDE4),
    surface = Color(0xFF1A0E06),
    onSurface = Color(0xFFFFEDE4),
    surfaceVariant = Color(0xFF33200F),
    onSurfaceVariant = Color(0xFFE8C8B0),
    outline = Color(0xFFFF8A50),
    outlineVariant = Color(0xFF4A2E18)
)

private val SchemaChiaroViola = lightColorScheme(
    primary = ViolaUber,
    onPrimary = Bianco,
    primaryContainer = Color(0xFFE8D5FF),
    onPrimaryContainer = Color(0xFF1A003D),
    secondary = GrigioScuro,
    onSecondary = Bianco,
    secondaryContainer = GrigioMoltoChiaro,
    onSecondaryContainer = Nero,
    tertiary = VerdeUber,
    onTertiary = Bianco,
    tertiaryContainer = Color(0xFFD4F5E2),
    onTertiaryContainer = Color(0xFF003820),
    error = Color(0xFFE53935),
    onError = Bianco,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Bianco,
    onBackground = Nero,
    surface = Bianco,
    onSurface = Nero,
    surfaceVariant = BiancoSporco,
    onSurfaceVariant = GrigioMedio,
    outline = GrigioChiaro,
    outlineVariant = GrigioMoltoChiaro
)

private val SchemaScuroViola = darkColorScheme(
    primary = Color(0xFFC08CFF),
    onPrimary = Color(0xFF2A0050),
    primaryContainer = Color(0xFF3D1A6B),
    onPrimaryContainer = Color(0xFFE8D5FF),
    secondary = Color(0xFFD8B8FF),
    onSecondary = Color(0xFF2A0050),
    secondaryContainer = Color(0xFF2E1450),
    onSecondaryContainer = Color(0xFFE8D5FF),
    tertiary = VerdeUber,
    onTertiary = Nero,
    tertiaryContainer = VerdeUberScuro,
    onTertiaryContainer = Bianco,
    error = Color(0xFFEF5350),
    onError = Nero,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Bianco,
    background = Color(0xFF120A1A),
    onBackground = Color(0xFFF0E6FF),
    surface = Color(0xFF120A1A),
    onSurface = Color(0xFFF0E6FF),
    surfaceVariant = Color(0xFF26143A),
    onSurfaceVariant = Color(0xFFD0B8E8),
    outline = Color(0xFFC08CFF),
    outlineVariant = Color(0xFF3A2055)
)

private val SchemaChiaroRosso = lightColorScheme(
    primary = RossoUber,
    onPrimary = Bianco,
    primaryContainer = Color(0xFFFFD6D6),
    onPrimaryContainer = Color(0xFF3D0000),
    secondary = GrigioScuro,
    onSecondary = Bianco,
    secondaryContainer = GrigioMoltoChiaro,
    onSecondaryContainer = Nero,
    tertiary = VerdeUber,
    onTertiary = Bianco,
    tertiaryContainer = Color(0xFFD4F5E2),
    onTertiaryContainer = Color(0xFF003820),
    error = Color(0xFFE53935),
    onError = Bianco,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Bianco,
    onBackground = Nero,
    surface = Bianco,
    onSurface = Nero,
    surfaceVariant = BiancoSporco,
    onSurfaceVariant = GrigioMedio,
    outline = GrigioChiaro,
    outlineVariant = GrigioMoltoChiaro
)

private val SchemaScuroRosso = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    onPrimary = Color(0xFF3D0000),
    primaryContainer = Color(0xFF6B1A1A),
    onPrimaryContainer = Color(0xFFFFD6D6),
    secondary = Color(0xFFFF9E9E),
    onSecondary = Color(0xFF3D0000),
    secondaryContainer = Color(0xFF521414),
    onSecondaryContainer = Color(0xFFFFD6D6),
    tertiary = VerdeUber,
    onTertiary = Nero,
    tertiaryContainer = VerdeUberScuro,
    onTertiaryContainer = Bianco,
    error = Color(0xFFFF8A80),
    onError = Nero,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Bianco,
    background = Color(0xFF1A0808),
    onBackground = Color(0xFFFFE8E8),
    surface = Color(0xFF1A0808),
    onSurface = Color(0xFFFFE8E8),
    surfaceVariant = Color(0xFF331212),
    onSurfaceVariant = Color(0xFFE8B8B8),
    outline = Color(0xFFFF6B6B),
    outlineVariant = Color(0xFF4A1A1A)
)

// Tipografia Uber — pulita e moderna
private val TipografiaUber = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold, lineHeight = 64.sp, letterSpacing = -0.25.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)


// Tipografia Google Pixel — geometrica e pulita
private val TipografiaPixel = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold, lineHeight = 64.sp, letterSpacing = -0.25.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

// Tipografia iOS — San Francisco style
private val TipografiaIOS = Typography(
    displayLarge = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Bold, lineHeight = 68.sp, letterSpacing = -0.5.sp),
    displayMedium = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, lineHeight = 56.sp),
    displaySmall = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, lineHeight = 46.sp),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.SemiBold, lineHeight = 38.sp),
    headlineSmall = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp),
    titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, letterSpacing = 0.5.sp)
)

// Tipografia Material You — arrotondata e amichevole
private val TipografiaMaterial = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold, lineHeight = 64.sp, letterSpacing = -0.25.sp),
    displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

// Funzione per selezionare la tipografia in base allo stile
private fun tipografiaPerStile(stile: StileDesign): Typography = when (stile) {
    StileDesign.PIXEL -> TipografiaPixel
    StileDesign.IOS -> TipografiaIOS
    StileDesign.MATERIAL -> TipografiaMaterial
    StileDesign.UBER -> TipografiaUber
}

// Raggi per ogni stile design
object RaggiPixel {
    val card = 16.dp
    val chip = 8.dp
    val bottone = 16.dp
}

object RaggiIOS {
    val card = 20.dp
    val chip = 10.dp
    val bottone = 14.dp
}

object RaggiMaterial {
    val card = 28.dp
    val chip = 12.dp
    val bottone = 20.dp
}

object RaggiUber {
    val card = 12.dp
    val chip = 8.dp
    val bottone = 12.dp
}

// Funzione per selezionare i raggi in base allo stile
fun raggiPerStile(stile: StileDesign) = when (stile) {
    StileDesign.PIXEL -> RaggiPixel
    StileDesign.IOS -> RaggiIOS
    StileDesign.MATERIAL -> RaggiMaterial
    StileDesign.UBER -> RaggiUber
}

// Spaziature per ogni stile design
object SpaziaturePixel {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object SpaziatureIOS {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 20.dp
    val xl = 28.dp
    val xxl = 40.dp
}

object SpaziatureMaterial {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object SpaziatureUber {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

fun spaziaturePerStile(stile: StileDesign) = when (stile) {
    StileDesign.PIXEL -> SpaziaturePixel
    StileDesign.IOS -> SpaziatureIOS
    StileDesign.MATERIAL -> SpaziatureMaterial
    StileDesign.UBER -> SpaziatureUber
}

enum class PaletteApp(val id: String, val nome: String) {
    UBER("uber", "Uber (Nero)"),
    BLU("blu", "Blu"),
    ARANCIO("arancio", "Arancio"),
    VIOLA("viola", "Viola"),
    ROSSO("rosso", "Rosso");

    companion object {
        fun daId(id: String): PaletteApp = entries.firstOrNull { it.id == id } ?: UBER
    }
}

@Composable
fun TedescoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    palette: PaletteApp = PaletteApp.UBER,
    stileDesign: StileDesign = StileDesign.UBER,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> when (palette) {
            PaletteApp.UBER -> SchemaScuro
            PaletteApp.BLU -> SchemaScuroBlu
            PaletteApp.ARANCIO -> SchemaScuroArancio
            PaletteApp.VIOLA -> SchemaScuroViola
            PaletteApp.ROSSO -> SchemaScuroRosso
        }
        else -> when (palette) {
            PaletteApp.UBER -> SchemaChiaro
            PaletteApp.BLU -> SchemaChiaroBlu
            PaletteApp.ARANCIO -> SchemaChiaroArancio
            PaletteApp.VIOLA -> SchemaChiaroViola
            PaletteApp.ROSSO -> SchemaChiaroRosso
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = tipografiaPerStile(stileDesign),
        content = content
    )
}

// Spaziature default (Uber) — retrocompatibilità
object Spaziature {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

// Raggi default (Uber) — retrocompatibilità
object Raggi {
    val card = 12.dp
    val chip = 8.dp
    val bottone = 12.dp
}

// Per usare i valori dinamici, importa raggiPerStile() e spaziaturePerStile()

// Breakpoints
object Breakpoints {
    const val COMPACT = 0
    const val MEDIUM = 600
    const val EXPANDED = 840
    const val LARGE = 1200
}

// Classi dimensione finestra
enum class WindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}

// Larghezza minima finestra
private const val LARGHEZZA_MINIMA_DP = 320

@Composable
fun rememberWindowSizeClass(): WindowSize {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.coerceAtLeast(LARGHEZZA_MINIMA_DP)
    return androidx.compose.runtime.remember(widthDp) {
        when {
            widthDp < 600 -> WindowSize.COMPACT
            widthDp < 840 -> WindowSize.MEDIUM
            else -> WindowSize.EXPANDED
        }
    }
}

@Composable
fun dimensioneContenuto(): androidx.compose.ui.unit.Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> androidx.compose.ui.unit.Dp.Unspecified
        WindowSize.MEDIUM -> 600.dp
        WindowSize.EXPANDED -> 720.dp
    }
}

@Composable
fun spaziaturaSchermo(): androidx.compose.ui.unit.Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
}

@Composable
fun responsiveVerticalSpacing(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16
        WindowSize.MEDIUM -> 20
        WindowSize.EXPANDED -> 24
    }
}

@Composable
fun responsiveGridColumns(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 1
        WindowSize.MEDIUM -> 2
        WindowSize.EXPANDED -> 3
    }
}

@Composable
fun responsiveToolbarHeight(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 56
        WindowSize.MEDIUM -> 64
        WindowSize.EXPANDED -> 72
    }
}

@Composable
fun responsiveButtonHeight(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 48
        WindowSize.MEDIUM -> 52
        WindowSize.EXPANDED -> 56
    }
}

@Composable
fun responsiveCornerRadius(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 8
        WindowSize.MEDIUM -> 12
        WindowSize.EXPANDED -> 16
    }
}

@Composable
fun responsiveTypography(stileDesign: StileDesign = StileDesign.UBER): Typography = tipografiaPerStile(stileDesign)

@Composable
fun responsiveColumnArrangement(): androidx.compose.foundation.layout.Arrangement.Vertical {
    return androidx.compose.foundation.layout.Arrangement.spacedBy(responsiveVerticalSpacing().dp)
}

@Composable
fun responsiveRowArrangement(): androidx.compose.foundation.layout.Arrangement.Horizontal {
    val spacing = when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 8
        WindowSize.MEDIUM -> 12
        WindowSize.EXPANDED -> 16
    }
    return androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.dp)
}

@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val arrangement = responsiveColumnArrangement()
    val horizontalPadding = when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxSize(),
        verticalArrangement = arrangement,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val arrangement = responsiveRowArrangement()
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun AltezzaBottonePrincipale(): androidx.compose.ui.unit.Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 56.dp
        else -> 60.dp
    }
}

@Composable
fun TitoloSchermata(testo: String) {
    androidx.compose.material3.Text(
        text = testo,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}
