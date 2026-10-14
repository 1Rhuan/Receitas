package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.theme.DesignTokens as T

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import cc.rhuan.receitas.domain.model.RecordState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import java.time.format.DateTimeFormatter

@Composable
fun PrescriptionDetailPresentation(
    uiState: PrescriptionDetailUiState,
    registerPickup: (String, java.time.LocalDate, Boolean, java.time.LocalDate?) -> Unit,
    deleteRecipe: () -> Unit,
    archive: (Boolean) -> Unit,
    clearActionMessage: () -> Unit,
    correctPickup: (String, java.time.LocalDate) -> Unit,
    deletePickup: (String) -> Unit,
    loadDetail: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: (String) -> Unit = {},
    onRenewClick: (String) -> Unit = {},
    onMedicationClick: (String) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingPickup by remember { mutableStateOf<MedicationItem?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmArchive by remember { mutableStateOf(false) }
    pendingPickup?.let { med -> cc.rhuan.receitas.ui.components.PickupDialog(med, onDismiss = { pendingPickup = null }) { date, early, cycle -> registerPickup(med.id, date, early, cycle) } }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text(stringResource(R.string.ui_excluir_receita_definitivamente)) }, text = { Text(stringResource(R.string.ui_o_documento_sera_removido_retiradas_e_acompanhamentos_serao_preservados_sem)) }, confirmButton = { TextButton(onClick = { confirmDelete = false; deleteRecipe() }) { Text(stringResource(R.string.action_delete)) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } })
    if (confirmArchive) AlertDialog(onDismissRequest = { confirmArchive = false }, title = { Text(stringResource(R.string.ui_arquivar_receita)) }, text = { Text(stringResource(R.string.ui_os_dados_ficam_no_historico_os_lembretes_e_acompanhamentos_vinculados_serao)) }, confirmButton = { TextButton(onClick = { confirmArchive = false; archive(true) }) { Text(stringResource(R.string.action_archive)) } }, dismissButton = { TextButton(onClick = { confirmArchive = false }) { Text(stringResource(R.string.action_cancel)) } })

    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onBackClick() }
    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { msg ->
            if (!uiState.isDeleted) snackbarHostState.showSnackbar(msg)
            clearActionMessage()
        }
    }

    PrescriptionDetailContent(
        uiState = uiState,
        onMedicationClick = onMedicationClick,
        onBackClick = onBackClick,
        onRenewClick = { uiState.prescription?.id?.let(onRenewClick) },
        onReplenishClick = { id -> pendingPickup = uiState.medications.find { it.medication.id == id }?.medication },
        onArchive = { if (uiState.prescription?.state == RecordState.ARCHIVED) archive(false) else confirmArchive = true },
        onDelete = { confirmDelete = true },
        onCorrectPickup = correctPickup, onDeletePickup = deletePickup,
        onRetry = loadDetail,
        onEditClick = { uiState.prescription?.id?.let { onEditClick(it) } },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionDetailContent(
    uiState: PrescriptionDetailUiState,
    onBackClick: () -> Unit,
    onRenewClick: () -> Unit,
    onReplenishClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onArchive: () -> Unit = {}, onDelete: () -> Unit = {},
    onCorrectPickup: (String, java.time.LocalDate) -> Unit = { _, _ -> }, onDeletePickup: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onMedicationClick: (String) -> Unit = {},
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val detailListState = rememberLazyListState()
    var viewingMedications by rememberSaveable { mutableStateOf(false) }
    var choosingRemoval by rememberSaveable(uiState.prescription?.id) { mutableStateOf(false) }
    if (choosingRemoval && uiState.prescription != null) RemovalChoiceDialog(
        stringResource(R.string.design_remove_recipe), uiState.prescription.state == RecordState.ACTIVE,
        stringResource(R.string.ui_os_dados_ficam_no_historico_os_lembretes_e_acompanhamentos_vinculados_serao),
        stringResource(R.string.ui_o_documento_sera_removido_retiradas_e_acompanhamentos_serao_preservados_sem),
        onArchive = { choosingRemoval = false; onArchive() },
        onDelete = { choosingRemoval = false; onDelete() }, onDismiss = { choosingRemoval = false })
    BackHandler(enabled = viewingMedications) { viewingMedications = false }
    if (viewingMedications) {
        PrescriptionMedicationList(uiState, { viewingMedications = false }, onMedicationClick, modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) })
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailHeader(stringResource(R.string.ui_detalhes_da_receita), onBackClick)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null -> {
                    cc.rhuan.receitas.ui.components.LoadErrorPanel(uiState.errorMessage, onRetry, Modifier.align(Alignment.Center))
                }

                uiState.prescription != null -> {
                    val prescription = uiState.prescription

                    LazyColumn(
                        state = detailListState,
                        modifier = Modifier.fillMaxSize().testTag("prescription-detail-list"),
                        contentPadding = PaddingValues(
                            horizontal = T.margin,
                            vertical = T.small,
                        ),
                        verticalArrangement = Arrangement.spacedBy(T.padding),
                    ) {
                        item {
                            PrescriptionHeaderCard(
                                prescription = prescription,
                                dateFormatter = dateFormatter,
                                onRenewClick = onRenewClick,
                                onEditClick = onEditClick,
                                onRemoveClick = { choosingRemoval = true }, onReactivateClick = onArchive,
                            )
                        }

                        item {
                            GroupedSection(stringResource(R.string.design_recipe_medications)) {
                                GroupedValueRow(stringResource(R.string.design_recipe_medications), uiState.medications.size.toString(), divider = false)
                                SecondaryActionButton(stringResource(R.string.design_view_recipe_medications),
                                    { viewingMedications = true }, Modifier.fillMaxWidth().padding(T.padding))
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(T.section))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrescriptionHeaderCard(
    prescription: PrescriptionItem,
    dateFormatter: DateTimeFormatter,
    onRenewClick: () -> Unit,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onReactivateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.small)) {
        if (prescription.specialty.isNotBlank()) Text(prescription.specialty, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        DetailIdentityHeader(prescription.title) {
            if (prescription.state != RecordState.REPLACED) FilledTonalIconButton(onEditClick, Modifier.size(T.control)) {
                Icon(Icons.Default.Edit, stringResource(R.string.ui_editar_receita))
            }
            if (prescription.state == RecordState.ARCHIVED) FilledTonalIconButton(onReactivateClick, Modifier.size(T.control)) {
                Icon(Icons.Default.Restore, stringResource(R.string.action_reactivate))
            }
            FilledTonalIconButton(onRemoveClick, Modifier.size(T.control)) {
                Icon(Icons.Default.DeleteOutline, stringResource(R.string.action_delete))
            }
        }
        StatusLabel(when {
            prescription.daysUntilExpiration < 0 -> "Vencida"
            prescription.daysUntilExpiration == 0 -> stringResource(R.string.ui_vence_hoje_2)
            prescription.daysUntilExpiration <= 30 -> pluralStringResource(R.plurals.prescription_expires_in_days, prescription.daysUntilExpiration, prescription.daysUntilExpiration)
            else -> stringResource(R.string.valid)
        }, attention = prescription.daysUntilExpiration <= 30)
        GroupedSection(stringResource(R.string.design_recipe_data)) {
            GroupedValueRow(stringResource(R.string.design_issue_date), prescription.issueDate.format(dateFormatter), divider = false)
            GroupedValueRow(stringResource(R.string.ui_tipo_de_receita), prescription.type?.label ?: stringResource(R.string.label_unknown_prescription_type))
            GroupedValueRow(stringResource(R.string.design_recipe_expiration), prescription.expirationDate.format(dateFormatter))
            GroupedValueRow(stringResource(R.string.design_record_state), stringResource(when (prescription.state) {
                RecordState.ACTIVE -> R.string.ui_ativa
                RecordState.ARCHIVED -> R.string.ui_arquivada
                RecordState.REPLACED -> R.string.ui_substituida
            }))
            if (prescription.doctorName.isNotBlank()) GroupedValueRow(stringResource(R.string.ui_profissional_responsavel), prescription.doctorName)
            prescription.notes?.takeIf { it.isNotBlank() }?.let { GroupedValueRow(stringResource(R.string.ui_observacoes_opcional), it) }
        }
        ActionButton(stringResource(R.string.action_register_new_prescription), onRenewClick, Modifier.fillMaxWidth(), enabled = prescription.isActive)
    }
}
@Composable
internal fun MedicationDetailCard(
    medication: MedicationItem,
    modifier: Modifier = Modifier,
    onMedicationClick: () -> Unit = {},
) {
    val openLabel = stringResource(R.string.design_view_medication)
    ContentCard(modifier.clickable(onClickLabel = openLabel, onClick = onMedicationClick)) {
        Text(medication.title, style = MaterialTheme.typography.titleLarge)
        Text(listOf(medication.form.label, medication.dosage).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (medication.prescribedPosology.isNotBlank()) DetailValue(stringResource(R.string.design_prescribed_instructions), medication.prescribedPosology)
        if (medication.prescribedQuantity.isNotBlank()) DetailValue(stringResource(R.string.design_prescribed_amount), medication.prescribedQuantity)
        SecondaryActionButton(stringResource(R.string.design_view_medication), onMedicationClick, Modifier.fillMaxWidth())
    }
}
