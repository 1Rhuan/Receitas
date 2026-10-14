package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.material3.MaterialTheme

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.components.wizard.WizardQuestionScreen
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PrescriptionWizardScreen(
    viewModel: PrescriptionWizardViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onViewSaved: (String?) -> Unit = { onNavigateUp() },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    val close = {
        if (!state.isLoading) {
            if (state.hasUnsavedChanges && !state.isSaved) showExitConfirmation = true else onNavigateUp()
        }
    }
    val back = {
        if (!state.isLoading) {
            if (state.isSaved || state.currentStep == 1) close() else viewModel.previousStep()
        }
    }
    BackHandler { back() }
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Sair sem salvar?") },
            text = { Text("Os dados preenchidos neste cadastro serão descartados.") },
            confirmButton = { TextButton(onClick = { showExitConfirmation = false; onNavigateUp() }) { Text("Descartar e sair") } },
            dismissButton = { TextButton(onClick = { showExitConfirmation = false }) { Text("Continuar cadastro") } },
        )
    }
    PrescriptionWizardContent(
        state, WizardCallbacks(
            update = viewModel::updateForm, next = viewModel::nextStep, back = back, close = close,
            save = viewModel::savePrescription, commitMedication = { viewModel.commitMedicationDraft() },
            addMedication = viewModel::startNewMedication, editMedication = viewModel::editMedication,
            removeMedication = viewModel::removeMedication, cancelMedication = viewModel::cancelMedicationEditor,
            retry = viewModel::retryInitialLoad, goToStep = viewModel::goToStep, complete = { onViewSaved(state.savedPrescriptionId) },
        ), modifier,
    )
}

internal data class WizardCallbacks(
    val update: ((PrescriptionWizardUiState) -> PrescriptionWizardUiState) -> Unit = {},
    val next: () -> Unit = {}, val back: () -> Unit = {}, val close: () -> Unit = {},
    val retry: () -> Unit = {},
    val save: () -> Unit = {}, val commitMedication: () -> Unit = {}, val addMedication: () -> Unit = {},
    val editMedication: (Int) -> Unit = {}, val removeMedication: (Int) -> Unit = {},
    val cancelMedication: () -> Unit = {}, val goToStep: (Int) -> Unit = {}, val complete: () -> Unit = {},
)

