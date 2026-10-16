package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.DesignTokens as T

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeHeader(title: String, notificationCount: Int, onNotificationClick: () -> Unit,
    onCalendarClick: () -> Unit, modifier: Modifier = Modifier) {
    val compact = LocalDensity.current.fontScale >= T.compactFontScale
    FlowRow(modifier.fillMaxWidth().padding(horizontal = T.margin, vertical = T.small),
        maxItemsInEachRow = if (compact) 1 else 2,
        horizontalArrangement = Arrangement.spacedBy(T.small), verticalArrangement = Arrangement.spacedBy(T.small)) {
        Row(Modifier.weight(1f).heightIn(min = T.control).clickable(onClick = onCalendarClick),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(T.small)) {
            Text(title, Modifier.weight(1f), style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium)
            FilledTonalIconButton(onCalendarClick, Modifier.size(T.control)) {
                Icon(Icons.Rounded.CalendarMonth, stringResource(R.string.ui_calendario), Modifier.size(T.icon))
            }
        }
        if (compact) cc.rhuan.receitas.ui.components.SecondaryActionButton(
            stringResource(R.string.notifications_title) + if (notificationCount > 0) " ($notificationCount)" else "",
            onNotificationClick, Modifier.fillMaxWidth())
        else FilledTonalIconButton(onNotificationClick, Modifier.size(T.control)) {
            BadgedBox(badge = { if (notificationCount > 0) Badge { Text(notificationCount.toString(), style = MaterialTheme.typography.labelSmall) } }) {
                Icon(painterResource(if (notificationCount > 0) R.drawable.ic_bell_bing else R.drawable.ic_bell),
                    stringResource(R.string.notifications_title), Modifier.size(T.icon))
            }
        }
    }
}
