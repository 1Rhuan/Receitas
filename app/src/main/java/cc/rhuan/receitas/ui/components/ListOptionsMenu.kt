package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

data class ListOptionGroup(val title: String, val options: List<String>, val selectedIndex: Int)

/** Compact summary with draft choices; only Done applies changes. */
@Composable
fun ListOptionsMenu(groups: List<ListOptionGroup>, label: String,
    onApply: (List<Int>) -> Unit, modifier: Modifier = Modifier, iconOnly: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(groups.map { it.selectedIndex }) }
    val summary = groups.joinToString(" | ") { it.options[it.selectedIndex] }
    Box(modifier) {
        if (iconOnly) IconButton(onClick = { draft = groups.map { it.selectedIndex }; expanded = true },
            modifier = Modifier.size(T.control).semantics { stateDescription = summary }) {
            Icon(Icons.Rounded.Tune, label,
                tint = if (groups.any { it.selectedIndex != 0 }) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        } else TextButton(onClick = { draft = groups.map { it.selectedIndex }; expanded = true },
            modifier = Modifier.heightIn(min = T.control).semantics { contentDescription = label; stateDescription = summary }) {
            Text(summary,
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(T.tight))
            Icon(Icons.Rounded.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface)
        }
        ContextMenu(expanded, { expanded = false }) {
            Column(Modifier.width(T.menuMaxWidth).heightIn(max = T.optionsMenuMaxHeight)
                .padding(horizontal = T.padding)) {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                    .testTag("list-options-scroll")) {
                    groups.forEachIndexed { groupIndex, group ->
                        Text(group.title, Modifier.padding(vertical = T.small).semantics { heading() },
                            style = MaterialTheme.typography.titleSmall)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(Modifier.selectableGroup()) {
                            group.options.forEachIndexed { index, option ->
                                Row(Modifier.fillMaxWidth().heightIn(min = T.control)
                                    .testTag("list-option-$groupIndex-$index")
                                    .selectable(draft[groupIndex] == index, role = Role.RadioButton,
                                        onClick = { draft = draft.toMutableList().also { it[groupIndex] = index } })
                                    .padding(vertical = T.tight),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(T.small)) {
                                    RadioButton(draft[groupIndex] == index, onClick = null)
                                    Text(option, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton({ expanded = false }, Modifier.weight(1f).heightIn(min = T.control)) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton({ expanded = false; onApply(draft.toList()) }, Modifier.weight(1f).heightIn(min = T.control)) {
                        Text(stringResource(R.string.list_options_done))
                    }
                }
            }
        }
    }
}
