package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.ui.components.intervalText

import cc.rhuan.receitas.ui.theme.DesignTokens as T
import cc.rhuan.receitas.R

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Restore
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
    onPrescriptionsClick: (String) -> Unit = {},
) {
    val med = state.medication
    val snackbar = remember { SnackbarHostState() }
    var pickup by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var archiving by remember { mutableStateOf(false) }
    var choosingRemoval by rememberSaveable(med?.id) { mutableStateOf(false) }
    var viewingHistory by rememberSaveable(med?.id) { mutableStateOf(false) }
    val detailScrollState = rememberScrollState()
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }
    LaunchedEffect(message) { message?.let { onClearMessage(); if (!state.isDeleted) snackbar.showSnackbar(it) } }
    BackHandler(enabled = viewingHistory) { viewingHistory = false }
    if (viewingHistory && med != null) {
        PickupHistoryScreen(med, { viewingHistory = false }, onCorrectPickup, onDeletePickup,
            snackbarHost = { SnackbarHost(snackbar) })
        return
    }
    med?.let { m ->
        if (choosingRemoval) RemovalChoiceDialog(stringResource(R.string.design_remove_medication), !m.trackingArchived,
            stringResource(R.string.ui_o_historico_sera_preservado_e_os_lembretes_serao_encerrados),
            stringResource(R.string.ui_o_acompanhamento_e_suas_retiradas_tambem_serao_excluidos_esta_acao_nao_pode),
            onArchive = { choosingRemoval = false; archiving = true },
            onDelete = { choosingRemoval = false; deleting = true }, onDismiss = { choosingRemoval = false })
        if (pickup) PickupDialog(m, onDismiss = { pickup = false }, onConfirm = onPickup)
        if (deleting) ConfirmationSheet(title = stringResource(R.string.ui_excluir_medicamento_definitivamente), message = stringResource(R.string.ui_o_acompanhamento_e_suas_retiradas_tambem_serao_excluidos_esta_acao_nao_pode), confirmLabel = stringResource(R.string.action_delete), onConfirm = { deleting = false; onDelete() }, onDismiss = { deleting = false }, destructive = true)
        if (archiving) ConfirmationSheet(title = stringResource(R.string.ui_arquivar_acompanhamento), message = stringResource(R.string.ui_o_historico_sera_preservado_e_os_lembretes_serao_encerrados), confirmLabel = stringResource(R.string.action_archive), onConfirm = { archiving = false; onArchive(true) }, onDismiss = { archiving = false }, destructive = false)
    }
    Scaffold(topBar = { DetailHeader(stringResource(R.string.design_medication_details), onBack) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(detailScrollState).padding(horizontal = T.margin, vertical = T.padding), verticalArrangement = Arrangement.spacedBy(T.gap)) {

            if (state.isLoading) CircularProgressIndicator()
            else if (state.errorMessage != null) LoadErrorPanel(requireNotNull(state.errorMessage), onRetry)
            else med?.let { m ->
                DetailIdentityHeader(m.title) {
                    FilledTonalIconButton({ onEdit(m.id) }, Modifier.size(T.control)) {
                        Icon(Icons.Rounded.Edit, stringResource(R.string.design_edit_tracking))
                    }
                    if (m.trackingArchived) FilledTonalIconButton({ onArchive(false) }, Modifier.size(T.control)) {
                        Icon(Icons.Rounded.Restore, stringResource(R.string.action_reactivate))
                    }
                    FilledTonalIconButton({ choosingRemoval = true }, Modifier.size(T.control)) {
                        Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.action_delete))
                    }
                }
                if (m.dosage.isNotBlank()) Text(m.dosage, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                m.trackingError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                GroupedSection(stringResource(R.string.design_medication_data)) {
                    GroupedValueRow(stringResource(R.string.design_medication_form), m.form.label, divider = false)
                    m.activeIngredient?.takeIf { it.isNotBlank() }?.let {
                        GroupedValueRow(stringResource(R.string.design_active_ingredient), it)
                    }
                    m.instructions?.takeIf { it.isNotBlank() }?.let {
                        GroupedValueRow(stringResource(R.string.design_medication_notes), it)
                    }
                    GroupedValueRow(stringResource(R.string.design_medication_prescriptions),
                        if (state.prescriptions.isEmpty()) stringResource(R.string.design_zero_linked_recipes)
                        else pluralStringResource(R.plurals.design_linked_recipe_count, state.prescriptions.size, state.prescriptions.size))
                    SecondaryActionButton(stringResource(R.string.design_view_linked_recipes), { onPrescriptionsClick(m.id) },
                        Modifier.fillMaxWidth().padding(T.padding), enabled = state.prescriptions.isNotEmpty())
                }
                GroupedSection(stringResource(R.string.design_pickup_tracking)) {
                    GroupedValueRow(stringResource(R.string.design_record_state), stringResource(when {
                        m.trackingArchived -> R.string.ui_arquivada
                        m.hasRecurringPickup -> R.string.ui_ativa
                        else -> R.string.ui_sem_retirada_recorrente
                    }), divider = false)
                    m.lastRefillDate?.let { GroupedValueRow(stringResource(R.string.design_last_pickup), it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
                    if (m.hasRecurringPickup) GroupedValueRow(stringResource(R.string.design_pickup_interval),
                        intervalText(m.refillCycleDays, m.intervalUnit))
                    m.nextAvailableRefillDate?.let { GroupedValueRow(stringResource(R.string.design_next_pickup), it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
                    if (m.quantity.isNotBlank()) GroupedValueRow(stringResource(R.string.design_pickup_amount), m.quantity)
                    GroupedValueRow(stringResource(R.string.design_pickup_location), m.sourceName?.takeIf { it.isNotBlank() } ?: m.source.label)
                    if (m.linkedPrescriptionId != null) GroupedValueRow(stringResource(R.string.design_tracking_recipe),
                        state.pickupPrescription?.title ?: stringResource(R.string.design_tracking_recipe_unavailable), stacked = true)
                    val validityStatus = cc.rhuan.receitas.domain.usecase.CheckRefillStatusUseCase()(m, listOfNotNull(state.pickupPrescription))
                    when (validityStatus) {
                        is RefillStatus.PrescriptionExpiresBeforeRefill -> GroupedValueRow(
                            stringResource(R.string.pickup_validity_warning),
                            stringResource(R.string.pickup_validity_conflict,
                                validityStatus.expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                validityStatus.nextRefillDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), stacked = true)
                        is RefillStatus.PrescriptionExpired -> GroupedValueRow(
                            stringResource(R.string.pickup_validity_warning),
                            stringResource(R.string.pickup_recipe_expired, validityStatus.prescriptionTitle), stacked = true)
                        else -> Unit
                    }
                }
                if (m.hasRecurringPickup && !m.trackingArchived) ActionButton(stringResource(R.string.action_register_pickup), { pickup = true }, Modifier.fillMaxWidth())
                SectionHeading(stringResource(R.string.design_recent_pickups))
                val recent = m.pickupHistory.sortedWith(compareByDescending<PickupEvent> { it.occurredOn }.thenByDescending { it.recordedAt }).take(3)
                if (recent.isEmpty()) Text(stringResource(R.string.ui_nenhuma_retirada_registrada),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else Column {
                    recent.forEachIndexed { index, event ->
                        TimelineEntry(first = index == 0, last = index == recent.lastIndex) {
                            PickupHistory(listOf(event), onCorrectPickup, onDeletePickup, showHeading = false, showActions = false)
                        }
                    }
                }
                SecondaryActionButton(stringResource(R.string.design_view_pickup_history),
                    { viewingHistory = true }, Modifier.fillMaxWidth())
            }
        }
    }
}
