package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import cc.rhuan.receitas.ui.theme.DesignTokens as T

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, label: String, shortLabel: String,
    clearLabel: String, modifier: Modifier = Modifier) {
    val visibleLabel = if (LocalDensity.current.fontScale >= T.compactFontScale) shortLabel else label
    OutlinedTextField(value, onValueChange, modifier.heightIn(min = T.search).testTag("record-search"),
        label = { Text(visibleLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { if (value.isNotEmpty()) IconButton(onClick = { onValueChange("") }) {
            Icon(Icons.Default.Clear, clearLabel)
        } }, shape = RoundedCornerShape(T.fieldRadius), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
}

@Composable
fun ActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.heightIn(min = T.control), enabled = enabled, shape = RoundedCornerShape(T.buttonRadius)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick, modifier.heightIn(min = T.control), enabled = enabled, shape = RoundedCornerShape(T.buttonRadius)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FilterPill(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(selected, onClick, modifier = Modifier.heightIn(min = T.control),
        label = { Text(label) }, shape = RoundedCornerShape(T.fieldRadius),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
}

@Composable
fun PageHeader(title: String, subtitle: String, expanded: Boolean) {
    Column(Modifier.fillMaxWidth().testTag(if (expanded) "page-header" else "page-app-bar").padding(horizontal = T.margin)
        .padding(vertical = if (expanded) T.viewingPadding else T.padding),
        horizontalAlignment = if (expanded) Alignment.CenterHorizontally else Alignment.Start) {
        Text(title, Modifier.semantics { heading() }, style = if (expanded) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
            textAlign = if (expanded) TextAlign.Center else TextAlign.Start)
        if (expanded && subtitle.isNotBlank()) Text(subtitle, Modifier.padding(top = T.small),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun SectionHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = T.small, bottom = T.small).semantics { heading() })
}

@Composable
fun EmptyStateCard(title: String, message: String, actionLabel: String?, onAction: () -> Unit,
    modifier: Modifier = Modifier) {
    ContentCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = T.section),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(T.padding)) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            if (actionLabel != null) ActionButton(actionLabel, onAction)
        }
    }
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    AppCard(modifier.fillMaxWidth(), shape = RoundedCornerShape(T.cardRadius)) {
        Column(Modifier.padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.small), content = content)
    }
}

@Composable
fun StatusLabel(label: String, attention: Boolean = false) {
    Surface(shape = RoundedCornerShape(T.fieldRadius),
        color = if (attention) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer) {
        Text(label, Modifier.padding(horizontal = T.gap, vertical = T.small),
            style = MaterialTheme.typography.labelMedium,
            color = if (attention) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
