package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

@Composable
fun WizardStepReview(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val allMedications = if (uiState.currentMedicationDraft.isStep1Complete && !uiState.medications.contains(uiState.currentMedicationDraft)) {
        uiState.medications + uiState.currentMedicationDraft
    } else uiState.medications

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
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "6. Revisão & Foto da Receita",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Tudo pronto! Confira antes de salvar.",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Card 1: Medications Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Medicamentos (${allMedications.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = { viewModel.goToStep(1) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Medicamentos")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (allMedications.isEmpty()) {
                    Text("Nenhum medicamento adicionado.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                } else {
                    allMedications.forEach { med ->
                        Text(
                            text = "• ${med.title} (${med.dosage}) ${if (med.prescribedPosology.isNotBlank()) "- " + med.prescribedPosology else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Card 2: Prescription Details Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receita: ${uiState.prescriptionTitle.ifBlank { "Sem título" }}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = { viewModel.goToStep(4) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Receita")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (uiState.isStep4Skipped || uiState.type == null) {
                    Text("Tipo de Receita: Não especificado (Etapa pulada)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                } else {
                    Text("Tipo: ${uiState.type.label}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
                    Text("Emissão: ${uiState.issueDate.format(dateFormatter)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
                    uiState.validityDays?.let { days ->
                        Text(
                            text = "Validade: $days dias (Vencimento: ${uiState.issueDate.plusDays(days.toLong()).format(dateFormatter)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }

        // Card 3: Doctor Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profissional Prescritor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = { viewModel.goToStep(5) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Médico")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (uiState.doctorName.isNotBlank()) {
                    Text("Médico: ${uiState.doctorName}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                    if (uiState.doctorRegistration.isNotBlank()) Text("Registro: ${uiState.doctorRegistration}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                    if (uiState.doctorSpecialty.isNotBlank()) Text("Especialidade: ${uiState.doctorSpecialty}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                    if (uiState.healthFacility.isNotBlank()) Text("Local: ${uiState.healthFacility}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                } else {
                    Text("Nenhum médico informado.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
            }
        }

        // Card 4: Photo and Notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Observações do Médico & Anexo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B)
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Observações ou Instruções do Médico") },
                    placeholder = { Text("Ex: Tomar após as refeições, evitar sol...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedButton(
                    onClick = {
                        // Photo attachment trigger placeholder
                        if (uiState.photoUri == null) {
                            viewModel.updatePhotoUri("content://dummy_photo")
                        } else {
                            viewModel.updatePhotoUri(null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.photoUri == null) "Anexar Foto da Receita (Opcional)" else "Foto Anexada ✓ (Clique para remover)")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = { viewModel.savePrescription() },
            enabled = uiState.canSave && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uiState.isLoading) "Salvando..." else "Salvar Receita",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
