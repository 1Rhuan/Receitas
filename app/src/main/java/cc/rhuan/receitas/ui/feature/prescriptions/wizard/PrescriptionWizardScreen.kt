package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.ui.res.pluralStringResource

import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.components.FormSection as Section
import cc.rhuan.receitas.ui.components.FormChoice
import cc.rhuan.receitas.ui.components.FormField
import cc.rhuan.receitas.ui.components.FormToggle as ToggleRow
import cc.rhuan.receitas.ui.components.OptionalFormSection
import cc.rhuan.receitas.ui.components.SecondaryActionButton
import cc.rhuan.receitas.ui.theme.DesignTokens as T

import androidx.compose.ui.res.stringResource

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
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.components.wizard.WizardQuestionScreen
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun PrescriptionWizardPresentation(
    state: PrescriptionWizardUiState,
    actions: WizardCallbacks,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    val close = {
        if (!state.isLoading) {
            if (state.hasUnsavedChanges && !state.isSaved) showExitConfirmation = true else onNavigateUp()
        }
    }
    val back = {
        if (!state.isLoading) {
            if (state.isSaved || state.currentStep == 1) close() else actions.back()
        }
    }
    BackHandler { back() }
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text(stringResource(R.string.ui_sair_sem_salvar)) },
            text = { Text(stringResource(R.string.ui_os_dados_preenchidos_neste_cadastro_serao_descartados)) },
            confirmButton = { TextButton(onClick = { showExitConfirmation = false; onNavigateUp() }) { Text(stringResource(R.string.ui_descartar_e_sair)) } },
            dismissButton = { TextButton(onClick = { showExitConfirmation = false }) { Text(stringResource(R.string.ui_continuar_cadastro)) } },
        )
    }
    PrescriptionWizardContent(
        state, actions.copy(back = back, close = close), modifier,
    )
}

