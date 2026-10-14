package cc.rhuan.receitas.ui.feature.prescriptions

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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.RefillStatus
import java.time.format.DateTimeFormatter

@Composable
fun PrescriptionDetailScreen(
    viewModel: PrescriptionDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    PrescriptionDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRenewClick = viewModel::renewPrescription,
        onReplenishClick = viewModel::replenishMedication,
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
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier,
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
                        text = "Detalhes da Receita",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
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
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
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
                            Text(
                                text = "MEDICAMENTOS NA RECEITA",
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
                                        text = "Nenhum medicamento vinculado a esta receita.",
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
    val isExpired = daysUntilExpiration <= 0
    val (bgColor, textColor, labelText) = when {
        isExpired -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "Vencida")
        daysUntilExpiration <= 30 -> {
            val daysText = if (daysUntilExpiration == 1) "dia" else "dias"
            Triple(AppPalette.warningContainer, AppPalette.warning, "Venc. $daysUntilExpiration $daysText")
        }
        else -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary, "Válida")
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

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Receita",
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
                                    text = "MÉDICO PRESCRITOR",
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
                                    text = "EMISSÃO",
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
                                text = "STATUS",
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
                        text = prescription.type?.label ?: "Sem Tipo Especificado",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppPalette.warning,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prescription.type?.legalNote ?: "Receita sem classificação regulatória especificada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppPalette.warning,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRenewClick,
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
                    text = if (isExpired) "Solicitar Nova Receita" else "Renovar Receita",
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

            if (medication.quantity.isNotBlank()) Text(
                text = "Quantidade por reposição: " + medication.quantity,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(10.dp))

            medication.lastRefillDate?.let { lastDate ->
                Text(
                    text = "Última retirada: ${lastDate.format(dateFormatter)} (Ciclo: ${medication.refillCycleDays} dias)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            medication.nextAvailableRefillDate?.let { nextDate ->
                Text(
                    text = "Próxima retirada prevista: ${nextDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Banner
            when (refillStatus) {
                is RefillStatus.PrescriptionExpiresBeforeRefill -> {
                    RefillWarningBanner(
                        title = "Atenção: Receita Vencerá Antes da Reposição",
                        message = "A receita expira em ${refillStatus.expirationDate.format(dateFormatter)}, mas a próxima retirada está prevista para ${refillStatus.nextRefillDate.format(dateFormatter)}. Solicite nova receita a tempo.",
                        bgColor = AppPalette.warningContainer,
                        iconColor = AppPalette.warning,
                    )
                }

                is RefillStatus.PrescriptionExpired -> {
                    RefillWarningBanner(
                        title = "Receita Vencida",
                        message = "A receita vinculada já expirou. Você precisará de uma nova consulta.",
                        bgColor = MaterialTheme.colorScheme.errorContainer,
                        iconColor = MaterialTheme.colorScheme.error,
                    )
                }

                is RefillStatus.RefillAvailable -> {
                    RefillSuccessBanner(
                        title = "Data de retirada alcançada",
                        message = "Confira a disponibilidade com a farmácia antes de retirar.",
                        onReplenishClick = onReplenishClick,
                    )
                }

                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Receita válida e dentro do prazo de reposição.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefillWarningBanner(
    title: String,
    message: String,
    bgColor: Color,
    iconColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = iconColor,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun RefillSuccessBanner(
    title: String,
    message: String,
    onReplenishClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onReplenishClick,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Registrar Retirada")
            }
        }
    }
}
