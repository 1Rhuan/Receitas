package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
import cc.rhuan.receitas.ui.components.wizard.OptionList
import cc.rhuan.receitas.ui.components.wizard.QuickChips
import cc.rhuan.receitas.ui.components.wizard.Toggle
import cc.rhuan.receitas.ui.components.wizard.ValueRow
import cc.rhuan.receitas.ui.components.wizard.WizardQuestionScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrescriptionWizardScreen(
    viewModel: PrescriptionWizardViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateUp()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFE8ECEF),
    ) {
        AnimatedContent(
            targetState = uiState.currentStep,
            label = "WizardStepTransition",
        ) { step ->
            when (step) {
                1 -> Step1NameScreen(uiState = uiState, viewModel = viewModel, onNavigateUp = onNavigateUp)
                2 -> Step2DosageScreen(uiState = uiState, viewModel = viewModel)
                3 -> Step3PosologyScreen(uiState = uiState, viewModel = viewModel)
                4 -> Step4SourceScreen(uiState = uiState, viewModel = viewModel)
                5 -> Step5StockScreen(uiState = uiState, viewModel = viewModel)
                6 -> if (uiState.entryMode == WizardEntryMode.STANDALONE_MEDICATION) {
                    Step6StandaloneReviewScreen(uiState = uiState, viewModel = viewModel)
                } else {
                    Step6MedicationLoopScreen(uiState = uiState, viewModel = viewModel)
                }
                7 -> Step7PrescriptionTypeScreen(uiState = uiState, viewModel = viewModel)
                8 -> Step8TitleReminderScreen(uiState = uiState, viewModel = viewModel)
                9 -> Step9DoctorScreen(uiState = uiState, viewModel = viewModel)
                10 -> Step10FinalReviewScreen(uiState = uiState, viewModel = viewModel)
                else -> Step1NameScreen(uiState = uiState, viewModel = viewModel, onNavigateUp = onNavigateUp)
            }
        }
    }
}

