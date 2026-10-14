package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.ui.components.intervalUnitText

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

import cc.rhuan.receitas.ui.theme.AppPalette

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import cc.rhuan.receitas.domain.model.RecordState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.RefillStatus
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
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ui_detalhes_da_receita),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            PrescriptionHeaderCard(
                                prescription = prescription,
                                dateFormatter = dateFormatter,
                                onRenewClick = onRenewClick,
                                onEditClick = onEditClick,
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (prescription.state != RecordState.REPLACED) TextButton(onClick = onArchive) { Text(if (prescription.state == RecordState.ARCHIVED) stringResource(R.string.action_reactivate) else stringResource(R.string.action_archive)) }
                                TextButton(onClick = onDelete) { Text(stringResource(R.string.ui_excluir_definitivamente)) }
                            }
                            Text(stringResource(R.string.record_status, when (prescription.state) { RecordState.ACTIVE -> stringResource(R.string.ui_ativa); RecordState.ARCHIVED -> stringResource(R.string.ui_arquivada); RecordState.REPLACED -> stringResource(R.string.ui_substituida) }))
                        }
                        item {
                            Text(
                                text = stringResource(R.string.ui_medicamentos_na_receita),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                            )
                        }

                        if (uiState.medications.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                ) {
                                    Text(
                                        text = stringResource(R.string.ui_nenhum_medicamento_vinculado_a_esta_receita),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                            }
                        } else {
                            items(
                                items = uiState.medications,
                                key = { it.medication.id },
                            ) { itemDetail ->
                                MedicationDetailCard(
                                    medication = itemDetail.medication,
                                    refillStatus = itemDetail.refillStatus,
                                    dateFormatter = dateFormatter,
                                    onReplenishClick = { onReplenishClick(itemDetail.medication.id) },
                                    onCorrectPickup = onCorrectPickup, onDeletePickup = onDeletePickup,
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailExpirationBadge(daysUntilExpiration: Int) {
    val isExpired = daysUntilExpiration < 0
    val (bgColor, textColor, labelText) = when {
        isExpired -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "Vencida")
        daysUntilExpiration == 0 -> Triple(AppPalette.warningContainer, AppPalette.warning, stringResource(R.string.ui_vence_hoje_2))
        daysUntilExpiration <= 30 -> {
            Triple(AppPalette.warningContainer, AppPalette.warning, pluralStringResource(R.plurals.prescription_expires_in_days, daysUntilExpiration, daysUntilExpiration))
        }
        else -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary, stringResource(R.string.valid))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
    ) {
        Text(
            text = labelText,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun PrescriptionHeaderCard(
    prescription: PrescriptionItem,
    dateFormatter: DateTimeFormatter,
    onRenewClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpired = prescription.isExpired
    val isExpiringSoon = prescription.daysUntilExpiration in 1..30
    
    val (containerBg, iconBg, iconColor) = when {
        isExpired -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
        isExpiringSoon -> Triple(AppPalette.warningContainer, AppPalette.warningContainer, AppPalette.warning)
        else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = iconBg,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bold_document_medicine),
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = prescription.specialty.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = iconColor,
                            ),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = prescription.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                IconButton(onClick = onEditClick, enabled = prescription.state != RecordState.REPLACED) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.ui_editar_receita),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (isExpired || isExpiringSoon) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (prescription.doctorName.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_info_circle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.ui_profissional_responsavel),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = prescription.doctorName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_info_circle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.ui_emissao),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = prescription.issueDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.ui_status),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            DetailExpirationBadge(daysUntilExpiration = prescription.daysUntilExpiration)
                        }
                    }
                }
            }

            if (prescription.notes != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prescription.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Legal note for prescription validity type
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppPalette.warningContainer, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppPalette.warning,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = prescription.type?.label ?: stringResource(R.string.label_unknown_prescription_type),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppPalette.warning,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.confirmed_expiration, prescription.expirationDate.format(dateFormatter)),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppPalette.warning,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRenewClick,
                enabled = prescription.isActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sort_vertical), // or a refresh icon
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_register_new_prescription),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun MedicationDetailCard(
    medication: MedicationItem,
    refillStatus: RefillStatus,
    dateFormatter: DateTimeFormatter,
    onReplenishClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCorrectPickup: (String, java.time.LocalDate) -> Unit = { _, _ -> }, onDeletePickup: (String) -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val subText = if (medication.prescribedPosology.isNotBlank()) "${medication.dosage} • ${medication.prescribedPosology}" else medication.dosage
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            }

            if (medication.prescribedQuantity.isNotBlank()) Text(stringResource(R.string.prescribed_quantity, medication.prescribedQuantity))
            if (medication.quantity.isNotBlank()) Text(
                text = stringResource(R.string.pickup_quantity, medication.quantity),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(10.dp))

            medication.lastRefillDate?.let { lastDate ->
                Text(
                    text = stringResource(R.string.last_pickup_with_interval, lastDate.format(dateFormatter), medication.refillCycleDays, intervalUnitText(medication.refillCycleDays, medication.intervalUnit)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            medication.nextAvailableRefillDate?.let { nextDate ->
                Text(
                    text = stringResource(R.string.next_pickup_date, nextDate.format(dateFormatter)),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (refillStatus) {
                is RefillStatus.PrescriptionExpired -> Text(stringResource(R.string.ui_receita_vinculada_vencida_ou_encerrada), color = MaterialTheme.colorScheme.error)
                is RefillStatus.PrescriptionExpiresBeforeRefill -> Text(stringResource(R.string.ui_a_receita_vence_antes_da_proxima_previsao_de_retirada), color = MaterialTheme.colorScheme.error)
                is RefillStatus.NoPrescriptionLinked -> Text(stringResource(R.string.ui_nenhuma_receita_vinculada_a_este_acompanhamento))
                else -> Unit
            }
            if (medication.hasRecurringPickup && !medication.trackingArchived) {
                OutlinedButton(onClick = onReplenishClick) { Text(stringResource(R.string.action_register_pickup)) }
                Text(stringResource(R.string.ui_a_data_e_uma_previsao_confirme_a_disponibilidade_com_a_farmacia), style = MaterialTheme.typography.bodySmall)
            }
            cc.rhuan.receitas.ui.components.PickupHistory(medication.pickupHistory, onCorrectPickup, onDeletePickup)

        }
    }
}
