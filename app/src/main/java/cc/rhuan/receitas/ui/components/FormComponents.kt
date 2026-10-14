package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import cc.rhuan.receitas.ui.theme.DesignTokens as T

@Composable
fun FormField(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "",
    singleLine: Boolean = true, error: String? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.tight)) {
        OutlinedTextField(value, onChange, modifier = Modifier.fillMaxWidth(),
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text(placeholder) }, singleLine = singleLine, minLines = if (singleLine) 1 else 2,
            shape = MaterialTheme.shapes.medium, keyboardOptions = keyboardOptions, isError = error != null,
            supportingText = if (error != null) ({ Text(error, Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }) else null)
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

@Composable
fun OptionalFormSection(title: String, initiallyExpanded: Boolean = false, summary: String? = null, content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    ContentCard {
        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Text(if (expanded) "−" else "+")
        }
        summary?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (expanded) content()
    }
}

@Composable
fun FormChoice(label: String, selected: String, options: List<String>, onSelect: (Int) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = T.control),
        shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(T.padding)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.tight)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(selected, style = MaterialTheme.typography.bodyLarge)
        }
    }
    if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text(label) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { index, option ->
                    TextButton(onClick = { onSelect(index); open = false }, modifier = Modifier.fillMaxWidth().heightIn(min = T.control)) {
                        Text(option, Modifier.weight(1f), color = if (option == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }, confirmButton = {}, dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.action_cancel)) } })
}
