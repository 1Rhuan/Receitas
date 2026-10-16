package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.compose.ui.res.pluralStringResource

import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.components.FormSection as Section
import cc.rhuan.receitas.ui.components.RecordSummarySection
import cc.rhuan.receitas.ui.components.SummaryItemGroup
import cc.rhuan.receitas.ui.components.FormRecordSection
import cc.rhuan.receitas.ui.components.OptionalDetailRow
import cc.rhuan.receitas.ui.components.GroupedSection
import cc.rhuan.receitas.ui.components.GroupedValueRow
import cc.rhuan.receitas.ui.components.ReviewSection
import cc.rhuan.receitas.ui.components.ReviewRecordCard
import cc.rhuan.receitas.ui.components.FormChoiceField
import cc.rhuan.receitas.ui.components.FormChoiceCards
import cc.rhuan.receitas.ui.components.FormChoiceOption
import cc.rhuan.receitas.ui.components.FormField
import cc.rhuan.receitas.ui.components.FormToggle as ToggleRow
import cc.rhuan.receitas.ui.components.SecondaryActionButton
import cc.rhuan.receitas.ui.components.ConfirmationSheet
import cc.rhuan.receitas.ui.components.RadioChoiceList
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
            if (state.typeEditor != null) actions.cancelTypeEditor()
            else if (state.isSaved || (state.currentStep == 1 && state.reviewEditStep == null)) close() else actions.back()
        }
    }
    BackHandler { back() }
    if (showExitConfirmation) {
        ConfirmationSheet(title = stringResource(R.string.ui_sair_sem_salvar),
            message = stringResource(R.string.ui_os_dados_preenchidos_neste_cadastro_serao_descartados),
            confirmLabel = stringResource(R.string.ui_descartar_e_sair),
            onConfirm = { showExitConfirmation = false; onNavigateUp() },
            onDismiss = { showExitConfirmation = false }, destructive = true,
            dismissLabel = stringResource(R.string.ui_continuar_cadastro))
    }
    PrescriptionWizardContent(
        state, actions.copy(back = back, close = close), modifier,
    )
}

@Composable
internal fun PrescriptionWizardContent(state: PrescriptionWizardUiState, actions: WizardCallbacks, modifier: Modifier = Modifier) {
    val screenStateHolder = rememberSaveableStateHolder()
    var optionalEditor by rememberSaveable(state.registrationId) { mutableStateOf<OptionalDetailDraft?>(null) }
    optionalEditor?.let { draft ->
        val dismiss = { optionalEditor = null }
        BackHandler(onBack = dismiss)
        OptionalDetailEditor(state, draft, { optionalEditor = it }, dismiss, {
            actions.update { draft.applyTo(it) }
            optionalEditor = null
        }, modifier)
        return
    }
    if (state.typeEditor != null) {
        PrescriptionTypeEditor(state, actions, modifier)
        return
    }
    val standalone = state.entryMode == WizardEntryMode.STANDALONE_MEDICATION
    val title = when {
        !state.isSaved && state.currentStep == state.maxSteps -> stringResource(R.string.ui_confira_antes_de_salvar)
        state.isSaved -> if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.ui_medicamento_atualizado) else stringResource(R.string.ui_medicamento_cadastrado) } else if (state.editingPrescriptionId != null) stringResource(R.string.ui_receita_atualizada) else stringResource(R.string.ui_receita_cadastrada)
        state.currentStep == state.medicationStep -> stringResource(R.string.ui_quais_medicamentos_estao_na_receita).let { if (standalone) stringResource(R.string.ui_qual_medicamento_deseja_acompanhar) else it }
        state.currentStep == state.recipeIdentityStep -> stringResource(R.string.design_recipe_identity)
        state.currentStep == state.renewalOptionsStep -> stringResource(R.string.recipe_renewal_options_title)
        state.currentStep == state.optionalDetailsStep -> stringResource(R.string.optional_details_title)
        state.currentStep == state.pickupContextStep -> stringResource(R.string.guided_pickup_context_title)
        state.currentStep == state.pickupOptionsStep -> stringResource(R.string.design_pickup_options_title)
        state.currentStep == state.pickupStep -> stringResource(R.string.ui_quando_voce_retira_os_medicamentos)
        else -> if (state.previousPrescriptionId != null) stringResource(R.string.ui_vamos_registrar_sua_nova_receita) else stringResource(R.string.ui_vamos_cadastrar_sua_receita)
    }
    screenStateHolder.SaveableStateProvider(state.currentStep) {
    key(state.currentStep, state.isSaved) {
        WizardQuestionScreen(
            question = title,
            onBack = actions.back, progress = if (state.isSaved) 1f else state.progress,
            primaryButtonText = when {
                state.isSaved -> if (standalone) stringResource(R.string.action_finish) else stringResource(R.string.ui_ver_receita)
                state.isLoading && state.currentStep == state.maxSteps -> stringResource(R.string.ui_salvando)
                state.currentStep == state.maxSteps -> if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.ui_salvar_alteracoes) else stringResource(R.string.ui_salvar_medicamento) } else if (state.editingPrescriptionId != null) stringResource(R.string.ui_salvar_alteracoes) else stringResource(R.string.ui_salvar_receita)
                state.reviewEditStep != null -> stringResource(R.string.ui_voltar_a_revisao)
                else -> stringResource(R.string.action_advance)
            },
            primaryButtonEnabled = !state.isLoading && !state.initialLoadFailed,
            onPrimaryClick = when { state.isSaved -> actions.complete; state.currentStep == state.maxSteps -> actions.save; else -> actions.next },
            onSkip = if (state.canSkipCurrentStep) actions.skip else null,
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
                state.maxSteps -> Summary(state, actions, editable = true)
                state.recipeNameStep -> RecipeNameSection(state, actions)
                state.renewalOptionsStep -> RenewalOptionsSection(state, actions)
                state.optionalDetailsStep -> OptionalDetailsSection(state) { optionalEditor = it }
                state.medicationStep -> MedicationSection(state, actions)
                state.pickupStep -> PickupSection(state, actions)
                state.pickupOptionsStep -> PickupOptionsSection(state, actions)
                state.pickupContextStep -> PickupContextSection(state, actions)
                else -> RecipeSection(state, actions)
            }
        }
    }
    }
}

