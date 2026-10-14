package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import cc.rhuan.receitas.ui.theme.DesignTokens as T

/** Decorative connector; event text carries the chronological information. */
@Composable
fun TimelineEntry(first: Boolean, last: Boolean, modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val markerColor = if (first) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier.fillMaxWidth().drawBehind {
        val x = T.small.toPx()
        val markerY = T.section.toPx()
        drawLine(lineColor, Offset(x, if (first) markerY else 0f),
            Offset(x, if (last) markerY else size.height), strokeWidth = T.hairline.toPx())
        drawCircle(markerColor, radius = T.tight.toPx(), center = Offset(x, markerY))
    }.padding(start = T.section), content = content)
}
