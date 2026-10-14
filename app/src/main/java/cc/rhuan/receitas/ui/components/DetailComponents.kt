package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Content-sized app bar so enlarged titles never depend on a fixed toolbar height. */
@Composable
fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = T.padding, vertical = T.small),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack, Modifier.size(T.control)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
        }
        Text(title, Modifier.weight(1f).padding(start = T.small).semantics { heading() },
            style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun DetailValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(T.tight)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Contextual actions accompany the record identity; enlarged text places them above the name. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailIdentityHeader(title: String, actions: @Composable RowScope.() -> Unit) {
    if (LocalDensity.current.fontScale >= T.compactFontScale) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.small)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, content = actions)
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        }
    } else FlowRow(Modifier.fillMaxWidth(), maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(T.small), verticalArrangement = Arrangement.spacedBy(T.small)) {
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(T.small), content = actions)
    }
}

@Composable
fun FormSection(title: String, onEdit: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    ContentCard {
        SectionHeading(title)
        if (onEdit != null) TextButton(onEdit) { Text(stringResource(R.string.action_edit)) }
        content()
    }
}
