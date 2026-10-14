package cc.rhuan.receitas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
 primary = Color(0xFF4263D5), onPrimary = Color.White,
 primaryContainer = Color(0xFFE8EDFF), onPrimaryContainer = Color(0xFF253D91),
 secondary = Color(0xFF526580), onSecondary = Color.White,
 secondaryContainer = Color(0xFFEDF1F8), onSecondaryContainer = Color(0xFF32445D),
 tertiary = Color(0xFF267465), onTertiary = Color.White,
 tertiaryContainer = Color(0xFFE4F4EE), onTertiaryContainer = Color(0xFF185343),
 background = Color(0xFFF6F6F8), onBackground = Color(0xFF282B33),
 surface = Color.White, onSurface = Color(0xFF282B33),
 surfaceVariant = Color(0xFFF0F1F5), onSurfaceVariant = Color(0xFF646A78),
 surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFAFAFC),
 surfaceContainer = Color(0xFFF0F1F5), surfaceContainerHigh = Color(0xFFE9EBF0),
 surfaceContainerHighest = Color(0xFFE2E5EC),
 outline = Color(0xFF858C9B), outlineVariant = Color(0xFFDEE1E8),
 error = Color(0xFFB83249), onError = Color.White,
 errorContainer = Color(0xFFFFE9ED), onErrorContainer = Color(0xFF882239),
 inverseSurface = Color(0xFF282B33), inverseOnSurface = Color(0xFFF6F6F8),
)
private val DarkColorScheme = darkColorScheme(
 primary = Color(0xFFB4C3FF), onPrimary = Color(0xFF162B73),
 primaryContainer = Color(0xFF2D448E), onPrimaryContainer = Color(0xFFDFE5FF),
 secondary = Color(0xFFBDC9DE), onSecondary = Color(0xFF27374E),
 secondaryContainer = Color(0xFF35445C), onSecondaryContainer = Color(0xFFDEE7F8),
 tertiary = Color(0xFF95D4BE), onTertiary = Color(0xFF073A2D),
 tertiaryContainer = Color(0xFF205344), onTertiaryContainer = Color(0xFFB2F0D8),
 background = Color(0xFF14161C), onBackground = Color(0xFFE6E7ED),
 surface = Color(0xFF1E2129), onSurface = Color(0xFFE6E7ED),
 surfaceVariant = Color(0xFF2A2E39), onSurfaceVariant = Color(0xFFBBC0CD),
 surfaceContainerLowest = Color(0xFF101218), surfaceContainerLow = Color(0xFF1A1D24),
 surfaceContainer = Color(0xFF242731), surfaceContainerHigh = Color(0xFF2D313C),
 surfaceContainerHighest = Color(0xFF383D49),
 outline = Color(0xFF9198A8), outlineVariant = Color(0xFF414753),
 error = Color(0xFFFFB2C0), onError = Color(0xFF640B24),
 errorContainer = Color(0xFF792139), onErrorContainer = Color(0xFFFFD9E1),
 inverseSurface = Color(0xFFE6E7ED), inverseOnSurface = Color(0xFF282B33),
)

@Composable
fun ReceitasTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false, content: @Composable () -> Unit) {
 val scheme = when {
  dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
   val context = LocalContext.current
   if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
  }
  darkTheme -> DarkColorScheme
  else -> LightColorScheme
 }
 MaterialTheme(colorScheme = scheme, typography = Typography,
  shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp)), content = content)
}