@Composable
internal fun PrescriptionWizardContent(state: PrescriptionWizardUiState, actions: WizardCallbacks, modifier: Modifier = Modifier) {
    val standalone = state.entryMode == WizardEntryMode.STANDALONE_MEDICATION
    val title = when {
        state.isSaved -> if (standalone) "Medicamento cadastrado" else if (state.editingPrescriptionId != null) "Receita atualizada" else "Receita cadastrada"
        state.currentStep == state.medicationStep -> "Quais medicamentos estão na receita?".let { if (standalone) "Qual medicamento deseja acompanhar?" else it }
        state.currentStep == state.pickupStep -> "Quando você retira os medicamentos?"
        state.currentStep == state.maxSteps -> "Confira antes de salvar"
        else -> "Vamos cadastrar sua receita"
    }
    val description = when {
        state.isSaved -> "Tudo pronto. Confira abaixo os lembretes configurados."
        state.currentStep == state.medicationStep -> "Informe o nome. A concentração e os outros detalhes são opcionais."
        state.currentStep == state.pickupStep -> "Ative o acompanhamento somente para medicamentos com retirada recorrente."
        state.currentStep == state.maxSteps -> "Use Editar para corrigir qualquer informação e retornar a esta revisão."
        else -> "Informe a emissão e a validade para acompanhar o vencimento e a renovação."
    }
    key(state.currentStep, state.isSaved) {
        WizardQuestionScreen(
            question = title, description = description,
            contextLabel = if (standalone) "Cadastro de medicamento" else if (state.editingPrescriptionId != null) "Editar receita" else "Nova receita",
            onBack = actions.back, onClose = actions.close, progress = if (state.isSaved) 1f else state.progress,
            stepNumber = if (state.isSaved) null else state.currentStep, stepCount = state.maxSteps,
            primaryButtonText = when {
                state.isSaved -> if (standalone) "Concluir" else "Ver receita"
                state.isLoading && state.currentStep == state.maxSteps -> "Salvando…"
                state.currentStep == state.maxSteps -> if (standalone) "Salvar medicamento" else if (state.editingPrescriptionId != null) "Salvar alterações" else "Salvar receita"
                state.reviewEditStep != null -> "Voltar à revisão"
                state.currentStep == state.pickupStep -> "Revisar cadastro"
                else -> "Continuar"
            },
            primaryButtonEnabled = !state.isLoading && !state.initialLoadFailed,
            onPrimaryClick = when { state.isSaved -> actions.complete; state.currentStep == state.maxSteps -> actions.save; else -> actions.next },
            isBusy = state.isLoading, errorMessage = state.errorMessage, modifier = modifier,
        ) {
            if (state.initialLoadFailed) {
                Button(onClick = actions.retry, modifier = Modifier.fillMaxWidth()) { Text("Tentar novamente") }
            } else if (state.isLoading && state.currentStep != state.maxSteps) {
                CircularProgressIndicator()
            } else if (state.isSaved) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Cadastro concluído", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Summary(state, actions, editable = false)
                Text("Você pode consultar e atualizar estes dados pelo aplicativo.", style = MaterialTheme.typography.bodyMedium)
            } else when (state.currentStep) {
                state.medicationStep -> MedicationSection(state, actions)
                state.pickupStep -> PickupSection(state, actions)
                state.maxSteps -> {
                    Summary(state, actions, editable = true)
                    if (!standalone) Field(state.notes, { value -> actions.update { it.copy(notes = value) } }, "Observações (opcional)", singleLine = false)
                }
                else -> RecipeSection(state, actions)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Section("Identificação") {
        Field(state.prescriptionTitle, { value -> actions.update { it.copy(prescriptionTitle = value, isTitleCustomized = value.isNotBlank()) } }, "Nome da receita (opcional)", "Ex.: Receita de acompanhamento")
        Text("Se deixar em branco, usaremos o primeiro medicamento e a data de emissão.", style = MaterialTheme.typography.bodySmall)
        DateField("Data de emissão", state.issueDate) { date -> actions.update { it.copy(issueDate = date) } }
        Text("Tipo de receita", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val commonTypes = listOf(PrescriptionType.CONTINUO, PrescriptionType.ANTIBIOTICO, PrescriptionType.CONTROLADO, PrescriptionType.FARMACIA_POPULAR, PrescriptionType.SUS_BASIC)
            val types = if (state.type != null && state.type !in commonTypes) commonTypes + state.type else commonTypes
            types.forEach { type ->
                FilterChip(selected = state.type == type, onClick = { actions.update { it.copy(type = type, validityDays = type.defaultValidityDays, validityDaysInput = type.defaultValidityDays.toString(), renewalReminderDaysBefore = it.renewalReminderDaysBefore.coerceAtMost(type.defaultValidityDays)) } }, label = { Text(type.label) })
            }
        }
        NumericField(state.validityDaysInput, { value -> actions.update { it.copy(validityDaysInput = value, validityDays = value.toIntOrNull()) } }, "Validade em dias", state.validityDays == null || state.validityDays !in 1..3650)
        Text("Confira o prazo indicado na receita ou informado pela unidade de retirada.", style = MaterialTheme.typography.bodySmall)
        state.validityDays?.takeIf { it in 1..3650 }?.let { days ->
            Text("Vencimento: " + dateText(state.issueDate.plusDays(days.toLong())), fontWeight = FontWeight.SemiBold)
        }
    }
    Section("Lembrete de renovação") {
        ToggleRow("Receber alertas de validade e renovação", state.renewalReminderEnabled) { enabled -> actions.update { it.copy(renewalReminderEnabled = enabled) } }
        if (state.renewalReminderEnabled) {
            DayOptions(state.renewalReminderDaysBefore, listOf(0, 3, 5, 7, 10, 15).filter { it <= (state.validityDays ?: 180) }) { days -> actions.update { it.copy(renewalReminderDaysBefore = days) } }
        } else Text("Os alertas desta receita estarão desativados.", style = MaterialTheme.typography.bodySmall)
    }
    var doctorExpanded by rememberSaveable { mutableStateOf(state.doctorName.isNotBlank()) }
    Section("Profissional (opcional)") {
        TextButton(onClick = { doctorExpanded = !doctorExpanded }) { Text(if (doctorExpanded) "Ocultar dados do profissional" else "Adicionar dados do profissional") }
        if (doctorExpanded) {
            Field(state.doctorName, { value -> actions.update { it.copy(doctorName = value, selectedDoctorId = null) } }, "Nome do profissional")
            state.availableDoctors.filter { state.doctorName.isNotBlank() && it.name.contains(state.doctorName, true) }.take(3).forEach { doctor ->
                OutlinedButton(onClick = { actions.update { it.copy(selectedDoctorId = doctor.id, doctorName = doctor.name, doctorRegistration = doctor.registration ?: "", doctorSpecialty = doctor.specialty ?: "", healthFacility = doctor.healthFacility ?: "", doctorPhone = doctor.phone ?: "") } }, modifier = Modifier.fillMaxWidth()) { Text(doctor.name) }
            }
            Field(state.doctorRegistration, { value -> actions.update { it.copy(doctorRegistration = value, selectedDoctorId = null) } }, "CRM / registro (opcional)")
            Field(state.doctorSpecialty, { value -> actions.update { it.copy(doctorSpecialty = value) } }, "Especialidade (opcional)")
        }
    }
}

@Composable
private fun MedicationSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        if (index != state.editingMedicationIndex || !state.showMedicationEditor) {
            Section(medication.title) {
                Text(medication.dosage.ifBlank { "Concentração não informada" }, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { actions.editMedication(index) }) { Text("Editar") }
                    TextButton(onClick = { actions.removeMedication(index) }) { Text("Remover") }
                }
            }
        }
    }
    if (state.showMedicationEditor) {
        val draft = state.currentMedicationDraft
        Section(if (state.editingMedicationIndex != null) "Editar medicamento" else "Adicionar medicamento") {
            Field(draft.title, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = value)) } }, "Nome do medicamento *", "Ex.: Losartana")
            if (state.editingMedicationIndex == null) {
                state.availableMedications.filter { draft.title.isNotBlank() && it.title.contains(draft.title, true) && state.medications.none { existing -> existing.id == it.id } }.take(3).forEach { medication ->
                    OutlinedButton(onClick = {
                        actions.update { it.copy(currentMedicationDraft = MedicationDraft(id = medication.id, title = medication.title, activeIngredient = medication.activeIngredient ?: "", dosageOverride = medication.dosage, form = medication.form, prescribedPosology = medication.prescribedPosology, source = medication.source, sourceName = medication.sourceName ?: "", hasRecurringPickup = medication.hasRecurringPickup, lastPickupDate = medication.lastRefillDate, pickupIntervalDays = medication.refillCycleDays, pickupIntervalInput = medication.refillCycleDays.toString(), pickupReminderDaysBefore = medication.pickupReminderDaysBefore, replenishmentQuantity = medication.quantity, currentStock = medication.stockQuantity, minStockThreshold = medication.minStockThreshold, availabilityNote = medication.instructions ?: "")) }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Usar cadastrado: " + medication.title + " " + medication.dosage) }
                }
            }
            Field(draft.dosage, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(dosageOverride = value)) } }, "Concentração (opcional)", "Ex.: 50 mg")
            var moreDetails by rememberSaveable(draft.id) { mutableStateOf(false) }
            TextButton(onClick = { moreDetails = !moreDetails }) { Text(if (moreDetails) "Ocultar detalhes" else "Mais detalhes (opcional)") }
            if (moreDetails) {
                Field(draft.activeIngredient, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(activeIngredient = value)) } }, "Princípio ativo")
                Field(draft.prescribedPosology, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(prescribedPosology = value)) } }, "Orientação escrita na receita", singleLine = false)
                Text("Apenas para consulta. O aplicativo acompanha validade, renovação e retirada.", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = actions.commitMedication, modifier = Modifier.fillMaxWidth()) { Text(if (state.editingMedicationIndex != null) "Concluir edição" else "Adicionar à receita") }
            if (state.medications.isNotEmpty()) TextButton(onClick = actions.cancelMedication) { Text(if (state.editingMedicationIndex != null) "Cancelar edição" else "Cancelar inclusão") }
        }
    } else {
        OutlinedButton(onClick = actions.addMedication, modifier = Modifier.fillMaxWidth()) { Text("Adicionar outro medicamento") }
        Text("Você pode continuar com " + state.medications.size + " medicamento(s).", style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickupSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        val update: ((MedicationDraft) -> MedicationDraft) -> Unit = { transform -> actions.update { current -> current.copy(medications = current.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) } }
        Section(medication.title) {
            Field(medication.replenishmentQuantity, { value -> update { it.copy(replenishmentQuantity = value) } }, "Quantidade por reposição (opcional)", "Ex.: 2 caixas ou 60 comprimidos")
            Text("Informe quanto você retira ou compra em cada reposição.", style = MaterialTheme.typography.bodySmall)
            ToggleRow("Acompanhar retirada recorrente", medication.hasRecurringPickup) { enabled -> update { it.copy(hasRecurringPickup = enabled) } }
            if (medication.hasRecurringPickup) {
                DateField("Última retirada", medication.lastPickupDate) { date -> update { it.copy(lastPickupDate = date) } }
                Text("Use a data em que você realmente retirou o medicamento.", style = MaterialTheme.typography.bodySmall)
                NumericField(medication.pickupIntervalInput, { value -> update { it.copy(pickupIntervalInput = value, pickupIntervalDays = value.toIntOrNull()) } }, "Intervalo entre retiradas (dias)", medication.pickupIntervalDays == null || medication.pickupIntervalDays !in 1..3650)
                medication.lastPickupDate?.let { date -> medication.pickupIntervalDays?.takeIf { it in 1..3650 }?.let { interval -> Text("Próxima retirada: " + dateText(date.plusDays(interval.toLong())), fontWeight = FontWeight.SemiBold) } }
                Text("Avisar com antecedência", style = MaterialTheme.typography.labelLarge)
                DayOptions(medication.pickupReminderDaysBefore, listOf(0, 1, 3, 5, 7).filter { it <= (medication.pickupIntervalDays ?: 30) }) { days -> update { it.copy(pickupReminderDaysBefore = days) } }
                Text("Onde você retira?", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedicationSource.entries.forEach { source -> FilterChip(selected = medication.source == source, onClick = { update { it.copy(source = source) } }, label = { Text(source.label) }) }
                }
                Field(medication.sourceName, { value -> update { it.copy(sourceName = value) } }, "Nome do local (opcional)", "Ex.: UBS Central")
                medication.pickupError()?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            } else Text("Sem lembretes de retirada para este medicamento.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Summary(state: PrescriptionWizardUiState, actions: WizardCallbacks, editable: Boolean) {
    if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
        Section("Receita", if (editable) ({ actions.goToStep(1) }) else null) {
            Text(state.prescriptionTitle.ifBlank { state.allMedications.firstOrNull()?.title.orEmpty() + " — " + dateText(state.issueDate) }, fontWeight = FontWeight.SemiBold)
            Text(state.type?.label ?: "Tipo não informado")
            Text("Emissão: " + dateText(state.issueDate))
            state.validityDays?.takeIf { it > 0 }?.let { days -> Text("Vencimento: " + dateText(state.issueDate.plusDays(days.toLong()))) }
            Text(if (state.renewalReminderEnabled) "Renovação: " + reminderText(state.renewalReminderDaysBefore) else "Alertas de validade e renovação desativados")
            if (state.doctorName.isNotBlank()) Text("Profissional: " + state.doctorName)
        }
    }
    Section("Medicamentos (" + state.allMedications.size + ")", if (editable) ({ actions.goToStep(state.medicationStep) }) else null) {
        state.allMedications.forEachIndexed { index, medication ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(medication.title.ifBlank { "Nome não informado" }, fontWeight = FontWeight.SemiBold)
                    if (medication.dosage.isNotBlank()) Text(medication.dosage, style = MaterialTheme.typography.bodySmall)
                }
                if (editable) TextButton(onClick = { actions.editMedication(index) }) { Text("Editar") }
            }
        }
    }
    Section("Lembretes de retirada", if (editable) ({ actions.goToStep(state.pickupStep) }) else null) {
        state.allMedications.forEach { medication ->
            Text(medication.title, fontWeight = FontWeight.SemiBold)
            if (medication.replenishmentQuantity.isNotBlank()) Text("Quantidade por reposição: " + medication.replenishmentQuantity, style = MaterialTheme.typography.bodySmall)
            if (medication.hasRecurringPickup) {
                val date = medication.lastPickupDate
                val interval = medication.pickupIntervalDays
                if (date != null && interval != null && interval > 0) Text("Próxima retirada: " + dateText(date.plusDays(interval.toLong())))
                else Text("Complete a data e o intervalo da retirada.", color = MaterialTheme.colorScheme.error)
                Text("Lembrete: " + reminderText(medication.pickupReminderDaysBefore), style = MaterialTheme.typography.bodySmall)
                if (medication.sourceName.isNotBlank()) Text("Local: " + medication.sourceName, style = MaterialTheme.typography.bodySmall)
            } else Text("Retirada recorrente desativada", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (editable && !state.canSave) Text(state.recipeError ?: state.medicationError ?: state.pickupError ?: "Confira os campos antes de salvar.", color = MaterialTheme.colorScheme.error)
}

@Composable
private fun Section(title: String, onEdit: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                onEdit?.let { TextButton(onClick = it) { Text("Editar") } }
            }
            content()
        }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "", singleLine: Boolean = true) {
    OutlinedTextField(value, onChange, label = { Text(label) }, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(), singleLine = singleLine, minLines = if (singleLine) 1 else 2)
}

@Composable
private fun NumericField(value: String, onChange: (String) -> Unit, label: String, error: Boolean = false) {
    OutlinedTextField(value, { onChange(it.filter(Char::isDigit).take(4)) }, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), isError = error)
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(12.dp))
        Switch(checked, onChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOptions(selected: Int, options: List<Int>, onSelect: (Int) -> Unit) {
    Text("Atual: " + reminderText(selected), style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { days -> FilterChip(selected = selected == days, onClick = { onSelect(days) }, label = { Text(if (days == 0) "No dia" else days.toString() + " dias antes") }) }
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, onSelect: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val initial = date ?: LocalDate.now()
        DatePickerDialog(context, { _, year, month, day -> onSelect(LocalDate.of(year, month + 1, day)) }, initial.year, initial.monthValue - 1, initial.dayOfMonth).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(date?.let(::dateText) ?: "Selecionar data", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun dateText(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
private fun reminderText(days: Int): String = if (days == 0) "no dia" else days.toString() + " dias antes"

@Preview(showBackground = true)
@Composable
private fun RegistrationPreview() { ReceitasTheme { PrescriptionWizardContent(PrescriptionWizardUiState(), WizardCallbacks()) } }

@Preview(showBackground = true)
@Composable
private fun ReviewPreview() {
    ReceitasTheme { PrescriptionWizardContent(PrescriptionWizardUiState(currentStep = 4, showMedicationEditor = false, medications = listOf(MedicationDraft(id = "preview", title = "Losartana", dosageOverride = "50 mg", hasRecurringPickup = true, lastPickupDate = LocalDate.now().minusDays(25)))), WizardCallbacks()) }
}
