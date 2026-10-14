package cc.rhuan.receitas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Semantic colors for due dates; do not depend on medication type. */
object AppPalette {
 val warning: Color @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFC582) else Color(0xFF8B500B)
 val warningContainer: Color @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF49331C) else Color(0xFFFFF0DE)
}
