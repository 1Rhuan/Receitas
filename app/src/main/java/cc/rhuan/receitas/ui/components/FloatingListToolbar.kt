package cc.rhuan.receitas.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

data class RecordListTools(
    val query: String, val onQueryChange: (String) -> Unit,
    val searchLabel: String, val shortSearchLabel: String, val clearSearchLabel: String,
    val optionGroups: List<ListOptionGroup>, val optionsLabel: String,
    val onOptionsApply: (List<Int>) -> Unit,
)

/** Overlay keeps list controls available without an opaque full-width app bar. */
@Composable
fun FloatingListToolbar(visible: Boolean, tools: RecordListTools, onBack: (() -> Unit)? = null) {
    var searching by rememberSaveable { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    fun closeSearch() { searching = false; keyboard?.hide() }
    BackHandler(searching) { closeSearch() }
    if (visible || searching) {
        Surface(Modifier.padding(horizontal = T.margin, vertical = T.small)
            .testTag("floating-list-toolbar"), shape = RoundedCornerShape(T.fieldRadius),
            color = MaterialTheme.colorScheme.surfaceContainer, shadowElevation = T.small) {
            Row(Modifier.padding(T.tight)) {
                if (searching) {
                    SearchField(tools.query, tools.onQueryChange, tools.searchLabel,
                        tools.shortSearchLabel, tools.clearSearchLabel,
                        Modifier.weight(1f).focusRequester(focus), fieldTag = "floating-list-search")
                    IconButton({ closeSearch() }, Modifier.size(T.control)) {
                        Icon(Icons.Rounded.Close, stringResource(R.string.close_list_search))
                    }
                } else {
                    if (onBack != null) IconButton(onBack, Modifier.size(T.control)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))
                    }
                    IconButton({ searching = true }, Modifier.size(T.control)) {
                        Icon(Icons.Rounded.Search, tools.searchLabel)
                    }
                    ListOptionsMenu(tools.optionGroups, tools.optionsLabel, tools.onOptionsApply, iconOnly = true)
                }
            }
        }
    }
    LaunchedEffect(searching) { if (searching) { focus.requestFocus(); keyboard?.show() } }
}
