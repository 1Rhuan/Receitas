package cc.rhuan.receitas.ui.feature.prescriptions.wizard

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.domain.model.PharmaceuticalForm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WizardStepMedication(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    modifier: Modifier = Modifier
) {
    val draft = uiState.currentMedicationDraft

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
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "1. O Medicamento",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Qual medicamento consta na sua receita?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // List of already added medications (if any)
        if (uiState.medications.isNotEmpty()) {
            Text(
                text = "MEDICAMENTOS NATA RECEITA (${uiState.medications.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            uiState.medications.forEachIndexed { index, med ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = med.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${med.dosage} • ${med.form.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.removeMedication(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Main Form Card
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
                // 1. Medication Name
                Text(
                    text = "Nome do Medicamento *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                var expandedMedSearch by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedMedSearch && uiState.availableMedications.isNotEmpty(),
                    onExpandedChange = { expandedMedSearch = !expandedMedSearch }
                ) {
                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { title ->
                            viewModel.updateCurrentMedicationDraft(title = title)
                            expandedMedSearch = true
                        },
                        placeholder = { Text("Ex: Losartana, Dipirona, Amoxicilina...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    val filteredMeds = uiState.availableMedications.filter {
                        it.title.contains(draft.title, ignoreCase = true)
                    }
                    if (filteredMeds.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expandedMedSearch,
                            onDismissRequest = { expandedMedSearch = false }
                        ) {
                            filteredMeds.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.title} (${item.dosage})") },
                                    onClick = {
                                        viewModel.selectExistingMedication(item)
                                        expandedMedSearch = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Dosage Value & Unit Selector
                Text(
                    text = "Dosagem *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Numeric value input
                    OutlinedTextField(
                        value = draft.dosageValue,
                        onValueChange = { valStr ->
                            viewModel.updateCurrentMedicationDraft(dosageValue = valStr, dosageOverride = "")
                        },
                        placeholder = { Text("50") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Unit Selector Chips
                    val units = listOf("mg", "g", "ml", "mcg", "gotas", "UI", "sachê")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        units.forEach { unit ->
                            FilterChip(
                                selected = draft.dosageUnit.equals(unit, ignoreCase = true),
                                onClick = {
                                    viewModel.updateCurrentMedicationDraft(dosageUnit = unit, dosageOverride = "")
                                },
                                label = { Text(unit, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Quick Dosage Value Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Valores de dosagem comuns:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("10", "20", "50", "100", "500", "1000", "5").forEach { preset ->
                            FilterChip(
                                selected = draft.dosageValue == preset,
                                onClick = {
                                    viewModel.updateCurrentMedicationDraft(dosageValue = preset, dosageOverride = "")
                                },
                                label = { Text(preset) }
                            )
                        }
                    }
                }

                // 3. Pharmaceutical Form
                Text(
                    text = "Forma Farmacêutica",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PharmaceuticalForm.entries.forEach { form ->
                        val isSelected = draft.form == form
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateCurrentMedicationDraft(form = form)
                            },
                            label = { Text(form.label) },
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
            }
        }
    }
}