@Composable
internal fun PrescriptionWizardContent(state: PrescriptionWizardUiState, actions: WizardCallbacks, modifier: Modifier = Modifier) {
    val standalone = state.entryMode == WizardEntryMode.STANDALONE_MEDICATION
    val title = when {
        state.isSaved -> if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.ui_medicamento_atualizado) else stringResource(R.string.ui_medicamento_cadastrado) } else if (state.editingPrescriptionId != null) stringResource(R.string.ui_receita_atualizada) else stringResource(R.string.ui_receita_cadastrada)
        state.currentStep == state.medicationStep -> stringResource(R.string.ui_quais_medicamentos_estao_na_receita).let { if (standalone) stringResource(R.string.ui_qual_medicamento_deseja_acompanhar) else it }
        state.currentStep == state.pickupOptionsStep -> stringResource(R.string.design_pickup_options_title)
        state.currentStep == state.pickupStep -> stringResource(R.string.ui_quando_voce_retira_os_medicamentos)
        state.currentStep == state.maxSteps -> stringResource(R.string.ui_confira_antes_de_salvar)
        else -> if (state.previousPrescriptionId != null) stringResource(R.string.ui_vamos_registrar_sua_nova_receita) else stringResource(R.string.ui_vamos_cadastrar_sua_receita)
    }
    val description = when {
        state.isSaved -> stringResource(R.string.ui_tudo_pronto_confira_abaixo_os_lembretes_configurados)
        state.currentStep == state.medicationStep -> stringResource(if (standalone) R.string.design_standalone_help else R.string.design_medication_help)
        state.currentStep == state.pickupOptionsStep -> stringResource(R.string.design_pickup_options_help)
        state.currentStep == state.pickupStep -> stringResource(R.string.design_pickup_help)
        state.currentStep == state.maxSteps -> stringResource(R.string.ui_toque_em_editar_para_corrigir_uma_informacao_e_voltar_a_revisao)
        else -> stringResource(R.string.ui_informe_a_emissao_e_a_validade_para_acompanhar_o_vencimento_e_a_renovacao)
    }
    key(state.currentStep, state.isSaved) {
        WizardQuestionScreen(
            question = title, description = description,
            prominentHeader = !standalone && state.currentStep == 1 && !state.isSaved,
            contextLabel = if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.ui_editar_medicamento) else stringResource(R.string.ui_cadastro_de_medicamento) } else if (state.editingPrescriptionId != null) stringResource(R.string.ui_editar_receita) else stringResource(R.string.action_new_prescription),
            onBack = actions.back, onClose = actions.close, progress = if (state.isSaved) 1f else state.progress,
            stepNumber = if (state.isSaved) null else state.currentStep, stepCount = state.maxSteps,
            primaryButtonText = when {
                state.isSaved -> if (standalone) stringResource(R.string.action_finish) else stringResource(R.string.ui_ver_receita)
                state.isLoading && state.currentStep == state.maxSteps -> stringResource(R.string.ui_salvando)
                state.currentStep == state.maxSteps -> if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.ui_salvar_alteracoes) else stringResource(R.string.ui_salvar_medicamento) } else if (state.editingPrescriptionId != null) stringResource(R.string.ui_salvar_alteracoes) else stringResource(R.string.ui_salvar_receita)
                state.reviewEditStep != null -> stringResource(R.string.ui_voltar_a_revisao)
                state.currentStep == state.pickupStep && state.pickupOptionsStep != null -> stringResource(R.string.design_configure_reminders)
                state.currentStep == state.pickupStep || state.currentStep == state.pickupOptionsStep -> stringResource(R.string.ui_revisar_cadastro)
                state.currentStep == state.medicationStep -> stringResource(R.string.design_continue_pickup)
                else -> stringResource(R.string.design_continue_medication)
            },
            primaryButtonEnabled = !state.isLoading && !state.initialLoadFailed,
            onPrimaryClick = when { state.isSaved -> actions.complete; state.currentStep == state.maxSteps -> actions.save; else -> actions.next },
            isBusy = state.isLoading, errorMessage = state.errorMessage, modifier = modifier,
        ) {
            if (state.initialLoadFailed) {
                Button(onClick = actions.retry, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_retry)) }
            } else if (state.isLoading && state.currentStep != state.maxSteps) {
                CircularProgressIndicator()
            } else if (state.isSaved) {
                Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.ui_cadastro_concluido), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(T.control))
                Summary(state, actions, editable = false)
                Text(stringResource(R.string.ui_voce_pode_consultar_e_atualizar_estes_dados_pelo_aplicativo), style = MaterialTheme.typography.bodyMedium)
            } else when (state.currentStep) {
                state.medicationStep -> MedicationSection(state, actions)
                state.pickupStep -> PickupSection(state, actions)
                state.pickupOptionsStep -> PickupOptionsSection(state, actions)
                state.maxSteps -> {
                    Summary(state, actions, editable = true)
                    if (!standalone) Field(state.notes, { value -> actions.update { it.copy(notes = value) } }, stringResource(R.string.ui_observacoes_opcional), singleLine = false)
                }
                else -> RecipeSection(state, actions)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Section(stringResource(R.string.design_recipe_identity)) {
        DateField(stringResource(R.string.ui_data_de_emissao), state.issueDate) { date -> actions.update { it.copy(issueDate = date) } }
        val commonTypes = listOf(PrescriptionType.CONTINUO, PrescriptionType.ANTIBIOTICO, PrescriptionType.CONTROLADO, PrescriptionType.FARMACIA_POPULAR, PrescriptionType.SUS_BASIC)
        val types = if (state.type != null && state.type !in commonTypes) commonTypes + state.type else commonTypes
        FormChoice(stringResource(R.string.ui_tipo_de_receita), state.type?.label ?: stringResource(R.string.label_unknown_prescription_type), types.map { it.label }) { actions.selectType(types[it]) }
    }
    Section(stringResource(R.string.design_recipe_validity)) {
        var chooseExpirationDate by rememberSaveable { mutableStateOf(false) }
        ToggleRow(stringResource(R.string.design_choose_expiration), chooseExpirationDate) { chooseExpirationDate = it }
        if (chooseExpirationDate) {
        DateField(stringResource(R.string.ui_vencimento_personalizado), state.validityDays?.let { state.issueDate.plusDays(it.toLong()) }, allowFuture = true) { date ->
            actions.updateExpirationDate(date)
        }
        } else {
        NumericField(state.validityDaysInput, { value -> actions.updateValidityDays(value) }, stringResource(R.string.ui_validade_em_dias), state.validityDays == null || state.validityDays !in 1..3650)
        }
        Text(stringResource(R.string.ui_confira_o_prazo_indicado_na_receita_ou_informado_pela_unidade_de_retirada), style = MaterialTheme.typography.bodySmall)
        state.validityDays?.takeIf { it in 1..3650 }?.let { days ->
            Text(stringResource(R.string.expiration_date, dateText(state.issueDate.plusDays(days.toLong()))), fontWeight = FontWeight.SemiBold)
        }
    }
    OptionalFormSection(stringResource(R.string.ui_lembrete_de_renovacao),
        summary = if (state.renewalReminderEnabled) reminderText(state.renewalReminderDaysBefore) else stringResource(R.string.ui_os_alertas_desta_receita_estarao_desativados)) {
        ToggleRow(stringResource(R.string.ui_receber_alertas_de_validade_e_renovacao), state.renewalReminderEnabled) { enabled -> actions.update { it.copy(renewalReminderEnabled = enabled) } }
        if (state.renewalReminderEnabled) {
            DayOptions(state.renewalReminderDaysBefore, listOf(0, 3, 5, 7, 10, 15).filter { it <= (state.validityDays ?: 180) }) { days -> actions.update { it.copy(renewalReminderDaysBefore = days) } }
        } else Text(stringResource(R.string.ui_os_alertas_desta_receita_estarao_desativados), style = MaterialTheme.typography.bodySmall)
    }
    OptionalFormSection(stringResource(R.string.design_optional_recipe), initiallyExpanded = state.doctorName.isNotBlank() || state.prescriptionTitle.isNotBlank()) {
        Field(state.prescriptionTitle, { value -> actions.update { it.copy(prescriptionTitle = value, isTitleCustomized = value.isNotBlank()) } }, stringResource(R.string.ui_nome_da_receita_opcional), stringResource(R.string.ui_ex_receita_de_acompanhamento))
        Text(stringResource(R.string.ui_se_deixar_em_branco_usaremos_o_primeiro_medicamento_e_a_data_de_emissao), style = MaterialTheme.typography.bodySmall)
            Field(state.doctorName, { value -> actions.update { it.copy(doctorName = value, selectedDoctorId = null) } }, stringResource(R.string.ui_nome_do_profissional))
            state.availableDoctors.filter { state.doctorName.isNotBlank() && it.name.contains(state.doctorName, true) }.take(3).forEach { doctor ->
                OutlinedButton(onClick = { actions.selectDoctor(doctor) }, modifier = Modifier.fillMaxWidth()) { Text(doctor.name) }
            }
            Field(state.doctorRegistration, { value -> actions.update { it.copy(doctorRegistration = value, selectedDoctorId = null) } }, stringResource(R.string.ui_crm_ou_registro_profissional_opcional))
            Field(state.doctorSpecialty, { value -> actions.update { it.copy(doctorSpecialty = value) } }, stringResource(R.string.ui_especialidade_opcional))

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicationSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        if (index != state.editingMedicationIndex || !state.showMedicationEditor) {
            Section(medication.title) {
                Text(medication.dosage.ifBlank { stringResource(R.string.ui_concentracao_nao_informada) }, style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(T.gap)) {
                    TextButton(onClick = { actions.editMedication(index) }) { Text(stringResource(R.string.action_edit)) }
                    TextButton(onClick = { actions.removeMedication(index) }) { Text(stringResource(R.string.action_remove)) }
                }
            }
        }
    }
    if (state.showMedicationEditor) {
        val draft = state.currentMedicationDraft
        Section(if (state.editingMedicationIndex != null) stringResource(R.string.ui_editar_medicamento) else stringResource(R.string.ui_adicionar_medicamento)) {
            Field(draft.title, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = value)) } }, stringResource(R.string.ui_nome_do_medicamento), stringResource(R.string.ui_ex_losartana))
            if (state.editingMedicationIndex == null) {
                state.availableMedications.filter { draft.title.isNotBlank() && it.title.contains(draft.title, true) && state.medications.none { existing -> existing.id == it.id } }.take(3).forEach { medication ->
                    OutlinedButton(onClick = {
                        actions.selectMedication(medication)
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.existing_medication, (medication.title + " " + medication.dosage).trim())) }
                }
            }
            Field(draft.dosage, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(dosageOverride = value)) } }, stringResource(R.string.ui_concentracao_opcional), stringResource(R.string.ui_ex_50_mg))
            var moreDetails by rememberSaveable(draft.id) { mutableStateOf(false) }
            TextButton(onClick = { moreDetails = !moreDetails }) { Text(if (moreDetails) stringResource(R.string.ui_ocultar_detalhes) else stringResource(R.string.ui_mais_detalhes_opcional)) }
            if (moreDetails) {
                Text(stringResource(R.string.ui_forma_farmaceutica), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(T.small)) {
                    PharmaceuticalForm.entries.forEach { form -> FilterChip(selected = draft.form == form, onClick = { actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(form = form)) } }, label = { Text(form.label) }) }
                }
                Field(draft.activeIngredient, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(activeIngredient = value)) } }, stringResource(R.string.ui_principio_ativo))
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) Field(draft.prescribedQuantity, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(prescribedQuantity = value)) } }, stringResource(R.string.ui_quantidade_prescrita_opcional))
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) Field(draft.prescribedPosology, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(prescribedPosology = value)) } }, stringResource(R.string.ui_orientacao_escrita_na_receita), singleLine = false)
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) Text(stringResource(R.string.ui_apenas_para_consulta_o_aplicativo_acompanha_validade_renovacao_e_retirada), style = MaterialTheme.typography.bodySmall)
            }
            if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && state.editingMedicationIndex == null) {
                SecondaryActionButton(stringResource(R.string.ui_adicionar_outro_medicamento), actions.addMedication, Modifier.fillMaxWidth())
            }
            if (state.medications.isNotEmpty()) TextButton(onClick = actions.cancelMedication) { Text(if (state.editingMedicationIndex != null) stringResource(R.string.ui_cancelar_edicao) else stringResource(R.string.ui_cancelar_inclusao)) }
        }
    } else {
        OutlinedButton(onClick = actions.addMedication, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_adicionar_outro_medicamento)) }
        Text(pluralStringResource(R.plurals.registration_medication_count, state.medications.size, state.medications.size), style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickupSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        val update: ((MedicationDraft) -> MedicationDraft) -> Unit = { transform -> actions.update { current -> current.copy(medications = current.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) } }
        Section(medication.title) {
            ToggleRow(stringResource(R.string.ui_acompanhar_retirada_recorrente), medication.hasRecurringPickup) { enabled -> update { it.copy(hasRecurringPickup = enabled) } }
            if (medication.hasRecurringPickup) {
                Text(stringResource(R.string.ui_unidade_do_intervalo), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(T.small)) {
                    IntervalUnit.entries.forEach { unit -> FilterChip(selected = medication.intervalUnit == unit, onClick = { update { it.copy(intervalUnit = unit) } }, label = { Text(unit.label) }) }
                }
                if (medication.hasPickupHistory) {
                    Text(stringResource(R.string.last_pickup_date, medication.lastPickupDate?.let(::dateText).orEmpty()))
                    Text(stringResource(R.string.ui_para_registrar_ou_corrigir_uma_retirada_use_o_historico_nos_detalhes_do_med), style = MaterialTheme.typography.bodySmall)
                } else DateField(stringResource(R.string.ui_ultima_retirada), medication.lastPickupDate) { date -> update { it.copy(lastPickupDate = date, initialPickupDate = date) } }
                Text(stringResource(R.string.ui_use_a_data_em_que_voce_realmente_retirou_o_medicamento), style = MaterialTheme.typography.bodySmall)
                NumericField(medication.pickupIntervalInput, { value -> actions.updatePickupInterval(index, value) }, stringResource(R.string.ui_intervalo_entre_retiradas), medication.pickupIntervalDays == null || medication.pickupIntervalDays !in 1..3650)
                medication.lastPickupDate?.let { date -> medication.pickupIntervalDays?.takeIf { it in 1..3650 }?.let { interval -> Text(stringResource(R.string.next_pickup_date, dateText(PickupInterval(interval, medication.intervalUnit).nextAfter(date))), fontWeight = FontWeight.SemiBold) } }
                medication.copy(pickupReminderDaysBefore = 0).pickupError()?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            } else Text(stringResource(R.string.ui_sem_lembretes_de_retirada_para_este_medicamento), style = MaterialTheme.typography.bodySmall)
            Field(medication.replenishmentQuantity, { value -> update { it.copy(replenishmentQuantity = value) } }, stringResource(R.string.ui_quantidade_por_retirada_opcional), stringResource(R.string.ui_ex_2_caixas_ou_60_comprimidos))
            Text(stringResource(R.string.ui_informe_a_quantidade_que_voce_costuma_retirar_ou_comprar), style = MaterialTheme.typography.bodySmall)

        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickupOptionsSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        if (medication.hasRecurringPickup) {
            val update: ((MedicationDraft) -> MedicationDraft) -> Unit = { transform -> actions.update { current -> current.copy(medications = current.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) } }
            Section(medication.title) {
                Text(stringResource(R.string.ui_avisar_com_antecedencia), style = MaterialTheme.typography.labelLarge)
                DayOptions(medication.pickupReminderDaysBefore, listOf(0, 1, 3, 5, 7).filter { it <= medication.intervalDurationDays }) { days -> update { it.copy(pickupReminderDaysBefore = days) } }
                Text(stringResource(R.string.ui_onde_voce_retira), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(T.small)) {
                    MedicationSource.entries.forEach { source -> FilterChip(selected = medication.source == source, onClick = { update { it.copy(source = source) } }, label = { Text(source.label) }) }
                }
                Field(medication.sourceName, { value -> update { it.copy(sourceName = value) } }, stringResource(R.string.ui_nome_do_local_opcional), stringResource(R.string.ui_ex_ubs_central))
                Text(stringResource(R.string.ui_receita_para_este_acompanhamento), style = MaterialTheme.typography.labelLarge)
                FilterChip(selected = medication.linkedPrescriptionId == null && !medication.linkToCurrentRecipe, onClick = { update { it.copy(linkedPrescriptionId = null, linkToCurrentRecipe = false) } }, label = { Text(stringResource(R.string.ui_sem_receita_vinculada)) })
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
                    FilterChip(selected = medication.linkToCurrentRecipe, onClick = { update { it.copy(linkToCurrentRecipe = true, linkedPrescriptionId = null, trackingArchived = false) } }, label = { Text(if (state.previousPrescriptionId != null) stringResource(R.string.ui_continuar_nesta_nova_receita) else stringResource(R.string.ui_usar_esta_receita)) })
                }
                state.availablePrescriptions.filter { p -> p.id != state.previousPrescriptionId && p.medicationIds.contains(medication.id) }.forEach { p ->
                    FilterChip(selected = !medication.linkToCurrentRecipe && medication.linkedPrescriptionId == p.id, onClick = { update { it.copy(linkedPrescriptionId = p.id, linkToCurrentRecipe = false, trackingArchived = false) } }, label = { Text(p.title) })
                }
                if (state.previousPrescriptionId != null && medication.linkedPrescriptionId == state.previousPrescriptionId && !medication.linkToCurrentRecipe) Text(stringResource(R.string.ui_este_acompanhamento_sera_arquivado_ao_substituir_a_receita_anterior_selecio), style = MaterialTheme.typography.bodySmall)

                medication.pickupError()?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
@Composable
private fun Summary(state: PrescriptionWizardUiState, actions: WizardCallbacks, editable: Boolean) {
    if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
        Section(stringResource(R.string.label_prescription), if (editable) ({ actions.goToStep(1) }) else null) {
            Text(state.prescriptionTitle.ifBlank { state.allMedications.firstOrNull()?.title.orEmpty() + " — " + dateText(state.issueDate) }, fontWeight = FontWeight.SemiBold)
            Text(state.type?.label ?: stringResource(R.string.label_unknown_prescription_type))
            Text(stringResource(R.string.issue_date, dateText(state.issueDate)))
            state.validityDays?.takeIf { it > 0 }?.let { days -> Text(stringResource(R.string.expiration_date, dateText(state.issueDate.plusDays(days.toLong())))) }
            Text(if (state.renewalReminderEnabled) stringResource(R.string.renewal_reminder, reminderText(state.renewalReminderDaysBefore)) else stringResource(R.string.ui_alertas_de_validade_e_renovacao_desativados))
            if (state.doctorName.isNotBlank()) Text(stringResource(R.string.professional_name, state.doctorName))
        }
    }
    Section(stringResource(R.string.medications_count, state.allMedications.size), if (editable) ({ actions.goToStep(state.medicationStep) }) else null) {
        state.allMedications.forEachIndexed { index, medication ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.tight)) {
                Column {
                    Text(medication.title.ifBlank { stringResource(R.string.ui_nome_nao_informado) }, fontWeight = FontWeight.SemiBold)
                    if (medication.dosage.isNotBlank()) Text(medication.dosage, style = MaterialTheme.typography.bodySmall)
                }
                if (editable) TextButton(onClick = { actions.editMedication(index) }) { Text(stringResource(R.string.action_edit)) }
            }
        }
    }
    Section(stringResource(R.string.ui_lembretes_de_retirada), if (editable) ({ actions.goToStep(state.pickupStep) }) else null) {
        state.allMedications.forEach { medication ->
            Text(medication.title, fontWeight = FontWeight.SemiBold)
            if (medication.replenishmentQuantity.isNotBlank()) Text(stringResource(R.string.pickup_quantity, medication.replenishmentQuantity), style = MaterialTheme.typography.bodySmall)
            if (medication.hasRecurringPickup) {
                val date = medication.lastPickupDate
                val interval = medication.pickupIntervalDays
                if (date != null && interval != null && interval > 0) Text(stringResource(R.string.next_pickup_date, dateText(PickupInterval(interval, medication.intervalUnit).nextAfter(date))))
                else Text(stringResource(R.string.ui_complete_a_data_e_o_intervalo_da_retirada), color = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.pickup_reminder, reminderText(medication.pickupReminderDaysBefore)), style = MaterialTheme.typography.bodySmall)
                if (medication.sourceName.isNotBlank()) Text(stringResource(R.string.pickup_place, medication.sourceName), style = MaterialTheme.typography.bodySmall)
            } else Text(stringResource(R.string.ui_retirada_recorrente_desativada), style = MaterialTheme.typography.bodySmall)
        }
    }
    if (editable && state.pickupOptionsStep != null) SecondaryActionButton(stringResource(R.string.design_edit_reminders), { actions.goToStep(requireNotNull(state.pickupOptionsStep)) }, Modifier.fillMaxWidth())
    if (editable && !state.canSave) Text(state.recipeError ?: state.medicationError ?: state.pickupError ?: stringResource(R.string.ui_confira_os_campos_antes_de_salvar), color = MaterialTheme.colorScheme.error)
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "", singleLine: Boolean = true) {
    FormField(value, onChange, label, placeholder, singleLine)
}

