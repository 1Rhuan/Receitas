package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

@Composable
fun ReviewEditButton(label: String, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onEdit, modifier.size(T.control)) {
        Icon(Icons.Rounded.Edit, stringResource(R.string.review_edit_field, label))
    }
}

/** An open review section; individual record cards remain inside its content. */
@Composable
fun ReviewSection(title: String, onEdit: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.padding)) {
        ReviewSectionHeading(title, onEdit)
        content()
    }
}

@Composable
fun ReviewSectionHeading(title: String, onEdit: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(T.small)) {
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleMedium)
        if (onEdit != null) ReviewEditButton(title, onEdit)
    }
}

@Composable
fun ReviewRecordCard(title: String, subtitle: String, modifier: Modifier = Modifier, onEdit: (() -> Unit)? = null) {
    ContentCard(modifier) {
        DetailIdentityHeader(title) {
            if (onEdit != null) ReviewEditButton(stringResource(R.string.review_medication_edit_label, title), onEdit)
        }
        if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyLarge)
    }
}

