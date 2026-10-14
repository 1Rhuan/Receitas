package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp

/** Shared flat card used by lists, summaries and registration. */
@Composable
fun AppCard(
 modifier: Modifier = Modifier,
 shape: Shape = MaterialTheme.shapes.large,
 colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
 elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
 border: BorderStroke? = null,
 content: @Composable ColumnScope.() -> Unit,
) = Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation, border = border, content = content)
