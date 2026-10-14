package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.ui.components.intervalUnitText

import cc.rhuan.receitas.R

import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.components.*
import java.time.format.DateTimeFormatter

@Composable
fun MedicationDetailContent(
    state: MedicationDetailUiState,
    message: String?,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onClearMessage: () -> Unit,
    onPickup: (java.time.LocalDate, Boolean, java.time.LocalDate?) -> Unit,
    onDelete: () -> Unit,
    onArchive: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onCorrectPickup: (String, java.time.LocalDate) -> Unit,
    onDeletePickup: (String) -> Unit,
) {
    val med = state.medication
    val snackbar = remember { SnackbarHostState() }
    var pickup by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var archiving by remember { mutableStateOf(false) }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }
    LaunchedEffect(message) { message?.let { onClearMessage(); if (!state.isDeleted) snackbar.showSnackbar(it) } }
    med?.let { m ->
        if (pickup) PickupDialog(m, onDismiss = { pickup = false }, onConfirm = onPickup)
        if (deleting) AlertDialog(onDismissRequest = { deleting = false }, title = { Text(stringResource(R.string.ui_excluir_medicamento_definitivamente)) }, text = { Text(stringResource(R.string.ui_o_acompanhamento_e_suas_retiradas_tambem_serao_excluidos_esta_acao_nao_pode)) }, confirmButton = { TextButton(onClick = { deleting = false; onDelete() }) { Text(stringResource(R.string.action_delete)) } }, dismissButton = { TextButton(onClick = { deleting = false }) { Text(stringResource(R.string.action_cancel)) } })
        if (archiving) AlertDialog(onDismissRequest = { archiving = false }, title = { Text(stringResource(R.string.ui_arquivar_acompanhamento)) }, text = { Text(stringResource(R.string.ui_o_historico_sera_preservado_e_os_lembretes_serao_encerrados)) }, confirmButton = { TextButton(onClick = { archiving = false; onArchive(true) }) { Text(stringResource(R.string.action_archive)) } }, dismissButton = { TextButton(onClick = { archiving = false }) { Text(stringResource(R.string.action_cancel)) } })
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            if (state.isLoading) CircularProgressIndicator()
            else if (state.errorMessage != null) LoadErrorPanel(requireNotNull(state.errorMessage), onRetry)
            else med?.let { m ->
                Text(m.title, style = MaterialTheme.typography.headlineSmall)
                Text(m.dosage)
                m.trackingError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text(if (m.trackingArchived) stringResource(R.string.ui_acompanhamento_arquivado) else if (m.hasRecurringPickup) stringResource(R.string.ui_acompanhamento_ativo) else stringResource(R.string.ui_sem_retirada_recorrente))
                Text(stringResource(R.string.pickup_interval, m.refillCycleDays, intervalUnitText(m.refillCycleDays, m.intervalUnit)))
                m.nextAvailableRefillDate?.let { Text(stringResource(R.string.next_pickup_date, it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))) }
                Text(stringResource(R.string.pickup_place, m.sourceName ?: m.source.label))
                TextButton(onClick = { onEdit(m.id) }) { Text(stringResource(R.string.ui_editar_acompanhamento_e_receita_vinculada)) }
                if (m.hasRecurringPickup && !m.trackingArchived) Button(onClick = { pickup = true }) { Text(stringResource(R.string.action_register_pickup)) }
                Row {
                    TextButton(onClick = { if (m.trackingArchived) onArchive(false) else archiving = true }) { Text(if (m.trackingArchived) stringResource(R.string.action_reactivate) else stringResource(R.string.action_archive)) }
                    TextButton(onClick = { deleting = true }) { Text(stringResource(R.string.ui_excluir_definitivamente)) }
                }
                PickupHistory(m.pickupHistory, onCorrectPickup, onDeletePickup)
            }
        }
    }
}
