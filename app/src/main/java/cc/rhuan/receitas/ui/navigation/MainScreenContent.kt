package cc.rhuan.receitas.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.ui.components.LocalNavigationClearance
import cc.rhuan.receitas.ui.theme.DesignTheme

@Composable
fun MainScreenContent(
    selectedRoute: String?,
    showBottomBar: Boolean,
    onSelectDestination: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    DesignTheme {
        var barHeight by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CompositionLocalProvider(LocalNavigationClearance provides if (showBottomBar) barHeight else 0.dp) {
                content(WindowInsets.systemBars.only(if (showBottomBar) WindowInsetsSides.Top + WindowInsetsSides.Horizontal else WindowInsetsSides.Top + WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal).asPaddingValues())
            }
            if (showBottomBar) Box(Modifier.align(Alignment.BottomCenter).onSizeChanged {
                barHeight = with(density) { it.height.toDp() }
            }) { TextNavigationBar(selectedRoute, onSelectDestination) }
        }
    }
}
