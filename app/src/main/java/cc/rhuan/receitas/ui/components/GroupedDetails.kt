package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

@Composable
fun GroupedSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(T.small)) {
        SectionHeading(title)
        Surface(shape = RoundedCornerShape(T.cardRadius), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
fun GroupedValueRow(label: String, value: String, divider: Boolean = true) {
    if (divider) HorizontalDivider(Modifier.padding(horizontal = T.padding), color = MaterialTheme.colorScheme.outlineVariant)
    if (LocalDensity.current.fontScale >= T.compactFontScale) {
        Column(Modifier.fillMaxWidth().padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.tight)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        Row(Modifier.fillMaxWidth().padding(T.padding), horizontalArrangement = Arrangement.spacedBy(T.padding),
            verticalAlignment = Alignment.Top) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
        }
    }
}

data class ContextAction(val label: String, val destructive: Boolean = false, val onClick: () -> Unit)

/** Visible button opens secondary actions; destructive actions keep their existing confirmation. */
@Composable
fun ContextActionsButton(actions: List<ContextAction>, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.heightIn(min = T.control),
            shape = RoundedCornerShape(T.buttonRadius)) {
            Text(stringResource(R.string.design_context_actions), style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(T.icon))
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            actions.forEachIndexed { index, action ->
                if (action.destructive && index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DropdownMenuItem(text = {
                    Text(action.label, color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }, onClick = { expanded = false; action.onClick() })
            }
        }
    }
}
