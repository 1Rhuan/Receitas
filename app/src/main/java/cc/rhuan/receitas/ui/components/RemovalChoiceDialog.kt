package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(T.small)) {
            if (canArchive) {
                SecondaryActionButton(stringResource(R.string.action_archive), onArchive, Modifier.fillMaxWidth())
                Text(archiveDescription, style = MaterialTheme.typography.bodySmall)
            }
            SecondaryActionButton(stringResource(R.string.ui_excluir_definitivamente), onDelete, Modifier.fillMaxWidth())
            Text(deleteDescription, style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } })
}