@Composable
private fun NumericField(value: String, onChange: (String) -> Unit, label: String, error: Boolean = false) {
    FormField(value, { onChange(it.filter(Char::isDigit).take(4)) }, label,
        error = if (error) stringResource(R.string.design_number_error) else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOptions(selected: Int, options: List<Int>, onSelect: (Int) -> Unit) {
    Text(stringResource(R.string.current_reminder, reminderText(selected)), style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(T.small)) {
        options.forEach { days -> FilterChip(selected = selected == days, onClick = { onSelect(days) }, label = { Text(reminderText(days)) }) }
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, allowFuture: Boolean = false, onSelect: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val initial = date ?: LocalDate.now()
        DatePickerDialog(context, { _, year, month, day -> onSelect(LocalDate.of(year, month + 1, day)) }, initial.year, initial.monthValue - 1, initial.dayOfMonth).apply { if (!allowFuture) datePicker.maxDate = System.currentTimeMillis() }.show()
    }, modifier = Modifier.fillMaxWidth().heightIn(min = T.control), shape = androidx.compose.foundation.shape.RoundedCornerShape(T.buttonRadius), contentPadding = PaddingValues(T.padding)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.tight)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(date?.let(::dateText) ?: stringResource(R.string.ui_selecionar_data), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun dateText(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
@Composable
private fun reminderText(days: Int): String = if (days == 0) stringResource(R.string.reminder_on_day) else pluralStringResource(R.plurals.reminder_days_before, days, days)

@Preview(showBackground = true)
@Composable
private fun RegistrationPreview() { ReceitasTheme { PrescriptionWizardContent(PrescriptionWizardUiState(), WizardCallbacks()) } }

@Preview(showBackground = true)
@Composable
private fun ReviewPreview() {
    ReceitasTheme { PrescriptionWizardContent(PrescriptionWizardUiState(currentStep = 5, showMedicationEditor = false, medications = listOf(MedicationDraft(id = "preview", title = "Losartana", dosageOverride = "50 mg", hasRecurringPickup = true, lastPickupDate = LocalDate.now().minusDays(25)))), WizardCallbacks()) }
}
