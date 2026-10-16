package cc.rhuan.receitas.ui.components

import cc.rhuan.receitas.R

import androidx.compose.ui.res.stringResource

import android.app.DatePickerDialog
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import cc.rhuan.receitas.domain.model.MedicationItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PickupDialog(medication: MedicationItem, initialDate: LocalDate = LocalDate.now(), onDismiss: () -> Unit,
    onConfirm: (LocalDate, Boolean, LocalDate?) -> Unit) {
    var date by remember(medication.id) { mutableStateOf(initialDate.coerceAtMost(LocalDate.now())) }
    var confirmEarly by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val early = medication.nextAvailableRefillDate?.let { date.isBefore(it) } == true && medication.lastRefillDate?.let { date.isAfter(it) } == true
    ActionSheet(title = stringResource(if (confirmEarly) R.string.ui_confirmar_retirada_antecipada else R.string.action_register_pickup),
        onDismiss = onDismiss, actions = {
            SheetActionButton(stringResource(if (confirmEarly) R.string.ui_confirmar_antecipacao else R.string.ui_registrar), {
                if (early && !confirmEarly) confirmEarly = true
                else { onConfirm(date, early, medication.nextAvailableRefillDate); onDismiss() }
            })
            GuidanceActionButton(stringResource(R.string.action_cancel), onDismiss, Modifier.fillMaxWidth(), outlined = true)
        }) {
        if (confirmEarly) Text(stringResource(R.string.ui_a_data_escolhida_e_anterior_a_previsao_confirme_que_voce_ja_retirou_o_medic),
            style = MaterialTheme.typography.bodyLarge)
        else {
            Text(medication.title, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.ui_informe_a_data_em_que_a_retirada_aconteceu), style = MaterialTheme.typography.bodyLarge)
            SecondaryActionButton(date.format(format), {
                DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                    date.year, date.monthValue - 1, date.dayOfMonth).apply {
                    datePicker.maxDate = System.currentTimeMillis()
                }.show()
            }, Modifier.fillMaxWidth())
        }
    }
}
