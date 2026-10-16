package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Native modal bottom sheet: anchored below, with content that grows and scrolls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSheet(title: String, onDismiss: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit, showClose: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    val contentDensity = LocalDensity.current
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("action-sheet"), dragHandle = null,
        shape = RoundedCornerShape(topStart = T.cardRadius, topEnd = T.cardRadius),
        containerColor = MaterialTheme.colorScheme.surface) {
        CompositionLocalProvider(LocalDensity provides contentDensity) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(T.section),
            verticalArrangement = Arrangement.spacedBy(T.section)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
                if (showClose) IconButton(onDismiss, Modifier.size(T.control)) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.action_cancel))
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.small), content = content)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.small), content = actions)
        }
        }
    }
}

@Composable
fun SheetActionButton(label: String, onClick: () -> Unit, destructive: Boolean = false) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = T.control), shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.onSurface,
            contentColor = if (destructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.surface)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ConfirmationSheet(title: String, message: String, confirmLabel: String,
    onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = false,
    dismissLabel: String = stringResource(R.string.action_cancel)) {
    ActionSheet(title, onDismiss, actions = {
        SheetActionButton(confirmLabel, onConfirm, destructive)
        GuidanceActionButton(dismissLabel, onDismiss, Modifier.fillMaxWidth(), outlined = true)
    }) { Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
