package cc.rhuan.receitas.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Floating navigation capsule; its measured height reserves space at the end of lists. */
@Composable
fun TextNavigationBar(selectedRoute: String?, onSelectDestination: (String) -> Unit,
    onCreateRecord: (() -> Unit)? = null) {
    val largeText = LocalDensity.current.fontScale >= T.compactFontScale
    val createLabel = when (selectedRoute) {
        NavRoute.Prescriptions.route -> stringResource(R.string.navigation_create_prescription)
        NavRoute.Medications.route -> stringResource(R.string.navigation_create_medication)
        else -> null
    }
    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = T.margin, vertical = T.small)
        .testTag("floating-navigation"), verticalArrangement = Arrangement.spacedBy(T.small)) {
      BoxWithConstraints(Modifier.fillMaxWidth()) {
        val actionSpace = T.search + T.small
        val navigationWidth = if (largeText) maxWidth else maxWidth - actionSpace - (T.small - T.tight) * 2
        val showAction = createLabel != null && onCreateRecord != null
        val displacement = animateDpAsState(
            if (largeText) 0.dp else if (showAction) T.small - T.tight else (actionSpace / 2) + T.small - T.tight,
            label = "navigation-displacement").value
      Row(Modifier.fillMaxWidth().offset(x = displacement).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(T.small)) {
        Surface(modifier = Modifier.width(navigationWidth).testTag("navigation-capsule"), shape = RoundedCornerShape(T.fieldRadius), color = MaterialTheme.colorScheme.surface,
            shadowElevation = T.small) {
            NavigationTabs(selectedRoute, onSelectDestination, if (largeText) 2 else 3,
                Modifier.fillMaxWidth().padding(T.tight))
        }
        if (!largeText) Box(Modifier.width(T.search).fillMaxHeight()) {
            androidx.compose.animation.AnimatedVisibility(showAction, enter = fadeIn(), exit = fadeOut()) {
                NavigationCreationAction(createLabel.orEmpty(), { onCreateRecord?.invoke() }, false,
                    Modifier.fillMaxSize().heightIn(min = T.control))
            }
        }
      }
      }
      if (largeText && createLabel != null && onCreateRecord != null) {
          NavigationCreationAction(createLabel, onCreateRecord, true, Modifier.fillMaxWidth())
      }
    }
}

@Composable
private fun NavigationCreationAction(label: String, onClick: () -> Unit, showLabel: Boolean,
    modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = T.control).testTag("navigation-create"),
        shape = RoundedCornerShape(T.fieldRadius), color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface, shadowElevation = T.small) {
        Row(Modifier.padding(T.small), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Add, if (showLabel) null else label, Modifier.size(T.icon),
                tint = MaterialTheme.colorScheme.primary)
            if (showLabel) {
                Spacer(Modifier.width(T.small))
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NavigationTabs(selectedRoute: String?, onSelectDestination: (String) -> Unit,
    columns: Int, modifier: Modifier = Modifier) {
    FlowRow(modifier.selectableGroup(), maxItemsInEachRow = columns,
        verticalArrangement = Arrangement.spacedBy(T.small)) {
        NavRoute.bottomNavItems.filter { it != NavRoute.Menu }.forEach { destination ->
            val selected = destination.route == selectedRoute
            val selectionColor = animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                label = "navigation-selection").value
            val label = when (destination) {
                NavRoute.Home -> stringResource(R.string.design_tab_home)
                NavRoute.Prescriptions -> stringResource(R.string.label_prescription_list)
                NavRoute.Medications -> stringResource(R.string.design_tab_medications)
                else -> stringResource(R.string.design_tab_menu)
            }
            Column(Modifier.weight(1f).heightIn(min = T.control)
                .clip(CircleShape).background(selectionColor)
                .selectable(selected, role = Role.Tab, onClick = { if (!selected) onSelectDestination(destination.route) })
                .semantics { contentDescription = label }
                .padding(vertical = T.tight), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                val icon = if (selected) destination.selectedIconRes else destination.iconRes
                Box(Modifier.size(T.icon), contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(T.icon),
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(T.tight))
                Text(label, style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        }
    }
}
