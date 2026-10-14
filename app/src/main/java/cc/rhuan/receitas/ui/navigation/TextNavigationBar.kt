package cc.rhuan.receitas.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

/** Floating navigation and contextual creation share one surface. */
@Composable
fun TextNavigationBar(selectedRoute: String?, onSelectDestination: (String) -> Unit,
    onCreateRecord: (() -> Unit)? = null) {
    val largeText = LocalDensity.current.fontScale >= T.compactFontScale
    val label = when (selectedRoute) {
        NavRoute.Prescriptions.route -> stringResource(R.string.action_new_prescription)
        NavRoute.Medications.route -> stringResource(R.string.ui_novo_medicamento)
        else -> null
    }
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = T.margin, vertical = T.small)) {
        Surface(modifier = Modifier.testTag("floating-navigation"), shape = RoundedCornerShape(T.fieldRadius), color = MaterialTheme.colorScheme.surface,
            shadowElevation = T.small) {
            if (largeText) Column(Modifier.padding(T.small)) {
                NavigationTabs(selectedRoute, onSelectDestination, 2, Modifier.fillMaxWidth())
                if (label != null && onCreateRecord != null) Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) { CreationButton(label, onCreateRecord) }
            } else Row(Modifier.padding(T.small), verticalAlignment = Alignment.CenterVertically) {
                NavigationTabs(selectedRoute, onSelectDestination, 3, Modifier.weight(1f))
                if (label != null && onCreateRecord != null) {
                    Spacer(Modifier.width(T.small))
                    CreationButton(label, onCreateRecord)
                }
            }
        }
    }
}

@Composable
private fun CreationButton(label: String, onClick: () -> Unit) {
    FilledIconButton(onClick, Modifier.size(T.control),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary)) {
        Icon(Icons.Default.Add, contentDescription = label, modifier = Modifier.size(T.icon))
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
                .padding(vertical = T.small), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                val icon = when (destination) {
                    NavRoute.Home -> R.drawable.ic_nav_home
                    NavRoute.Prescriptions -> R.drawable.ic_nav_prescriptions
                    else -> R.drawable.ic_nav_medications
                }
                Box(Modifier.size(T.control - T.small).background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(T.fieldRadius)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(T.icon),
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(T.small))
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
