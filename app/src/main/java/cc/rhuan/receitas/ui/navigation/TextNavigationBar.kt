package cc.rhuan.receitas.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Floating navigation capsule; its measured height reserves space at the end of lists. */
@Composable
fun TextNavigationBar(selectedRoute: String?, onSelectDestination: (String) -> Unit) {
    val largeText = LocalDensity.current.fontScale >= T.compactFontScale
    Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = T.margin, vertical = T.small)
        .testTag("floating-navigation"), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(T.gap)) {
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(T.fieldRadius), color = MaterialTheme.colorScheme.surface,
            shadowElevation = T.small) {
            NavigationTabs(selectedRoute, onSelectDestination, if (largeText) 2 else 3,
                Modifier.fillMaxWidth().padding(T.small))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NavigationTabs(selectedRoute: String?, onSelectDestination: (String) -> Unit,
    columns: Int, modifier: Modifier = Modifier) {
    val largeText = LocalDensity.current.fontScale >= T.compactFontScale
    FlowRow(modifier.selectableGroup(), maxItemsInEachRow = columns,
        verticalArrangement = Arrangement.spacedBy(T.small)) {
        NavRoute.bottomNavItems.filter { it != NavRoute.Menu }.forEach { destination ->
            val selected = destination.route == selectedRoute
            val label = when (destination) {
                NavRoute.Home -> stringResource(R.string.design_tab_home)
                NavRoute.Prescriptions -> stringResource(R.string.label_prescription_list)
                NavRoute.Medications -> stringResource(R.string.design_tab_medications)
                else -> stringResource(R.string.design_tab_menu)
            }
            Column(Modifier.weight(1f).heightIn(min = T.control)
                .selectable(selected, role = Role.Tab, onClick = { if (!selected) onSelectDestination(destination.route) })
                .semantics { contentDescription = destination.title }
                .padding(vertical = T.tight), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                val icon = when (destination) {
                    NavRoute.Home -> R.drawable.ic_nav_home
                    NavRoute.Prescriptions -> R.drawable.ic_nav_prescriptions
                    else -> R.drawable.ic_nav_medications
                }
                Box(Modifier.size(T.icon).background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(T.fieldRadius)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(T.icon),
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(T.tight))
                Text(label, style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
                if (selected && largeText) HorizontalDivider(Modifier.padding(top = T.small).width(T.section),
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
