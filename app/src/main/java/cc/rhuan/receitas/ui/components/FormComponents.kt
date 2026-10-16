package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Separates records in a multi-medication form without enclosing fields in cards. */
@Composable
fun FormRecordSection(title: String, modifier: Modifier = Modifier, subtitle: String = "", divider: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.padding)) {
        if (divider) {
            Spacer(Modifier.height(T.padding))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(T.padding))
        }
        DetailIdentityHeader(title) {}
        if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
fun FormField(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "",
    singleLine: Boolean = true, error: String? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    required: Boolean = false, showLabel: Boolean = true) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.tight)) {
        if (showLabel) FormFieldLabel(label, required)
        TextField(value, onChange, modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            placeholder = { Text(placeholder) }, singleLine = singleLine, minLines = if (singleLine) 1 else 2,
            shape = RoundedCornerShape(T.buttonRadius),
            keyboardOptions = keyboardOptions, isError = error != null,
            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent),
            supportingText = if (error != null) ({ Text(error, Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }) else null)
    }
}

@Composable
private fun FormFieldLabel(label: String, required: Boolean, modifier: Modifier = Modifier) {
    val requiredDescription = stringResource(R.string.form_required)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(T.tight)) {
        Text(label, Modifier.weight(1f, fill = false), style = if (required) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (required) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        if (required) Icon(Icons.Rounded.Info, requiredDescription, Modifier.size(T.icon),
            tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun FormToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = T.control).toggleable(checked, role = Role.Switch, onValueChange = onChange)
        .padding(vertical = T.small), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(T.gap))
        Switch(checked, onCheckedChange = null)
    }
}

data class FormChoiceOption(val title: String, val description: String)

/** Compact record identity inside a grouped registration list. */
@Composable
fun FormRecordRow(title: String, subtitle: String, onEdit: () -> Unit, actions: List<ContextAction>) {
    val identity: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(T.tight)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    val controls: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(T.small)) {
            ReviewEditButton(title, onEdit)
            OverflowActionsButton(title, actions)
        }
    }
    if (LocalDensity.current.fontScale >= T.compactFontScale) {
        Column(Modifier.fillMaxWidth().padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.small)) {
            identity()
            controls()
        }
    } else Row(Modifier.fillMaxWidth().padding(T.padding), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(T.gap)) {
        Box(Modifier.weight(1f)) { identity() }
        controls()
    }
}

/** Optional information opens a separate flow; the row only displays its summary. */
@Composable
fun OptionalDetailRow(label: String, summary: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val actionLabel = stringResource(if (summary.isBlank()) R.string.optional_detail_add else R.string.action_edit)
    Column(modifier.fillMaxWidth().padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.small)) {
        val identity: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(T.tight)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                if (summary.isNotBlank()) Text(summary, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val action: @Composable () -> Unit = {
            FilledTonalButton(onClick = onClick, modifier = Modifier.heightIn(min = T.control)
                .semantics { contentDescription = "$actionLabel $label" }) { Text(actionLabel) }
        }
        if (LocalDensity.current.fontScale >= T.compactFontScale) {
            identity()
            action()
        } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(T.gap)) {
            Box(Modifier.weight(1f)) { identity() }
            action()
        }
    }
}
/** Tonal choices; the entire row is a radio-button touch target. */
@Composable
fun RadioChoiceList(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(T.gap)) {
        options.forEachIndexed { index, label ->
            Surface(shape = RoundedCornerShape(T.buttonRadius), color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(Modifier.fillMaxWidth().heightIn(min = T.control)
                    .selectable(selectedIndex == index, role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(horizontal = T.padding, vertical = T.small), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(T.gap)) {
                    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    RadioButton(selectedIndex == index, onClick = null, modifier = Modifier.size(T.icon))
                }
            }
        }
    }
}

@Composable
fun FormChoiceField(label: String, selected: String, onClick: () -> Unit, supportingText: String? = null, required: Boolean = false) {
    val stacked = LocalDensity.current.fontScale >= T.compactFontScale
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.small)) {
        Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = T.control),
            shape = RoundedCornerShape(T.buttonRadius), color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(Modifier.padding(T.padding), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(T.gap)) {
                if (stacked) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(T.tight)) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text(selected, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text(label, Modifier.weight(1.4f), style = MaterialTheme.typography.bodyMedium)
                    Text(selected, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        supportingText?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun FormChoiceCards(options: List<FormChoiceOption>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(T.small)) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            Surface(shape = RoundedCornerShape(T.buttonRadius),
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface) {
                Row(Modifier.fillMaxWidth().heightIn(min = T.control)
                    .testTag("form-choice-option-$index")
                    .selectable(isSelected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(T.padding), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(T.tight)) {
                        Text(option.title, style = MaterialTheme.typography.titleSmall)
                        Text(option.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(T.small))
                    RadioButton(selected = isSelected, onClick = null, modifier = Modifier.size(T.icon),
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}
