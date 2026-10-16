package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** A minimum, rather than fixed, height preserves scrolling for large text. */
@Composable
fun CenteredListGuidance(occupiedHeight: Dp = 0.dp, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = (LocalRecordListHeight.current - occupiedHeight).coerceAtLeast(0.dp))
        .padding(horizontal = T.margin), contentAlignment = Alignment.Center) { content() }
}

/** Shared, open composition for an empty collection, rather than a record card. */
@Composable
fun EmptyCollectionState(title: String, message: String, icon: @Composable () -> Unit,
    modifier: Modifier = Modifier, actions: (@Composable ColumnScope.() -> Unit)? = null) {
        Column(modifier.widthIn(max = T.guidanceMaxWidth).fillMaxWidth()
            .padding(horizontal = T.small, vertical = T.section),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(T.section)) {
            icon()
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(T.padding)) {
                Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center)
                Text(message, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            if (actions != null) Column(Modifier.widthIn(max = T.guidanceActionMaxWidth).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(T.gap), content = actions)
        }
}

/** Guidance actions use the theme accent, with an outlined secondary alternative. */
@Composable
fun GuidanceActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    outlined: Boolean = false) {
    if (outlined) OutlinedButton(onClick, modifier.heightIn(min = T.control), shape = CircleShape) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    } else Button(onClick, modifier.heightIn(min = T.control), shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
