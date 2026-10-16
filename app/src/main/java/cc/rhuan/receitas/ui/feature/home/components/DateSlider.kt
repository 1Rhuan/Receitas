package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.feature.home.DayEventPriority
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
private fun calendarCellWidth() = T.control * (LocalDensity.current.fontScale / 1.5f).coerceAtLeast(1f)

@Composable
fun EventIndicator(priority: DayEventPriority, modifier: Modifier = Modifier) {
    Box(modifier.size(T.tight).clip(CircleShape).background(priority.color))
}

@Composable
fun DateItem(date: LocalDate, isSelected: Boolean, isToday: Boolean, eventPriority: DayEventPriority?,
    onDateSelected: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val locale = Locale.forLanguageTag("pt-BR")
    val description = date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", locale)) +
        if (eventPriority != null) stringResource(R.string.calendar_has_events) else ""
    Column(modifier.width(calendarCellWidth()).heightIn(min = T.control)
        .testTag("calendar-date-$date")
        .clip(MaterialTheme.shapes.large)
        .selectable(isSelected, role = Role.Button, onClick = { onDateSelected(date) })
        .semantics { contentDescription = description }
        .padding(vertical = T.small),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(T.tight)) {
        Text(date.format(DateTimeFormatter.ofPattern("EEE", locale)).removeSuffix("."),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.size(T.control).clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center) {
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
        if (eventPriority != null) EventIndicator(eventPriority) else Spacer(Modifier.height(T.tight))
    }
}

@Composable
fun DateSlider(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit,
    getEventIndicator: (LocalDate) -> DayEventPriority?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val cellWidth = calendarCellWidth()
        val available = (maxWidth - T.margin * 2).coerceAtLeast(cellWidth)
        val capacity = ((available + T.small) / (cellWidth + T.small)).toInt().coerceAtLeast(1)
        val daysPerPage = listOf(7, 5, 3, 1).first { it <= capacity }
        key(daysPerPage) { DatePager(selectedDate, onDateSelected, getEventIndicator, daysPerPage) }
    }
}

@Composable
private fun DatePager(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit,
    getEventIndicator: (LocalDate) -> DayEventPriority?, daysPerPage: Int) {
    val baseDate = remember { LocalDate.now() }
    val initialPage = Int.MAX_VALUE / 2
    val half = daysPerPage / 2
    val daysFromBase = ChronoUnit.DAYS.between(baseDate, selectedDate)
    val targetPage = initialPage + Math.floorDiv(daysFromBase + half, daysPerPage.toLong()).toInt()
    val pagerState = rememberPagerState(initialPage = targetPage, pageCount = { Int.MAX_VALUE })
    LaunchedEffect(targetPage, daysPerPage) {
        if (pagerState.currentPage != targetPage) pagerState.animateScrollToPage(targetPage)
    }
    LaunchedEffect(pagerState.settledPage, daysPerPage) {
        val center = baseDate.plusDays((pagerState.settledPage - initialPage).toLong() * daysPerPage)
        val start = center.minusDays(half.toLong())
        val end = start.plusDays(daysPerPage - 1L)
        if (selectedDate.isBefore(start) || selectedDate.isAfter(end)) {
            val position = Math.floorMod(daysFromBase + half, daysPerPage.toLong())
            onDateSelected(start.plusDays(position))
        }
    }
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().testTag("date-pager")) { page ->
        val center = baseDate.plusDays((page - initialPage).toLong() * daysPerPage)
        Row(Modifier.fillMaxWidth().padding(horizontal = T.margin),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            (-half..half).forEach { offset ->
                val date = center.plusDays(offset.toLong())
                DateItem(date, date == selectedDate, date == baseDate, getEventIndicator(date), onDateSelected)
            }
        }
    }
}
