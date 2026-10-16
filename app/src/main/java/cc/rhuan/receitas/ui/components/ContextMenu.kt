package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Rounded secondary-action menu shared by sorting and event actions. */
@Composable
fun ContextMenu(expanded: Boolean, onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit) {
    val contentDensity = LocalDensity.current
    DropdownMenu(expanded, onDismissRequest,
        modifier = Modifier.widthIn(min = T.menuMinWidth, max = T.menuMaxWidth),
        shape = RoundedCornerShape(T.menuRadius),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        content = { CompositionLocalProvider(LocalDensity provides contentDensity) { content() } })
}
