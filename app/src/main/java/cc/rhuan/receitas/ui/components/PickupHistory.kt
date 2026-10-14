package cc.rhuan.receitas.ui.components

import cc.rhuan.receitas.ui.theme.DesignTokens as T
import cc.rhuan.receitas.R

import androidx.compose.ui.res.stringResource

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import cc.rhuan.receitas.domain.model.PickupEvent
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PickupHistory(events: List<PickupEvent>, onCorrect: (String, LocalDate) -> Unit, onDelete: (String) -> Unit,
    showHeading: Boolean = true, showActions: Boolean = true) {
    val context = LocalContext.current
    var deleting by remember { mutableStateOf<PickupEvent?>(null) }
    var correcting by remember { mutableStateOf<Pair<PickupEvent, LocalDate>?>(null) }
    deleting?.let { event -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text(stringResource(R.string.ui_excluir_retirada)) },
        text = { Text(stringResource(R.string.ui_a_proxima_previsao_sera_recalculada_a_partir_dos_registros_restantes)) },
        confirmButton = { TextButton(onClick = { onDelete(event.id); deleting = null }) { Text(stringResource(R.string.action_delete)) } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel)) } }) }
    correcting?.let { (event, date) -> AlertDialog(onDismissRequest = { correcting = null }, title = { Text(stringResource(R.string.ui_corrigir_data_da_retirada)) },
        text = { Text(stringResource(R.string.correct_pickup_date, date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))) },
        confirmButton = { TextButton(onClick = { onCorrect(event.id, date); correcting = null }) { Text(stringResource(R.string.action_confirm)) } },
        dismissButton = { TextButton(onClick = { correcting = null }) { Text(stringResource(R.string.action_cancel)) } }) }
    if (showHeading) SectionHeading(stringResource(R.string.ui_historico_de_retiradas))
    if (events.isEmpty()) Text(stringResource(R.string.ui_nenhuma_retirada_registrada))
    events.forEach { event ->
        FlowRow(Modifier.fillMaxWidth().padding(T.padding), maxItemsInEachRow = if (LocalDensity.current.fontScale >= T.compactFontScale) 1 else 2,
            horizontalArrangement = Arrangement.spacedBy(T.small), verticalArrangement = Arrangement.spacedBy(T.small)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(T.tight)) {
            Text(text = event.occurredOn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.titleMedium)
            Text(event.occurredOn.format(DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("pt-BR"))),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (event.recordedAt > 0) Text(stringResource(R.string.design_pickup_recorded) + ": " +
                Instant.ofEpochMilli(event.recordedAt).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showActions) ContextActionsButton(listOf(ContextAction(stringResource(R.string.action_correct), onClick = {
                DatePickerDialog(context, { _, y, m, d -> correcting = event to LocalDate.of(y, m + 1, d) }, event.occurredOn.year, event.occurredOn.monthValue - 1, event.occurredOn.dayOfMonth)
                    .apply { datePicker.maxDate = System.currentTimeMillis() }.show()
            }), ContextAction(stringResource(R.string.action_delete), destructive = true, onClick = { deleting = event })))
        }
    }
}
