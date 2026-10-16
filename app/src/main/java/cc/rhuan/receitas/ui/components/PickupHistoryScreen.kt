package cc.rhuan.receitas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import java.time.LocalDate

/** Dedicated history page, shared by prescription and standalone medication details. */
@Composable
fun PickupHistoryScreen(medication: MedicationItem, onBack: () -> Unit,
    onCorrect: (String, LocalDate) -> Unit, onDelete: (String) -> Unit,
    modifier: Modifier = Modifier, snackbarHost: @Composable () -> Unit = {}) {
    Scaffold(modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background,
        topBar = { DetailHeader(stringResource(R.string.pickup_history), onBack) },
        snackbarHost = snackbarHost) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("pickup-history-list"),
            contentPadding = PaddingValues(horizontal = T.margin, vertical = T.padding),
            verticalArrangement = Arrangement.Top) {
            item("medication") {
                Column(Modifier.padding(bottom = T.padding), verticalArrangement = Arrangement.spacedBy(T.tight)) {
                    Text(medication.title, style = MaterialTheme.typography.titleLarge)
                    Text(pluralStringResource(R.plurals.design_history_total, medication.pickupHistory.size, medication.pickupHistory.size),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (medication.pickupHistory.isEmpty()) item("empty") {
                ContentCard { Text(stringResource(R.string.no_pickups_recorded)) }
            }
            val sortedEvents = medication.pickupHistory.sortedWith(compareByDescending<cc.rhuan.receitas.domain.model.PickupEvent> { it.occurredOn }.thenByDescending { it.recordedAt })
            items(sortedEvents, key = { it.id }) { event ->
                TimelineEntry(first = event == sortedEvents.first(), last = event == sortedEvents.last()) {
                    PickupHistory(listOf(event), onCorrect, onDelete, showHeading = false)
                }
            }
        }
    }
}
