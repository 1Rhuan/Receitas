package cc.rhuan.receitas.ui.feature.prescriptions.wizard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import cc.rhuan.receitas.domain.model.MedicationSource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardStepReplenishment(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    modifier: Modifier = Modifier
) {
    val draft = uiState.currentMedicationDraft
    val medName = if (draft.title.isNotBlank()) draft.title else "o medicamento"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Conversational Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "3. Reposição & Estoque",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Como você repõe $medName?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Main Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Origem
                Text(
                    text = "Origem do Medicamento",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MedicationSource.entries.forEach { src ->
                        val isSelected = draft.source == src
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCurrentMedicationDraft(source = src) },
                            label = { Text(src.label) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 2. Estoque Atual
                Text(
                    text = "Estoque Atual (Opcional - Ativa alerta de recarga)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                OutlinedTextField(
                    value = draft.currentStock?.toString() ?: "",
                    onValueChange = { str ->
                        val stock = str.toIntOrNull()
                        viewModel.updateCurrentMedicationDraft(currentStock = stock)
                    },
                    placeholder = { Text("Ex: 30 unidades") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Stock Quick Chips
                val stockPresets = listOf(10, 20, 30, 60, 90)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stockPresets.forEach { preset ->
                        FilterChip(
                            selected = draft.currentStock == preset,
                            onClick = { viewModel.updateCurrentMedicationDraft(currentStock = preset) },
                            label = { Text("$preset un") }
                        )
                    }
                }

                // 3. Retirada Recorrente na Farmácia/SUS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Retirada Recorrente na Farmácia/SUS?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Lembrete periódico para buscar nova caixa",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = draft.hasRecurringPickup,
                        onCheckedChange = { viewModel.updateCurrentMedicationDraft(hasRecurringPickup = it) }
                    )
                }

                AnimatedVisibility(visible = draft.hasRecurringPickup) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Intervalo de retirada (dias):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { interval ->
                                FilterChip(
                                    selected = draft.pickupIntervalDays == interval,
                                    onClick = { viewModel.updateCurrentMedicationDraft(pickupIntervalDays = interval) },
                                    label = { Text("$interval dias") },
                                    leadingIcon = if (draft.pickupIntervalDays == interval) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Button to Register Another Medication
                if (draft.isStep1Complete) {
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    OutlinedButton(
                        onClick = { viewModel.addCurrentDraftAndRegisterAnother() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Adicionar Outro Medicamento para esta Receita")
                    }
                }
            }
        }
    }
}
