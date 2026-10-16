package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import cc.rhuan.receitas.ui.theme.DesignTokens as T

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchField(value: String, onValueChange: (String) -> Unit, label: String, shortLabel: String,
    clearLabel: String, modifier: Modifier = Modifier, fieldTag: String = "record-search") {
    val visibleLabel = if (LocalDensity.current.fontScale >= T.compactFontScale) shortLabel else label
    val interaction = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
    BasicTextField(value, onValueChange, modifier.heightIn(min = T.control).testTag(fieldTag)
        .semantics { contentDescription = label }, singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), interactionSource = interaction,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(value = value, innerTextField = innerTextField,
                enabled = true, singleLine = true, visualTransformation = VisualTransformation.None,
                interactionSource = interaction, colors = colors,
                contentPadding = PaddingValues(T.small),
                placeholder = { Text(visibleLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = if (value.isNotEmpty()) ({ IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Rounded.Clear, clearLabel)
                } }) else null,
                container = { OutlinedTextFieldDefaults.Container(true, false, interaction, colors = colors,
                    shape = RoundedCornerShape(T.fieldRadius)) })
        })
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
fun PageHeader(title: String, subtitle: String, expanded: Boolean,
    actions: @Composable RowScope.() -> Unit = {}) {
    Column(Modifier.fillMaxWidth().testTag(if (expanded) "page-header" else "page-app-bar").padding(horizontal = T.margin)
        .padding(vertical = T.padding), horizontalAlignment = Alignment.Start) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f).semantics { heading() },
                style = if (expanded) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge)
            actions()
        }
        if (expanded && subtitle.isNotBlank()) Text(subtitle, Modifier.padding(top = T.small),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start)
    }
}

@Composable
fun SectionHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = T.small, bottom = T.small).semantics { heading() })
}

@Composable
fun EmptyListGuidance(title: String, message: String, actionLabel: String?, onAction: () -> Unit,
    @DrawableRes iconRes: Int, modifier: Modifier = Modifier) {
    EmptyCollectionState(title, message, icon = {
        Icon(painterResource(iconRes), null, Modifier.size(T.padding * 2),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }, modifier = modifier, actions = if (actionLabel != null) ({
        GuidanceActionButton(actionLabel, onAction, Modifier.fillMaxWidth().testTag("create-record")
            .semantics { contentDescription = actionLabel })
    }) else null)
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
