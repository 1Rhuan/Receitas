package cc.rhuan.receitas.ui.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.components.AppCard as Card
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TrackingList(
    items: List<TrackingItem>,
    onItemClick: (TrackingItem) -> Unit,
    onPrimaryAction: (TrackingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(T.padding)
    ) {
        items.forEach { item ->
            when (item) {
                is TrackingItem.Medication -> {
                    MedicationTrackingCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onPrimaryAction = { onPrimaryAction(item) },
                    )
                }
                is TrackingItem.Prescription -> {
                    PrescriptionTrackingCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onPrimaryAction = { onPrimaryAction(item) },
                    )
                }
            }
        }
    }
}

@Composable
fun NextEventHeader() {
    cc.rhuan.receitas.ui.components.SectionHeading(stringResource(R.string.ui_proximos_eventos))
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NextEventCard(item: TrackingItem, onClick: () -> Unit, onNavigateToDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier) {
    val iconRes = if (item is TrackingItem.Prescription) R.drawable.ic_bold_document_medicine else R.drawable.ic_bold_pills
    val typeLabel = if (item is TrackingItem.Prescription) stringResource(R.string.label_prescription) else stringResource(R.string.label_medication)
    val eventLabel = if (item is TrackingItem.Prescription) stringResource(R.string.ui_vencimento_da_receita) else stringResource(R.string.ui_retirada_de_medicamento)
    Card(modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.small)) {
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(T.small),
                verticalArrangement = Arrangement.spacedBy(T.small)) {
                Icon(painterResource(iconRes), null, Modifier.size(T.icon), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(typeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                item.contextTag?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(eventLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            item.rawDate?.let { date ->
                cc.rhuan.receitas.ui.components.SecondaryActionButton(
                    stringResource(R.string.view_event_date, date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                    { onNavigateToDate(date) }, Modifier.fillMaxWidth())
            }
        }
    }
}
