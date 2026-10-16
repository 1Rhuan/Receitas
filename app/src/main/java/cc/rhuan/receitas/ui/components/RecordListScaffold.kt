package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.ui.theme.DesignTheme
import cc.rhuan.receitas.ui.theme.DesignTokens as T

val LocalNavigationClearance = compositionLocalOf { 0.dp }
internal val LocalRecordListHeight = compositionLocalOf { 0.dp }

/** Shared header, page creation action, list and insets. */
@Composable
fun RecordListScaffold(title: String, subtitle: String,
    listTag: String, modifier: Modifier = Modifier,
    createLabel: String? = null, onCreate: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    floatingTools: RecordListTools? = null, floatingAfterItems: Int = 3,
    headerActions: @Composable RowScope.() -> Unit = {}, content: LazyListScope.() -> Unit) {
    DesignTheme {
        val navigationClearance = LocalNavigationClearance.current
        val density = LocalDensity.current
        var viewportHeight by remember { mutableStateOf(0.dp) }
        var scrollingHeaderHeight by remember { mutableStateOf(0.dp) }
        val state = rememberLazyListState()
        val threshold = with(LocalDensity.current) { T.small.roundToPx() }
        var collapsed by rememberSaveable { mutableStateOf(false) }
        val scroll = remember(state, threshold) {
            object : NestedScrollConnection {
                private var travel = 0f
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput) {
                        if (travel * available.y < 0f) travel = 0f
                        travel += available.y
                        if (travel < -threshold) { collapsed = true; travel = 0f }
                        if (travel > threshold && state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0) {
                            collapsed = false; travel = 0f
                        }
                    }
                    return Offset.Zero
                }
            }
        }
        BoxWithConstraints(modifier.fillMaxSize().imePadding()) {
            val compact = maxHeight < T.compactHeight || LocalDensity.current.fontScale >= T.compactFontScale
            val expanded = !compact && !collapsed
            Scaffold(Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0), snackbarHost = {
                    Box(Modifier.padding(bottom = navigationClearance)) { snackbarHost() }
                },
                topBar = {
                    if (floatingTools == null) {
                        if (onBack != null) DetailHeader(title, onBack, headerActions)
                        else PageHeader(title, if (expanded) subtitle else "", expanded, headerActions)
                    }
                }) { padding ->
                CompositionLocalProvider(LocalRecordListHeight provides
                    (viewportHeight - T.section - navigationClearance -
                        if (floatingTools != null) scrollingHeaderHeight + T.gap else 0.dp).coerceAtLeast(0.dp)) {
                LazyColumn(state = state, modifier = Modifier.fillMaxSize().padding(padding)
                    .onSizeChanged { viewportHeight = with(density) { it.height.toDp() } }
                    .nestedScroll(scroll).testTag(listTag),
                    contentPadding = PaddingValues(bottom = T.section + navigationClearance),
                    verticalArrangement = Arrangement.spacedBy(T.gap)) {
                    if (floatingTools != null) item("scrolling-page-header") {
                      Box(Modifier.onSizeChanged { scrollingHeaderHeight = with(density) { it.height.toDp() } }) {
                        if (onBack != null) DetailHeader(title, onBack, headerActions)
                        else PageHeader(title, if (!compact) subtitle else "", !compact, headerActions)
                      }
                    }
                    createLabel?.let { label ->
                        item("create-record") {
                            ActionButton(label, onCreate, Modifier.fillMaxWidth()
                                .padding(horizontal = T.margin)
                                .testTag("create-record")
                                .semantics { contentDescription = label })
                        }
                    }
                    content()
                }
                }
            }
            floatingTools?.let { tools ->
                val showFloating by remember(state, floatingAfterItems) {
                    derivedStateOf { state.firstVisibleItemIndex >= floatingAfterItems }
                }
                Box(Modifier.align(Alignment.TopEnd)) { FloatingListToolbar(showFloating, tools, onBack) }
            }
        }
    }
}