@Composable
private fun PrescriptionTypeEditor(state: PrescriptionWizardUiState, actions: WizardCallbacks, modifier: Modifier) {
    val draft = requireNotNull(state.typeEditor)
    val types = PrescriptionType.entries
    var showCustomValidity by rememberSaveable { mutableStateOf(draft.type == PrescriptionType.PERSONALIZADO) }
    val editorPages = rememberSaveableStateHolder()
    val back = { if (showCustomValidity) showCustomValidity = false else actions.cancelTypeEditor() }
    BackHandler(onBack = back)
    editorPages.SaveableStateProvider(showCustomValidity) {
    key(showCustomValidity) {
    WizardQuestionScreen(
        question = stringResource(if (showCustomValidity) R.string.design_recipe_validity else R.string.recipe_type_and_validity_title),
        onBack = back,
        progress = state.progress,
        primaryButtonText = stringResource(when {
            showCustomValidity -> R.string.recipe_apply_custom_validity
            draft.type == PrescriptionType.PERSONALIZADO -> R.string.action_advance
            state.currentStep == state.maxSteps || state.reviewEditStep != null -> R.string.ui_voltar_a_revisao
            else -> R.string.action_advance
        }),
        primaryButtonEnabled = !state.isLoading,
        onPrimaryClick = {
            if (draft.type == PrescriptionType.PERSONALIZADO && !showCustomValidity) showCustomValidity = true
            else actions.confirmTypeEditor()
        },
        errorMessage = state.errorMessage.takeIf { showCustomValidity },
        modifier = modifier,
    ) {
        if (!showCustomValidity) Section(stringResource(R.string.ui_tipo_de_receita)) {
            FormChoiceCards(types.map { type ->
                FormChoiceOption(type.label, stringResource(when (type) {
                    PrescriptionType.CONTINUO -> R.string.recipe_type_continuous_help
                    PrescriptionType.ANTIBIOTICO -> R.string.recipe_type_antibiotic_help
                    PrescriptionType.CONTROLADO -> R.string.recipe_type_controlled_help
                    PrescriptionType.FARMACIA_POPULAR -> R.string.recipe_type_popular_help
                    PrescriptionType.SUS_BASIC -> R.string.recipe_type_basic_help
                    PrescriptionType.ANUAL -> R.string.recipe_type_annual_help
                    PrescriptionType.PERSONALIZADO -> R.string.recipe_type_custom_help
                }) + type.defaultValidityDays?.let { days ->
                    "\n" + pluralStringResource(R.plurals.recipe_type_suggested_validity, days, days)
                }.orEmpty())
            }, types.indexOf(draft.type), {
                actions.selectType(types[it])
                if (types[it] == PrescriptionType.PERSONALIZADO) showCustomValidity = true
            })
        }
        if (showCustomValidity) Column(verticalArrangement = Arrangement.spacedBy(T.padding)) {
            Text(stringResource(R.string.recipe_custom_validity_help), style = MaterialTheme.typography.bodyMedium)
            ToggleRow(stringResource(R.string.design_choose_expiration), draft.useExpirationDate, actions.setValidityDateMode)
            if (draft.useExpirationDate) {
                DateField(stringResource(R.string.ui_vencimento_personalizado), draft.validityDays?.let { state.issueDate.plusDays(it.toLong()) }, allowFuture = true, onSelect = actions.updateExpirationDate)
            } else {
                NumericField(draft.validityDaysInput, actions.updateValidityDays, stringResource(R.string.ui_validade_em_dias),
                    draft.validityDays == null || draft.validityDays !in 1..3650)
            }
            Text(stringResource(R.string.ui_confira_o_prazo_indicado_na_receita_ou_informado_pela_unidade_de_retirada), style = MaterialTheme.typography.bodySmall)
            draft.validityDays?.takeIf { it in 1..3650 }?.let { days ->
                Text(stringResource(R.string.expiration_date, dateText(state.issueDate.plusDays(days.toLong()))), fontWeight = FontWeight.SemiBold)
            }
        }
    }
    }
    }
}

