package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.R

@Composable
fun HomeHeader(
    title: String,
    notificationCount: Int,
    onNotificationClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(onClick = onCalendarClick)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF0F172A)
                )
            )

            Surface(
                onClick = onCalendarClick,
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendário",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Surface(
            onClick = onNotificationClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = notificationCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if (notificationCount > 0) R.drawable.ic_bell_bing else R.drawable.ic_bell),
                        contentDescription = "Notificações",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
