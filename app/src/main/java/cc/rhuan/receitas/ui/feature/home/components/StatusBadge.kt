package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        TrackingStatus.EXPIRED, TrackingStatus.PICKUP_OVERDUE -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
        TrackingStatus.EXPIRES_TODAY -> Color(0xFFFFEDD5) to Color(0xFFEA580C)
        TrackingStatus.PICKUP_TODAY -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        TrackingStatus.EXPIRES_SOON, TrackingStatus.PICKUP_SOON -> Color(0xFFEFF6FF) to Color(0xFF2563EB)
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
