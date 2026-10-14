package cc.rhuan.receitas.ui.components

import cc.rhuan.receitas.R

import androidx.compose.ui.res.stringResource

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cc.rhuan.receitas.domain.model.PickupEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PickupHistory(events: List<PickupEvent>, onCorrect: (String, LocalDate) -> Unit, onDelete: (String) -> Unit) {
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
    Text(stringResource(R.string.ui_historico_de_retiradas), style = MaterialTheme.typography.titleSmall)
    if (events.isEmpty()) Text(stringResource(R.string.ui_nenhuma_retirada_registrada))
    events.forEach { event ->
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = event.occurredOn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), modifier = Modifier.weight(1f))
            TextButton(onClick = {
                DatePickerDialog(context, { _, y, m, d -> correcting = event to LocalDate.of(y, m + 1, d) }, event.occurredOn.year, event.occurredOn.monthValue - 1, event.occurredOn.dayOfMonth)
                    .apply { datePicker.maxDate = System.currentTimeMillis() }.show()
            }) { Text(stringResource(R.string.action_correct)) }
            TextButton(onClick = { deleting = event }) { Text(stringResource(R.string.action_delete)) }
        }
    }
}
