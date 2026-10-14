package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import cc.rhuan.receitas.ui.components.AppCard as Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardStepDoctor(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    modifier: Modifier = Modifier
) {
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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "5. Profissional Prescritor",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Quem é o profissional que assinou a receita?",
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
                Text(
                    text = "Nome do Médico / Profissional",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Doctor Name Autocomplete
                var expandedDoctorSearch by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDoctorSearch && uiState.availableDoctors.isNotEmpty(),
                    onExpandedChange = { expandedDoctorSearch = !expandedDoctorSearch }
                ) {
                    OutlinedTextField(
                        value = uiState.doctorName,
                        onValueChange = { name ->
                            viewModel.updateDoctorName(name)
                            expandedDoctorSearch = true
                        },
                        placeholder = { Text("Ex: Dr. Carlos Eduardo, Dra. Ana Paula...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    val filteredDoctors = uiState.availableDoctors.filter {
                        it.name.contains(uiState.doctorName, ignoreCase = true)
                    }
                    if (filteredDoctors.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expandedDoctorSearch,
                            onDismissRequest = { expandedDoctorSearch = false }
                        ) {
                            filteredDoctors.forEach { doc ->
                                DropdownMenuItem(
                                    text = { Text("${doc.name} (${doc.specialty ?: "Geral"})") },
                                    onClick = {
                                        viewModel.selectDoctor(doc)
                                        expandedDoctorSearch = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Discrete link to toggle extra doctor details
                TextButton(
                    onClick = { viewModel.toggleExtraDoctorFields() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = if (uiState.showExtraDoctorFields) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.showExtraDoctorFields)
                            "Ocultar detalhes extras"
                        else
                            "+ Adicionar mais detalhes (CRM, especialidade, local, telefone)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // Expanded Extra Fields
                AnimatedVisibility(visible = uiState.showExtraDoctorFields) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.doctorSpecialty,
                                onValueChange = viewModel::updateDoctorSpecialty,
                                label = { Text("Especialidade") },
                                placeholder = { Text("Cardiologia") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = uiState.doctorRegistration,
                                onValueChange = viewModel::updateDoctorRegistration,
                                label = { Text("CRM / Registro") },
                                placeholder = { Text("CRM/SP 123456") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = uiState.healthFacility,
                            onValueChange = viewModel::updateHealthFacility,
                            label = { Text("Local de Atendimento / Unidade de Saúde") },
                            placeholder = { Text("Ex: UBS Vila Mariana") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.doctorPhone,
                            onValueChange = viewModel::updateDoctorPhone,
                            label = { Text("Telefone / Contato do Consultório") },
                            placeholder = { Text("Ex: (11) 98765-4321") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
