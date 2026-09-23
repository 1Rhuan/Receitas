package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardStepPosology(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    modifier: Modifier = Modifier
) {
    val draft = uiState.currentMedicationDraft
    val context = LocalContext.current
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "2. Posologia & Horários",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "Qual a forma de tomar $medName?",
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
                // 1. Dose por tomada
                Text(
                    text = "Dose por tomada *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                OutlinedTextField(
                    value = draft.doseAmount,
                    onValueChange = { viewModel.updateCurrentMedicationDraft(doseAmount = it) },
                    placeholder = { Text("Ex: 1 comprimido, 10 gotas, 5 ml...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dose Amount Quick Chips
                val dosePresets = listOf("1 comprimido", "2 comprimidos", "10 gotas", "5 ml", "1 cápsula", "1 sachê")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dosePresets.forEach { preset ->
                        FilterChip(
                            selected = draft.doseAmount == preset,
                            onClick = { viewModel.updateCurrentMedicationDraft(doseAmount = preset) },
                            label = { Text(preset) }
                        )
                    }
                }

                // 2. Frequência / Intervalo
                Text(
                    text = "Frequência / Intervalo *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                val frequencyOptions = listOf(
                    8 to "De 8 em 8h (3x/dia)",
                    12 to "De 12 em 12h (2x/dia)",
                    24 to "1x ao dia (24h)",
                    6 to "De 6 em 6h (4x/dia)",
                    4 to "De 4 em 4h (6x/dia)"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    frequencyOptions.forEach { (hours, label) ->
                        val isSelected = draft.frequencyHours == hours
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCurrentMedicationDraft(frequencyHours = hours) },
                            label = { Text(label) },
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

                // 3. Duração do Tratamento
                Text(
                    text = "Duração do Tratamento (dias)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                OutlinedTextField(
                    value = draft.durationDays.toString(),
                    onValueChange = { str ->
                        str.toIntOrNull()?.let { viewModel.updateCurrentMedicationDraft(durationDays = it) }
                    },
                    placeholder = { Text("Ex: 30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val durationPresets = listOf(
                    7 to "7 dias",
                    14 to "14 dias",
                    30 to "30 dias",
                    60 to "60 dias",
                    180 to "Uso contínuo (180d)"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    durationPresets.forEach { (days, label) ->
                        FilterChip(
                            selected = draft.durationDays == days,
                            onClick = { viewModel.updateCurrentMedicationDraft(durationDays = days) },
                            label = { Text(label) }
                        )
                    }
                }

                // 4. Horário da Primeira Dose (Alimenta o Pill Reminder)
                Text(
                    text = "Horário da Primeira Dose (Alarme do Lembrador)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                val formattedTime = draft.firstDoseTime.format(DateTimeFormatter.ofPattern("HH:mm"))

                OutlinedButton(
                    onClick = {
                        val dialog = TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.updateCurrentMedicationDraft(firstDoseTime = LocalTime.of(hour, minute))
                            },
                            draft.firstDoseTime.hour,
                            draft.firstDoseTime.minute,
                            true
                        )
                        dialog.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Horário Selecionado: $formattedTime", fontWeight = FontWeight.Bold)
                }

                // Quick time preset chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        LocalTime.of(7, 0) to "07:00",
                        LocalTime.of(8, 0) to "08:00",
                        LocalTime.of(12, 0) to "12:00",
                        LocalTime.of(18, 0) to "18:00",
                        LocalTime.of(22, 0) to "22:00"
                    ).forEach { (time, label) ->
                        FilterChip(
                            selected = draft.firstDoseTime == time,
                            onClick = { viewModel.updateCurrentMedicationDraft(firstDoseTime = time) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}
