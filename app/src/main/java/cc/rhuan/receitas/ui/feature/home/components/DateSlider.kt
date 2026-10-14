package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.ui.feature.home.DayEventPriority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun EventIndicator(
    priority: DayEventPriority,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(priority.color)
    )
}

@Composable
fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    eventPriority: DayEventPriority?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayInitial = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "D"
        DayOfWeek.MONDAY -> "S"
        DayOfWeek.TUESDAY -> "T"
        DayOfWeek.WEDNESDAY -> "Q"
        DayOfWeek.THURSDAY -> "Q"
        DayOfWeek.FRIDAY -> "S"
        DayOfWeek.SATURDAY -> "S"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onDateSelected(date) }
            .background(
                if (isSelected) MaterialTheme.colorScheme.outlineVariant else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = dayInitial,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                )
            }
        } else {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                )
            }
        }

        if (eventPriority != null) {
            EventIndicator(priority = eventPriority)
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun DateSlider(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    getEventIndicator: (LocalDate) -> DayEventPriority?,
    modifier: Modifier = Modifier
) {
    val baseDate = remember { LocalDate.now() }
    val initialPage = 5000
    val pageCount = 10000

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    val daysFromBase = ChronoUnit.DAYS.between(baseDate, selectedDate)
    val targetPage = initialPage + Math.floorDiv(daysFromBase + 3, 7L).toInt()

    LaunchedEffect(targetPage) {
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val currentOffset = pagerState.settledPage - initialPage
        val centerDate = baseDate.plusDays(currentOffset * 7L)
        val pageStart = centerDate.minusDays(3)
        val pageEnd = centerDate.plusDays(3)

        if (selectedDate.isBefore(pageStart) || selectedDate.isAfter(pageEnd)) {
            val relativeOffset = ChronoUnit.DAYS.between(baseDate, selectedDate) % 7
            val normalizedOffset = if (relativeOffset < 0) relativeOffset + 7 else relativeOffset
            val newSelectedDate = pageStart.plusDays(normalizedOffset)
            onDateSelected(newSelectedDate)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
    ) { page ->
        val weekOffset = page - initialPage
        val centerDate = baseDate.plusDays(weekOffset * 7L)
        val weekDays = (-3..3).map { centerDate.plusDays(it.toLong()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDays.forEach { date ->
                DateItem(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == baseDate,
                    eventPriority = getEventIndicator(date),
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}
