package cc.rhuan.receitas.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Documented Samsung values and project adaptations are distinguished in DESIGN.md. */
object DesignTokens {
    val margin = 24.dp
    val small = 8.dp
    val gap = 12.dp
    val padding = 16.dp
    val section = 24.dp
    val viewingPadding = 24.dp
    val compactHeight = 600.dp
    const val compactFontScale = 1.5f
    val control = 48.dp
    val search = 56.dp
    val icon = 24.dp
    val cardRadius = 24.dp
    val fieldRadius = 28.dp
    val buttonRadius = 18.dp
    val hairline = 1.dp
    val tight = 4.dp
}

private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontFamily = FontFamily.Default, fontSize = size.sp, lineHeight = height.sp, fontWeight = weight)

val DesignTypography = Typography(
    headlineLarge = type(36, 44, FontWeight.Bold),
    headlineMedium = type(28, 36, FontWeight.Bold),
    headlineSmall = type(24, 32, FontWeight.SemiBold),
    titleLarge = type(22, 28, FontWeight.SemiBold),
    titleMedium = type(18, 26, FontWeight.SemiBold),
    titleSmall = type(16, 24, FontWeight.SemiBold),
    bodyLarge = type(16, 24), bodyMedium = type(14, 20), bodySmall = type(12, 18),
    labelLarge = type(14, 20, FontWeight.SemiBold),
    labelMedium = type(12, 18, FontWeight.Medium), labelSmall = type(12, 18, FontWeight.Medium),
)

internal val DesignLight = lightColorScheme(
    primary = Color(0xFF0072DE), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FF), onPrimaryContainer = Color(0xFF123E86),
    background = Color(0xFFF2F2F2), onBackground = Color(0xFF171717),
    surface = Color.White, onSurface = Color(0xFF171717),
    surfaceVariant = Color(0xFFEDEDED), onSurfaceVariant = Color(0xFF595959),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainer = Color(0xFFEDEDED), surfaceContainerHigh = Color(0xFFE7E7E7),
    surfaceContainerHighest = Color(0xFFDFDFDF),
    secondary = Color(0xFF595959), secondaryContainer = Color(0xFFEDEDED), onSecondaryContainer = Color(0xFF171717),
    tertiary = Color(0xFF267465), tertiaryContainer = Color(0xFFE4F4EE), onTertiaryContainer = Color(0xFF185343),
    outline = Color(0xFF757575), outlineVariant = Color(0xFFDADADA),
    error = Color(0xFFB3261E), errorContainer = Color(0xFFFFEDEA), onErrorContainer = Color(0xFF8C1D18),
)
internal val DesignDark = darkColorScheme(
    primary = Color(0xFF3E91FF), onPrimary = Color(0xFF001A3D),
    primaryContainer = Color(0xFF1B355C), onPrimaryContainer = Color(0xFFDCE8FF),
    background = Color(0xFF000000), onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1C1C1E), onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF2C2C2E), onSurfaceVariant = Color(0xFFBDBDBD),
    surfaceContainerLowest = Color(0xFF000000), surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF2C2C2E), surfaceContainerHigh = Color(0xFF343436),
    surfaceContainerHighest = Color(0xFF414143),
    secondary = Color(0xFFBDBDBD), secondaryContainer = Color(0xFF2C2C2E), onSecondaryContainer = Color(0xFFF5F5F5),
    tertiary = Color(0xFF95D4BE), tertiaryContainer = Color(0xFF205344), onTertiaryContainer = Color(0xFFB2F0D8),
    outline = Color(0xFF949494), outlineVariant = Color(0xFF414143),
    error = Color(0xFFFFB4AB), errorContainer = Color(0xFF522521), onErrorContainer = Color(0xFFFFDAD6),
)

/** Also supports isolated previews by following their host theme selection. */
@Composable
fun DesignTheme(content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    ReceitasTheme(darkTheme = dark, content = content)
}