@Composable
private fun RecipeSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.padding)) {
        DateField(stringResource(R.string.ui_data_de_emissao), state.issueDate, onSelect = actions.updateIssueDate)
        FormChoiceField(stringResource(R.string.ui_tipo_de_receita),
            state.type?.label ?: stringResource(R.string.label_unknown_prescription_type),
            actions.openTypeEditor,
            state.validityDays?.takeIf { it in 1..3650 }?.let { days ->
                pluralStringResource(R.plurals.recipe_type_suggested_validity, days, days)
                    .takeIf { state.validityOrigin == "SUGGESTED" }
                    ?: stringResource(R.string.expiration_date, dateText(state.issueDate.plusDays(days.toLong())))
            }, required = true)
    }
}

@Composable
private fun RecipeNameSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Section(stringResource(R.string.label_prescription)) {
        Field(state.prescriptionTitle, { value -> actions.update { it.copy(prescriptionTitle = value, isTitleCustomized = value.isNotBlank()) } }, stringResource(R.string.ui_nome_da_receita_opcional), stringResource(R.string.ui_ex_receita_de_acompanhamento), required = true)
        Text(stringResource(R.string.form_name_required_help), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RenewalOptionsSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Section(stringResource(R.string.ui_lembrete_de_renovacao)) {
        ToggleRow(stringResource(R.string.ui_receber_alertas_de_validade_e_renovacao), state.renewalReminderEnabled) { enabled -> actions.update { it.copy(renewalReminderEnabled = enabled) } }
        if (state.renewalReminderEnabled) {
            DayOptions(state.renewalReminderDaysBefore,
                (listOf(0, 3, 5, 7, 10, 15) + state.renewalReminderDaysBefore).distinct().sorted()
                    .filter { it in 0..(state.validityDays ?: 180) }) { days -> actions.update { it.copy(renewalReminderDaysBefore = days) } }
        } else Text(stringResource(R.string.ui_os_alertas_desta_receita_estarao_desativados), style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicationSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    if (state.addToPrescriptionId != null) Text(stringResource(R.string.medication_destination_recipe, state.addToPrescriptionTitle), style = MaterialTheme.typography.bodyMedium)
    state.medications.forEachIndexed { index, medication ->
        if (index != state.editingMedicationIndex || !state.showMedicationEditor) {
            RecordSummarySection(medication.title) {
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
        Section(if (state.editingMedicationIndex != null) stringResource(R.string.ui_editar_medicamento) else stringResource(R.string.ui_adicionar_medicamento), prominent = true) {
            Field(draft.title, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = value)) } }, stringResource(R.string.ui_nome_do_medicamento), stringResource(R.string.ui_ex_losartana), required = true)
            if (state.editingMedicationIndex == null) {
                state.availableMedications.filter { draft.title.isNotBlank() && it.title.contains(draft.title, true) && it.id !in state.existingPrescriptionMedicationIds && state.medications.none { existing -> existing.id == it.id } }.take(3).forEach { medication ->
                    OutlinedButton(onClick = {
                        actions.selectMedication(medication)
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.existing_medication, (medication.title + " " + medication.dosage).trim())) }
                }
            }
            Field(draft.dosage, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(dosageOverride = value)) } }, stringResource(R.string.ui_concentracao_opcional), stringResource(R.string.ui_ex_50_mg))
            if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) Text(stringResource(R.string.ui_apenas_para_consulta_o_aplicativo_acompanha_validade_renovacao_e_retirada), style = MaterialTheme.typography.bodySmall)
            if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && state.editingMedicationIndex == null) {
                SecondaryActionButton(stringResource(R.string.ui_adicionar_outro_medicamento), actions.addMedication, Modifier.fillMaxWidth())
            }
            if (state.medications.isNotEmpty() || (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && draft != MedicationDraft())) TextButton(onClick = actions.cancelMedication) { Text(if (state.editingMedicationIndex != null) stringResource(R.string.ui_cancelar_edicao) else stringResource(R.string.ui_cancelar_inclusao)) }
        }
    } else {
        SecondaryActionButton(stringResource(if (state.medications.isEmpty()) R.string.ui_adicionar_medicamento else R.string.ui_adicionar_outro_medicamento), actions.addMedication, Modifier.fillMaxWidth())
        Text(pluralStringResource(R.plurals.registration_medication_count, state.medications.size, state.medications.size), style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickupSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        val update: ((MedicationDraft) -> MedicationDraft) -> Unit = { transform -> actions.update { current -> current.copy(medications = current.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) } }
        Section(medication.title) {
            ToggleRow(stringResource(R.string.ui_acompanhar_retirada_recorrente), medication.hasRecurringPickup) { enabled -> update {
                val suggestedDate = it.lastPickupDate ?: if (enabled && !it.hasPickupHistory) LocalDate.now() else null
                it.copy(hasRecurringPickup = enabled, lastPickupDate = suggestedDate,
                    initialPickupDate = it.initialPickupDate ?: suggestedDate.takeIf { _ -> !it.hasPickupHistory })
            } }
            if (medication.hasRecurringPickup) {
                if (medication.hasPickupHistory) {
                    Text(stringResource(R.string.last_pickup_date, medication.lastPickupDate?.let(::dateText).orEmpty()))
                    Text(stringResource(R.string.ui_para_registrar_ou_corrigir_uma_retirada_use_o_historico_nos_detalhes_do_med), style = MaterialTheme.typography.bodySmall)
                } else DateField(stringResource(R.string.ui_ultima_retirada), medication.lastPickupDate) { date -> update { it.copy(lastPickupDate = date, initialPickupDate = date) } }
                Text(stringResource(R.string.ui_use_a_data_em_que_voce_realmente_retirou_o_medicamento), style = MaterialTheme.typography.bodySmall)
                NumericField(medication.pickupIntervalDisplayInput, { value -> actions.updatePickupInterval(index, value) }, stringResource(R.string.ui_intervalo_entre_retiradas), medication.pickupIntervalDays == null || medication.pickupIntervalDays !in 1..3650)
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
            FormRecordSection(medication.title, subtitle = medication.dosage,
                divider = state.medications.take(index).any { it.hasRecurringPickup }, modifier = Modifier.testTag("pickup-options-$index")) {
                Text(stringResource(R.string.ui_avisar_com_antecedencia), style = MaterialTheme.typography.labelLarge)
                DayOptions(medication.pickupReminderDaysBefore, listOf(0, 1, 3, 5, 7).filter { it <= medication.intervalDurationDays }) { days -> update { it.copy(pickupReminderDaysBefore = days) } }
                medication.pickupError()?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickupContextSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        if (medication.hasRecurringPickup) {
            val update: ((MedicationDraft) -> MedicationDraft) -> Unit = { transform -> actions.update { current -> current.copy(medications = current.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) } }
            FormRecordSection(medication.title, subtitle = medication.dosage,
                divider = state.medications.take(index).any { it.hasRecurringPickup }, modifier = Modifier.testTag("pickup-context-$index")) {
                Section(stringResource(R.string.ui_onde_voce_retira)) {
                RadioChoiceList(MedicationSource.entries.map { it.label }, MedicationSource.entries.indexOf(medication.source),
                    { choice -> update { it.copy(source = MedicationSource.entries[choice]) } })
                Field(medication.sourceName, { value -> update { it.copy(sourceName = value) } }, stringResource(R.string.ui_nome_do_local_opcional), stringResource(R.string.ui_ex_ubs_central))
                }
                Section(stringResource(R.string.ui_receita_para_este_acompanhamento)) {
                if (state.addToPrescriptionId != null) {
                    Text(state.addToPrescriptionTitle)
                } else {
                FilterChip(selected = medication.linkedPrescriptionId == null && !medication.linkToCurrentRecipe, onClick = { update { it.copy(linkedPrescriptionId = null, linkToCurrentRecipe = false) } }, label = { Text(stringResource(R.string.ui_sem_receita_vinculada)) })
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
                    FilterChip(selected = medication.linkToCurrentRecipe, onClick = { update { it.copy(linkToCurrentRecipe = true, linkedPrescriptionId = null, trackingArchived = false) } }, label = { Text(if (state.previousPrescriptionId != null) stringResource(R.string.ui_continuar_nesta_nova_receita) else stringResource(R.string.ui_usar_esta_receita)) })
                }
                state.availablePrescriptions.filter { p -> p.id != state.previousPrescriptionId && p.medicationIds.contains(medication.id) }.forEach { p ->
                    FilterChip(selected = !medication.linkToCurrentRecipe && medication.linkedPrescriptionId == p.id, onClick = { update { it.copy(linkedPrescriptionId = p.id, linkToCurrentRecipe = false, trackingArchived = false) } }, label = { Text(p.title) })
                }
                if (state.previousPrescriptionId != null && medication.linkedPrescriptionId == state.previousPrescriptionId && !medication.linkToCurrentRecipe) Text(stringResource(R.string.ui_este_acompanhamento_sera_arquivado_ao_substituir_a_receita_anterior_selecio), style = MaterialTheme.typography.bodySmall)
                }
                }

                medication.pickupError()?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
@Composable
private fun Summary(state: PrescriptionWizardUiState, actions: WizardCallbacks, editable: Boolean) {
    val recipeTitle = state.prescriptionTitle.trim().ifBlank {
        (state.allMedications.firstOrNull()?.title ?: stringResource(R.string.label_prescription)) + " — " + dateText(state.issueDate)
    }
    if (state.addToPrescriptionId != null) GroupedSection(stringResource(R.string.medication_origin_recipe)) {
        GroupedValueRow(stringResource(R.string.review_recipe_name), state.addToPrescriptionTitle, divider = false)
    }
    if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
        GroupedSection(stringResource(R.string.label_prescription)) {
            GroupedValueRow(stringResource(R.string.review_recipe_name), recipeTitle, divider = false,
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeNameStep)) }) else null, stacked = true)
            GroupedValueRow(stringResource(R.string.ui_data_de_emissao), dateText(state.issueDate),
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeIdentityStep)) }) else null)
            val expiration = state.validityDays?.takeIf { it > 0 }?.let { state.issueDate.plusDays(it.toLong()) }
            val validity = expiration?.let { stringResource(R.string.expiration_date, dateText(it)) }
            GroupedValueRow(stringResource(R.string.recipe_type_and_validity_title),
                listOfNotNull(state.type?.label ?: stringResource(R.string.label_unknown_prescription_type), validity).joinToString("\n"),
                onEdit = if (editable) actions.openTypeEditor else null, stacked = true)
            GroupedValueRow(stringResource(R.string.recipe_renewal_options_title),
                if (state.renewalReminderEnabled) stringResource(R.string.renewal_reminder, reminderText(state.renewalReminderDaysBefore)) else stringResource(R.string.ui_alertas_de_validade_e_renovacao_desativados),
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.renewalOptionsStep)) }) else null, stacked = true)
            GroupedValueRow(stringResource(R.string.ui_profissional_responsavel),
                listOf(state.doctorName, state.doctorRegistration, state.doctorSpecialty).filter { it.isNotBlank() }.joinToString("\n").ifBlank { stringResource(R.string.review_not_provided) },
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeDetailsStep)) }) else null, stacked = true)
            GroupedValueRow(stringResource(R.string.recipe_notes_group), state.notes.ifBlank { stringResource(R.string.review_no_notes) },
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeDetailsStep)) }) else null, stacked = true)
        }
    }
    ReviewSection(stringResource(R.string.medications_count, state.allMedications.size),
        onEdit = if (editable) ({ actions.goToStep(state.medicationStep) }) else null) {
        if (state.allMedications.isEmpty()) Text(stringResource(R.string.recipe_medications_optional_summary))
        state.allMedications.forEachIndexed { index, medication ->
            ReviewRecordCard(medication.title.ifBlank { stringResource(R.string.ui_nome_nao_informado) }, medication.dosage,
                onEdit = if (editable) ({ actions.editMedication(index) }) else null,
                modifier = Modifier.testTag("review-medication-$index"))
        }
    }
    if (state.allMedications.isNotEmpty()) GroupedSection(stringResource(R.string.review_pickups_title),
        onEdit = if (editable) ({ actions.goToStep(state.pickupStep) }) else null) {
        state.allMedications.forEachIndexed { index, medication ->
            SummaryItemGroup(medication.title, divider = index > 0,
                modifier = Modifier.padding(T.padding).testTag("review-pickup-$index")) {
                if (medication.replenishmentQuantity.isNotBlank()) Text(stringResource(R.string.pickup_quantity, medication.replenishmentQuantity))
                if (medication.hasRecurringPickup) {
                    val date = medication.lastPickupDate
                    val interval = medication.pickupIntervalDays
                    if (date != null && interval != null && interval > 0) Text(stringResource(R.string.next_pickup_date, dateText(PickupInterval(interval, medication.intervalUnit).nextAfter(date))))
                    else Text(stringResource(R.string.ui_complete_a_data_e_o_intervalo_da_retirada), color = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.pickup_reminder, reminderText(medication.pickupReminderDaysBefore)))
                    if (medication.sourceName.isNotBlank()) Text(stringResource(R.string.pickup_place, medication.sourceName))
                    val pickupRecipeTitle = when {
                        medication.linkToCurrentRecipe -> recipeTitle
                        medication.linkedPrescriptionId == state.addToPrescriptionId && state.addToPrescriptionId != null -> state.addToPrescriptionTitle
                        else -> state.availablePrescriptions.find { it.id == medication.linkedPrescriptionId }?.title
                    }
                    if (medication.linkToCurrentRecipe || medication.linkedPrescriptionId != null) Text(stringResource(R.string.pickup_recipe_summary, pickupRecipeTitle ?: stringResource(R.string.pickup_selected_recipe)))
                    else Text(stringResource(R.string.ui_sem_receita_vinculada))
                } else Text(stringResource(R.string.ui_retirada_recorrente_desativada))
            }
        }
        if (editable && state.pickupOptionsStep != null) Column(Modifier.fillMaxWidth().padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.small)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SecondaryActionButton(stringResource(R.string.design_edit_reminders), { actions.goToStep(requireNotNull(state.pickupOptionsStep)) }, Modifier.fillMaxWidth())
            SecondaryActionButton(stringResource(R.string.guided_edit_context), { actions.goToStep(requireNotNull(state.pickupContextStep)) }, Modifier.fillMaxWidth())
        }
    }
    if (editable && !state.canSave) Text(state.recipeError ?: state.medicationError ?: state.pickupError ?: stringResource(R.string.ui_confira_os_campos_antes_de_salvar), color = MaterialTheme.colorScheme.error)
}
@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "", singleLine: Boolean = true, required: Boolean = false) {
    FormField(value, onChange, label, placeholder, singleLine, required = required)
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
    RadioChoiceList(options.map { reminderText(it) }, options.indexOf(selected), { index -> onSelect(options[index]) })
}

@Composable
private fun DateField(label: String, date: LocalDate?, allowFuture: Boolean = false, onSelect: (LocalDate) -> Unit) {
    val context = LocalContext.current
    FormChoiceField(label, date?.let(::dateText) ?: stringResource(R.string.ui_selecionar_data), onClick = {
        val initial = date ?: LocalDate.now()
        DatePickerDialog(context, { _, year, month, day -> onSelect(LocalDate.of(year, month + 1, day)) }, initial.year, initial.monthValue - 1, initial.dayOfMonth).apply { if (!allowFuture) datePicker.maxDate = System.currentTimeMillis() }.show()
    }, required = true)
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
    ReceitasTheme { PrescriptionWizardContent(PrescriptionWizardUiState(currentStep = 9, showMedicationEditor = false, medications = listOf(MedicationDraft(id = "preview", title = "Losartana", dosageOverride = "50 mg", hasRecurringPickup = true, lastPickupDate = LocalDate.now().minusDays(25)))), WizardCallbacks()) }
}