// Step 1: Nome do Medicamento
@Composable
private fun Step1NameScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
    onNavigateUp: () -> Unit,
) {
    WizardQuestionScreen(
        question = "Qual o nome do medicamento?",
        onBack = {
            if (uiState.currentStep > 1) viewModel.previousStep() else onNavigateUp()
        },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = uiState.currentMedicationDraft.title.isNotBlank() || uiState.medications.isNotEmpty(),
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.currentMedicationDraft.title,
                onValueChange = { viewModel.updateCurrentMedicationDraft(title = it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Plasil, Losartana, Amoxicilina...") },
                label = { Text("Nome do Medicamento *") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.currentMedicationDraft.activeIngredient,
                onValueChange = { viewModel.updateCurrentMedicationDraft(activeIngredient = it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Amoxicilina Tri-hidratada") },
                label = { Text("Princípio Ativo (Opcional)") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                ),
                singleLine = true,
            )

            if (uiState.availableMedications.isNotEmpty()) {
                val suggestions = uiState.availableMedications.filter {
                    uiState.currentMedicationDraft.title.isNotBlank() &&
                        it.title.contains(uiState.currentMedicationDraft.title, ignoreCase = true)
                }.take(3)

                if (suggestions.isNotEmpty()) {
                    Text(
                        text = "Medicamentos cadastrados sugeridos:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF6C7280),
                    )
                    suggestions.forEach { med ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectExistingMedication(med) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                        ) {
                            Text(
                                text = "${med.title} — ${med.dosage}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF1E293B),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Step 2: Dosagem e Forma Farmacêutica
@Composable
private fun Step2DosageScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    val commonValues = listOf("10 mg", "20 mg", "50 mg", "100 mg", "500 mg")

    WizardQuestionScreen(
        contextLabel = uiState.currentMedicationDraft.title.ifBlank { null },
        question = "Qual é a dosagem e forma do medicamento?",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = true,
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = uiState.currentMedicationDraft.dosageOverride,
                onValueChange = { viewModel.updateCurrentMedicationDraft(dosageOverride = it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Digite a dosagem aqui...") },
                label = { Text("Dosagem / Concentração") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
            )

            QuickChips(
                options = commonValues,
                selectedOption = uiState.currentMedicationDraft.dosageOverride,
                onOptionSelected = { value ->
                    viewModel.updateCurrentMedicationDraft(dosageOverride = value)
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Forma Farmacêutica:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF6C7280),
            )

            val formOptions = PharmaceuticalForm.entries.map { it.label }
            OptionList(
                options = formOptions,
                selectedOption = uiState.currentMedicationDraft.form.label,
                onOptionSelected = { label ->
                    val selectedForm = PharmaceuticalForm.fromLabel(label)
                    viewModel.updateCurrentMedicationDraft(form = selectedForm)
                },
            )
        }
    }
}

// Step 3: Posologia Prescrita
@Composable
private fun Step3PosologyScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    WizardStepPosology(
        uiState = uiState,
        viewModel = viewModel
    )
}

// Step 4: Origem e Retirada Recorrente
@Composable
private fun Step4SourceScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    var showPickupDetails by remember { mutableStateOf(uiState.currentMedicationDraft.hasRecurringPickup) }
    val context = LocalContext.current

    val datePickerDialog = remember(context, uiState.currentMedicationDraft.lastPickupDate) {
        val initialDate = uiState.currentMedicationDraft.lastPickupDate ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.updateCurrentMedicationDraft(lastPickupDate = date)
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        )
    }

    WizardQuestionScreen(
        contextLabel = uiState.currentMedicationDraft.title.ifBlank { null },
        question = "Onde você obtém este medicamento?",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = true,
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val sources = MedicationSource.entries.map { it.label }
            OptionList(
                options = sources,
                selectedOption = uiState.currentMedicationDraft.source.label,
                onOptionSelected = { label ->
                    val src = MedicationSource.fromLabel(label)
                    viewModel.updateCurrentMedicationDraft(source = src)
                },
            )

            Toggle(
                label = "Possui retirada recorrente no posto/farmácia?",
                checked = showPickupDetails,
                onCheckedChange = { checked ->
                    showPickupDetails = checked
                    viewModel.updateCurrentMedicationDraft(hasRecurringPickup = checked)
                },
            )

            if (showPickupDetails) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ValueRow.Plain(
                            label = "Última retirada",
                            value = uiState.currentMedicationDraft.lastPickupDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Selecione data",
                            onClick = { datePickerDialog.show() },
                        )

                        OutlinedTextField(
                            value = (uiState.currentMedicationDraft.pickupIntervalDays ?: 30).toString(),
                            onValueChange = { daysStr ->
                                val days = daysStr.toIntOrNull() ?: 30
                                viewModel.updateCurrentMedicationDraft(pickupIntervalDays = days)
                            },
                            label = { Text("Intervalo de retirada (dias)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
            }
        }
    }
}

// Step 5: Estoque
@Composable
private fun Step5StockScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    WizardQuestionScreen(
        contextLabel = uiState.currentMedicationDraft.title.ifBlank { null },
        question = "Qual é o seu estoque atual deste medicamento?",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = true,
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = (uiState.currentMedicationDraft.currentStock ?: 0).toString(),
                onValueChange = { text ->
                    val stock = text.toIntOrNull() ?: 0
                    viewModel.updateCurrentMedicationDraft(currentStock = stock)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Quantidade em estoque (unidades)") },
                placeholder = { Text("Ex: 30") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.currentMedicationDraft.availabilityNote,
                onValueChange = { text ->
                    viewModel.updateCurrentMedicationDraft(availabilityNote = text)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observações adicionais (opcional)") },
                placeholder = { Text("Ex: Tomar com água, armazenar na geladeira...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                minLines = 2,
            )
        }
    }
}

// Step 6: Standalone Review Screen (Modo Medicamento Isolado)
@Composable
private fun Step6StandaloneReviewScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    WizardQuestionScreen(
        question = "Confirme os dados do medicamento",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = if (uiState.isLoading) "Salvando..." else "Salvar Medicamento",
        primaryButtonEnabled = !uiState.isLoading && uiState.canSave,
        onPrimaryClick = { viewModel.savePrescription() },
    ) {
        val draft = uiState.currentMedicationDraft
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = draft.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (draft.activeIngredient.isNotBlank()) {
                    Text(text = "Princípio Ativo: ${draft.activeIngredient}", style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "Dosagem: ${draft.dosage}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Forma: ${draft.form.label}", style = MaterialTheme.typography.bodyMedium)
                if (draft.prescribedPosology.isNotBlank()) {
                    Text(text = "Posologia: ${draft.prescribedPosology}", style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "Origem: ${draft.source.label}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Estoque Inicial: ${draft.currentStock ?: 0} un", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Step 6: Medication Loop Screen (Modo Receita Completa - Adicionar mais remédios)
@Composable
private fun Step6MedicationLoopScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    var showDialog by remember { mutableStateOf(false) }

    WizardQuestionScreen(
        question = "Você possui mais algum medicamento nesta mesma receita?",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Continuar para Dados da Receita",
        primaryButtonEnabled = true,
        onPrimaryClick = {
            if (uiState.currentMedicationDraft.isStep1Complete) {
                viewModel.addCurrentDraftAndRegisterAnother()
            }
            viewModel.nextStep()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val totalMeds = uiState.medications.size + if (uiState.currentMedicationDraft.isStep1Complete) 1 else 0

            Text(
                text = "$totalMeds medicamento(s) cadastrado(s) até agora.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Button(
                onClick = {
                    viewModel.addCurrentDraftAndRegisterAnother()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "+ Adicionar outro medicamento")
            }
        }
    }
}

// Step 7: Tipo de Receita
@Composable
private fun Step7PrescriptionTypeScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    WizardQuestionScreen(
        question = "Qual é o tipo desta receita?",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = true,
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrescriptionType.entries.forEach { pType ->
                val isSelected = uiState.type == pType
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updatePrescriptionType(pType) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.updatePrescriptionType(pType) },
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            Text(
                                text = pType.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = pType.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Step 8: Título e Lembrete de Renovação
@Composable
private fun Step8TitleReminderScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    val context = LocalContext.current
    val datePickerDialog = remember(context, uiState.issueDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.updateIssueDate(date)
            },
            uiState.issueDate.year,
            uiState.issueDate.monthValue - 1,
            uiState.issueDate.dayOfMonth,
        )
    }

    WizardQuestionScreen(
        question = "Identificação e Emissão da Receita",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = true,
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.prescriptionTitle,
                onValueChange = { viewModel.updatePrescriptionTitle(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título / Apelido da Receita") },
                placeholder = { Text("Ex: Receita Cardiologista Dr. Carlos") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
            )

            ValueRow.Plain(
                label = "Data de emissão",
                value = uiState.issueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onClick = { datePickerDialog.show() },
            )
        }
    }
}

// Step 9: Médico Responsável
@Composable
private fun Step9DoctorScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    WizardQuestionScreen(
        question = "Quem é o médico emissor desta receita?",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = "Próximo",
        primaryButtonEnabled = true,
        onPrimaryClick = { viewModel.nextStep() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.doctorName,
                onValueChange = { viewModel.updateDoctorName(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do Médico") },
                placeholder = { Text("Ex: Dr. Carlos Eduardo") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.doctorRegistration,
                onValueChange = { viewModel.updateDoctorRegistration(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("CRM / Registro (Opcional)") },
                placeholder = { Text("Ex: CRM/SP 123456") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.doctorSpecialty,
                onValueChange = { viewModel.updateDoctorSpecialty(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Especialidade (Opcional)") },
                placeholder = { Text("Ex: Cardiologia, Pediatria...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
            )
        }
    }
}

// Step 10: Revisão Final e Salvamento
@Composable
private fun Step10FinalReviewScreen(
    uiState: PrescriptionWizardUiState,
    viewModel: PrescriptionWizardViewModel,
) {
    WizardQuestionScreen(
        question = "Revisão e Confirmação da Receita",
        onBack = { viewModel.previousStep() },
        progress = uiState.progress,
        primaryButtonText = if (uiState.isLoading) "Salvando..." else "Salvar Receita",
        primaryButtonEnabled = !uiState.isLoading && uiState.canSave,
        onPrimaryClick = { viewModel.savePrescription() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = uiState.prescriptionTitle.ifBlank { "Receita Médica" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = "Tipo: ${uiState.type?.label ?: "Uso Geral"}", style = MaterialTheme.typography.bodyMedium)
                    if (uiState.doctorName.isNotBlank()) {
                        Text(text = "Médico: ${uiState.doctorName} (${uiState.doctorRegistration})", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "Data de emissão: ${uiState.issueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            WizardStepReview(uiState = uiState, viewModel = viewModel)
        }
    }
}
