package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Select removal intent; callers retain their existing operation confirmations. */
@Composable
fun RemovalChoiceDialog(title: String, canArchive: Boolean, archiveDescription: String,
    deleteDescription: String, onArchive: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    ActionSheet(title, onDismiss, actions = {
        GuidanceActionButton(stringResource(R.string.action_cancel), onDismiss, Modifier.fillMaxWidth(), outlined = true)
    }) {
        if (canArchive) {
            SheetActionButton(stringResource(R.string.action_archive), onArchive)
            Text(archiveDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(T.small))
        }
        SheetActionButton(stringResource(R.string.ui_excluir_definitivamente), onDelete, destructive = true)
        Text(deleteDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
