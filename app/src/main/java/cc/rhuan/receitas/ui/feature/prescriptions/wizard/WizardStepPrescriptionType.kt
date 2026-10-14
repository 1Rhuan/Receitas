package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.ui.theme.AppPalette

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardStepPrescriptionType(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    modifier: Modifier = Modifier
) {
    val selectedType = uiState.type

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Conversational Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "4. Tipo & Validade da Receita",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Qual é o tipo e validade desta receita?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Main Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Prescription Title
                Text(
                    text = "Título da Receita",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = uiState.prescriptionTitle,
                    onValueChange = viewModel::updatePrescriptionTitle,
                    placeholder = { Text("Ex: Receita Uso Contínuo - 15/10/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Prescription Type Chips / Selection
                Text(
                    text = "Classificação / Tipo de Receita",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val mainTypes = listOf(
                    PrescriptionType.CONTINUO,
                    PrescriptionType.ANTIBIOTICO,
                    PrescriptionType.CONTROLADO,
                    PrescriptionType.FARMACIA_POPULAR,
                    PrescriptionType.SUS_BASIC
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mainTypes.forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updatePrescriptionType(type) },
                            label = { Text(type.label) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Legal Note Banner if type is selected
                if (selectedType != null) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppPalette.warningContainer, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AppPalette.warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Validade Regulamentar: ${selectedType.defaultValidityDays} dias",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = AppPalette.warning
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedType.legalNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppPalette.warning
                            )
                        }
                    }
                }

                // 3. Validity Days
                uiState.validityDays?.let { validity ->
                    Text(
                        text = "Validade Prescrita em Dias",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = validity.toString(),
                        onValueChange = { str ->
                            str.toIntOrNull()?.let { viewModel.updateValidityDays(it) }
                        },
                        placeholder = { Text("180") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val expirationDate = uiState.issueDate.plusDays(validity.toLong())
                    Text(
                        text = "Data de Vencimento: ${expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 4. Renewal Reminder Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Avisar antes da receita vencer?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lembrete para marcar nova consulta médica",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.renewalReminderEnabled,
                        onCheckedChange = viewModel::updateRenewalReminderEnabled
                    )
                }

                AnimatedVisibility(visible = uiState.renewalReminderEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Antecipação do lembrete:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(3, 5, 7, 10, 15).forEach { days ->
                                FilterChip(
                                    selected = uiState.renewalReminderDaysBefore == days,
                                    onClick = { viewModel.updateRenewalReminderDaysBefore(days) },
                                    label = { Text("$days dias antes") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
