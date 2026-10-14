package cc.rhuan.receitas.ui.feature.home.components

import cc.rhuan.receitas.ui.theme.AppPalette

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(
    status: TrackingStatus,
    label: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        TrackingStatus.EXPIRED, TrackingStatus.PICKUP_OVERDUE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        TrackingStatus.EXPIRES_TODAY -> AppPalette.warningContainer to AppPalette.warning
        TrackingStatus.PICKUP_TODAY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        TrackingStatus.EXPIRES_SOON, TrackingStatus.PICKUP_SOON -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
