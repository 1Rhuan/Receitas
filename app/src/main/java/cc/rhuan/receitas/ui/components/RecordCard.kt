package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Same information hierarchy for recipe and medication records. */
@Composable
fun RecordCard(title: String, category: String, iconRes: Int, onClick: () -> Unit,
 modifier: Modifier = Modifier, badge: @Composable () -> Unit = {},
 content: @Composable ColumnScope.() -> Unit) {
 AppCard(modifier.fillMaxWidth().clickable(onClick = onClick)) {
  Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(category, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    badge()
   }
   HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
   Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
     Box(contentAlignment = Alignment.Center) { Icon(painterResource(iconRes), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
    }
    Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
   }
   content()
  }
 }
}
