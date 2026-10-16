package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.text.asString

import androidx.compose.ui.res.pluralStringResource

import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.components.FormSection as Section
import cc.rhuan.receitas.ui.components.RecordSummarySection
import cc.rhuan.receitas.ui.components.SummaryItemGroup
import cc.rhuan.receitas.ui.components.FormRecordSection
import cc.rhuan.receitas.ui.components.ContentCard
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
import cc.rhuan.receitas.ui.theme.AppTheme
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
        ConfirmationSheet(title = stringResource(R.string.exit_without_saving),
            message = stringResource(R.string.discard_registration_confirmation),
            confirmLabel = stringResource(R.string.discard_and_exit),
            onConfirm = { showExitConfirmation = false; onNavigateUp() },
            onDismiss = { showExitConfirmation = false }, destructive = true,
            dismissLabel = stringResource(R.string.continue_registration))
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
    if (state.isSaved && state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && state.editingPrescriptionId == null) {
        RecipeRegistrationSuccess(state, actions, modifier)
        return
    }
    val standalone = state.entryMode == WizardEntryMode.STANDALONE_MEDICATION
    val title = when {
        !state.isSaved && state.currentStep == state.maxSteps -> stringResource(R.string.review_before_saving)
        state.isSaved -> if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.medication_updated) else stringResource(R.string.medication_registered) } else if (state.editingPrescriptionId != null) stringResource(R.string.prescription_updated) else stringResource(R.string.prescription_registered)
        state.currentStep == state.medicationStep -> stringResource(R.string.prescription_medications_question).let { if (standalone) stringResource(R.string.registration_medication_name_question) else it }
        state.currentStep == state.recipeNameStep -> stringResource(R.string.registration_recipe_name_question)
        state.currentStep == state.recipeIdentityStep -> stringResource(R.string.registration_issue_date_question)
        state.currentStep == state.renewalOptionsStep -> stringResource(R.string.recipe_renewal_options_title)
        state.currentStep == state.optionalDetailsStep -> stringResource(R.string.optional_details_title)
        state.currentStep == state.pickupContextStep -> stringResource(R.string.guided_pickup_context_title)
        state.currentStep == state.pickupOptionsStep -> stringResource(R.string.design_pickup_options_title)
        state.currentStep == state.pickupStep -> stringResource(R.string.medication_pickup_question)
        else -> if (state.previousPrescriptionId != null) stringResource(R.string.register_renewal_heading) else stringResource(R.string.register_prescription_heading)
    }
    screenStateHolder.SaveableStateProvider(state.currentStep) {
    key(state.currentStep, state.isSaved) {
        WizardQuestionScreen(
            question = title,
            onBack = actions.back, progress = if (state.isSaved) 1f else state.progress,
            primaryButtonText = when {
                state.isSaved -> if (standalone) stringResource(R.string.action_finish) else stringResource(R.string.view_prescription)
                state.isLoading && state.currentStep == state.maxSteps -> stringResource(R.string.saving)
                state.currentStep == state.maxSteps -> if (standalone) { if (state.editingMedicationId != null) stringResource(R.string.save_changes) else stringResource(R.string.save_medication) } else if (state.editingPrescriptionId != null) stringResource(R.string.save_changes) else stringResource(R.string.save_prescription)
                state.reviewEditStep != null -> stringResource(R.string.return_to_review)
                else -> stringResource(R.string.action_advance)
            },
            primaryButtonEnabled = !state.isLoading && !state.initialLoadFailed && (state.isSaved || state.currentStepError == null),
            onPrimaryClick = when { state.isSaved -> actions.complete; state.currentStep == state.maxSteps -> actions.save; else -> actions.next },
            onSkip = if (state.canSkipCurrentStep) actions.skip else null,
            isBusy = state.isLoading, errorMessage = state.errorMessage, modifier = modifier,
        ) {
            if (state.initialLoadFailed) {
                Button(onClick = actions.retry, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_retry)) }
            } else if (state.isLoading && state.currentStep != state.maxSteps) {
                CircularProgressIndicator()
            } else if (state.isSaved) {
                Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.registration_complete), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(T.control))
                Summary(state, actions, editable = false)
                Text(stringResource(R.string.saved_record_guidance), style = MaterialTheme.typography.bodyMedium)
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
    var showCustomValidity by rememberSaveable { mutableStateOf(false) }
    val editorPages = rememberSaveableStateHolder()
    val back = { if (showCustomValidity) showCustomValidity = false else actions.cancelTypeEditor() }
    BackHandler(onBack = back)
    editorPages.SaveableStateProvider(showCustomValidity) {
    key(showCustomValidity) {
    WizardQuestionScreen(
        question = stringResource(if (showCustomValidity) R.string.custom_validity_question else R.string.registration_validity_question),
        onBack = back,
        progress = state.progress,
        primaryButtonText = stringResource(when {
            showCustomValidity -> R.string.recipe_apply_custom_validity
            else -> R.string.action_advance
        }),
        primaryButtonEnabled = !state.isLoading && (!showCustomValidity || draft.error == null),
        onPrimaryClick = {
            if (draft.type == PrescriptionType.CUSTOM && !showCustomValidity) showCustomValidity = true
            else actions.confirmTypeEditor()
        },
        errorMessage = state.errorMessage.takeIf { showCustomValidity },
        modifier = modifier,
    ) {
        if (!showCustomValidity) Column(verticalArrangement = Arrangement.spacedBy(T.padding)) {
            FormChoiceCards(types.map { type ->
                FormChoiceOption(stringResource(type.labelRes), stringResource(when (type) {
                    PrescriptionType.CONTINUOUS_USE -> R.string.recipe_type_continuous_help
                    PrescriptionType.ANTIBIOTIC -> R.string.recipe_type_antibiotic_help
                    PrescriptionType.CONTROLLED -> R.string.recipe_type_controlled_help
                    PrescriptionType.POPULAR_PHARMACY -> R.string.recipe_type_popular_help
                    PrescriptionType.SUS_BASIC -> R.string.recipe_type_basic_help
                    PrescriptionType.ANNUAL_USE -> R.string.recipe_type_annual_help
                    PrescriptionType.CUSTOM -> R.string.recipe_type_custom_help
                }) + type.defaultValidityDays?.let { days ->
                    "\n" + pluralStringResource(R.plurals.recipe_type_suggested_validity, days, days)
                }.orEmpty())
            }, types.indexOf(draft.type), {
                actions.selectType(types[it])
            })
        }
        if (showCustomValidity) Column(verticalArrangement = Arrangement.spacedBy(T.section)) {
            RadioChoiceList(listOf(stringResource(R.string.custom_validity_days_option), stringResource(R.string.custom_validity_date_option)),
                if (draft.useExpirationDate) 1 else 0, { actions.setValidityDateMode(it == 1) })
            if (draft.useExpirationDate) {
                DateField(stringResource(R.string.custom_expiration), draft.validityDays?.let { state.issueDate.plusDays(it.toLong()) }, allowFuture = true, onSelect = actions.updateExpirationDate)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(T.small)) {
                    NumericField(draft.validityDaysInput, actions.updateValidityDays, stringResource(R.string.validity_days),
                        draft.validityDaysInput.isNotEmpty() && (draft.validityDays == null || draft.validityDays !in 1..3650),
                        placeholder = stringResource(R.string.custom_validity_days_example))
                    Text(stringResource(R.string.custom_validity_counted_from, dateText(state.issueDate)),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            draft.validityDays?.takeIf { it in 1..3650 && !draft.useExpirationDate }?.let { days ->
                ContentCard {
                    Text(stringResource(R.string.custom_validity_expiration_label), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateText(state.issueDate.plusDays(days.toLong())), style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(stringResource(R.string.validity_confirmation_guidance),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    }
    }
}

@Composable
private fun RecipeSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(T.padding)) {
        DateField(stringResource(R.string.issue_date), state.issueDate, onSelect = actions.updateIssueDate)
        FormChoiceField(stringResource(R.string.registration_validity_label),
            state.validityDays?.takeIf { it in 1..3650 }?.let { days ->
                if (state.validityDateMode) dateText(state.issueDate.plusDays(days.toLong()))
                else pluralStringResource(R.plurals.interval_days, days, days)
            } ?: stringResource(R.string.select_date),
            actions.openTypeEditor, required = true)
    }
}

@Composable
private fun RecipeNameSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    FormField(state.prescriptionTitle, { value -> actions.update { it.copy(prescriptionTitle = value, isTitleCustomized = value.isNotBlank()) } },
        stringResource(R.string.optional_prescription_name), stringResource(R.string.prescription_name_example), required = true, showLabel = false)
}

@Composable
private fun RenewalOptionsSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    Section(stringResource(R.string.renewal_reminder_title)) {
        ToggleRow(stringResource(R.string.enable_expiration_and_renewal_alerts), state.renewalReminderEnabled) { enabled -> actions.update { it.copy(renewalReminderEnabled = enabled) } }
        if (state.renewalReminderEnabled) {
            DayOptions(state.renewalReminderDaysBefore,
                (listOf(0, 3, 5, 7, 10, 15) + state.renewalReminderDaysBefore).distinct().sorted()
                    .filter { it in 0..(state.validityDays ?: 180) }) { days -> actions.update { it.copy(renewalReminderDaysBefore = days) } }
        } else Text(stringResource(R.string.prescription_alerts_disabled), style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicationSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    if (state.addToPrescriptionId != null) Text(stringResource(R.string.medication_destination_recipe, state.addToPrescriptionTitle), style = MaterialTheme.typography.bodyMedium)
    state.medications.forEachIndexed { index, medication ->
        if (index != state.editingMedicationIndex || !state.showMedicationEditor) {
            RecordSummarySection(medication.title) {
                Text(medication.dosage.ifBlank { stringResource(R.string.concentration_not_provided) }, style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(T.gap)) {
                    TextButton(onClick = { actions.editMedication(index) }) { Text(stringResource(R.string.action_edit)) }
                    TextButton(onClick = { actions.removeMedication(index) }) { Text(stringResource(R.string.action_remove)) }
                }
            }
        }
    }
    if (state.showMedicationEditor) {
        val draft = state.currentMedicationDraft
        Section(if (state.entryMode == WizardEntryMode.STANDALONE_MEDICATION) "" else if (state.editingMedicationIndex != null) stringResource(R.string.edit_medication) else stringResource(R.string.add_medication), prominent = state.entryMode != WizardEntryMode.STANDALONE_MEDICATION) {
            FormField(draft.title, { value -> actions.update { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = value)) } }, stringResource(R.string.medication_name), stringResource(R.string.medication_name_example), required = true, showLabel = false)
            if (state.editingMedicationIndex == null) {
                state.availableMedications.filter { draft.title.isNotBlank() && it.title.contains(draft.title, true) && it.id !in state.existingPrescriptionMedicationIds && state.medications.none { existing -> existing.id == it.id } }.take(3).forEach { medication ->
                    OutlinedButton(onClick = {
                        actions.selectMedication(medication)
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.existing_medication, (medication.title + " " + medication.dosage).trim())) }
                }
            }
            if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) Text(stringResource(R.string.prescription_instructions_readonly_guidance), style = MaterialTheme.typography.bodySmall)
            if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && state.editingMedicationIndex == null) {
                SecondaryActionButton(stringResource(R.string.add_another_medication), actions.addMedication, Modifier.fillMaxWidth())
            }
            if (state.medications.isNotEmpty() || (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && draft != MedicationDraft())) TextButton(onClick = actions.cancelMedication) { Text(if (state.editingMedicationIndex != null) stringResource(R.string.cancel_edit) else stringResource(R.string.cancel_add)) }
        }
    } else {
        SecondaryActionButton(stringResource(if (state.medications.isEmpty()) R.string.add_medication else R.string.add_another_medication), actions.addMedication, Modifier.fillMaxWidth())
        Text(pluralStringResource(R.plurals.registration_medication_count, state.medications.size, state.medications.size), style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickupSection(state: PrescriptionWizardUiState, actions: WizardCallbacks) {
    state.medications.forEachIndexed { index, medication ->
        val update: ((MedicationDraft) -> MedicationDraft) -> Unit = { transform -> actions.update { current -> current.copy(medications = current.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) } }
        Section(medication.title) {
            ToggleRow(stringResource(R.string.track_recurring_pickup), medication.hasRecurringPickup) { enabled -> update {
                val suggestedDate = it.lastPickupDate ?: if (enabled && !it.hasPickupHistory) LocalDate.now() else null
                it.copy(hasRecurringPickup = enabled, lastPickupDate = suggestedDate,
                    initialPickupDate = it.initialPickupDate ?: suggestedDate.takeIf { _ -> !it.hasPickupHistory })
            } }
            if (medication.hasRecurringPickup) {
                if (medication.hasPickupHistory) {
                    Text(stringResource(R.string.last_pickup_date, medication.lastPickupDate?.let(::dateText).orEmpty()))
                    Text(stringResource(R.string.pickup_history_edit_guidance), style = MaterialTheme.typography.bodySmall)
                } else DateField(stringResource(R.string.last_pickup), medication.lastPickupDate) { date -> update { it.copy(lastPickupDate = date, initialPickupDate = date) } }
                Text(stringResource(R.string.actual_pickup_date_guidance), style = MaterialTheme.typography.bodySmall)
                NumericField(medication.pickupIntervalDisplayInput, { value -> actions.updatePickupInterval(index, value) }, stringResource(R.string.pickup_interval), medication.pickupIntervalDays == null || medication.pickupIntervalDays !in 1..3650)
                medication.lastPickupDate?.let { date -> medication.pickupIntervalDays?.takeIf { it in 1..3650 }?.let { interval -> Text(stringResource(R.string.next_pickup_date, dateText(PickupInterval(interval, medication.intervalUnit).nextAfter(date))), fontWeight = FontWeight.SemiBold) } }
                medication.copy(pickupReminderDaysBefore = 0).pickupError()?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            } else Text(stringResource(R.string.no_medication_pickup_reminders), style = MaterialTheme.typography.bodySmall)
            Field(medication.replenishmentQuantity, { value -> update { it.copy(replenishmentQuantity = value) } }, stringResource(R.string.optional_pickup_quantity), stringResource(R.string.pickup_quantity_example))
            Text(stringResource(R.string.pickup_quantity_guidance), style = MaterialTheme.typography.bodySmall)

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
                Text(stringResource(R.string.reminder_lead_time), style = MaterialTheme.typography.labelLarge)
                DayOptions(medication.pickupReminderDaysBefore, listOf(0, 1, 3, 5, 7).filter { it <= medication.intervalDurationDays }) { days -> update { it.copy(pickupReminderDaysBefore = days) } }
                medication.pickupError()?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
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
                Section(stringResource(R.string.pickup_location)) {
                RadioChoiceList(MedicationSource.entries.map { stringResource(it.labelRes) }, MedicationSource.entries.indexOf(medication.source),
                    { choice -> update { it.copy(source = MedicationSource.entries[choice]) } })
                Field(medication.sourceName, { value -> update { it.copy(sourceName = value) } }, stringResource(R.string.optional_location_name), stringResource(R.string.pickup_location_example))
                }
                Section(stringResource(R.string.tracking_prescription)) {
                if (state.addToPrescriptionId != null) {
                    Text(state.addToPrescriptionTitle)
                } else {
                val offersCurrentRecipe = state.entryMode == WizardEntryMode.FULL_PRESCRIPTION
                val prescriptions = state.availablePrescriptions.filter { p -> p.id != state.previousPrescriptionId && p.medicationIds.contains(medication.id) }
                val labels = listOf(stringResource(R.string.no_linked_prescription)) +
                    (if (offersCurrentRecipe) listOf(stringResource(if (state.previousPrescriptionId != null) R.string.continue_with_new_prescription else R.string.use_this_prescription)) else emptyList()) +
                    prescriptions.map { it.title }
                val offset = if (offersCurrentRecipe) 2 else 1
                val selected = when {
                    offersCurrentRecipe && medication.linkToCurrentRecipe -> 1
                    !medication.linkToCurrentRecipe && medication.linkedPrescriptionId == null -> 0
                    !medication.linkToCurrentRecipe -> prescriptions.indexOfFirst { it.id == medication.linkedPrescriptionId }.let { if (it < 0) -1 else it + offset }
                    else -> -1
                }
                RadioChoiceList(labels, selected, { choice -> update {
                    when {
                        choice == 0 -> it.copy(linkedPrescriptionId = null, linkToCurrentRecipe = false)
                        offersCurrentRecipe && choice == 1 -> it.copy(linkToCurrentRecipe = true, linkedPrescriptionId = null, trackingArchived = false)
                        else -> it.copy(linkedPrescriptionId = prescriptions[choice - offset].id, linkToCurrentRecipe = false, trackingArchived = false)
                    }
                } })
                if (state.previousPrescriptionId != null && medication.linkedPrescriptionId == state.previousPrescriptionId && !medication.linkToCurrentRecipe) Text(stringResource(R.string.renewal_tracking_archive_guidance), style = MaterialTheme.typography.bodySmall)
                }
                }

                medication.pickupError()?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
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
            GroupedValueRow(stringResource(R.string.issue_date), dateText(state.issueDate),
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeIdentityStep)) }) else null)
            val expiration = state.validityDays?.takeIf { it > 0 }?.let { state.issueDate.plusDays(it.toLong()) }
            val validity = expiration?.let { stringResource(R.string.expiration_date, dateText(it)) }
            GroupedValueRow(stringResource(R.string.recipe_type_and_validity_title),
                listOfNotNull(state.type?.let { stringResource(it.labelRes) } ?: stringResource(R.string.label_unknown_prescription_type), validity).joinToString("\n"),
                onEdit = if (editable) actions.openTypeEditor else null, stacked = true)
            GroupedValueRow(stringResource(R.string.recipe_renewal_options_title),
                if (state.renewalReminderEnabled) stringResource(R.string.renewal_reminder, reminderText(state.renewalReminderDaysBefore)) else stringResource(R.string.expiration_and_renewal_alerts_disabled),
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.renewalOptionsStep)) }) else null, stacked = true)
            GroupedValueRow(stringResource(R.string.responsible_professional),
                listOf(state.doctorName, state.doctorRegistration, state.doctorSpecialty).filter { it.isNotBlank() }.joinToString("\n").ifBlank { stringResource(R.string.review_not_provided) },
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeDetailsStep)) }) else null, stacked = true)
            GroupedValueRow(stringResource(R.string.recipe_notes_group), state.notes.ifBlank { stringResource(R.string.review_no_notes) },
                onEdit = if (editable) ({ actions.goToStep(requireNotNull(state.recipeDetailsStep)) }) else null, stacked = true)
        }
    }
    if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
        if (state.medications.isNotEmpty()) GroupedSection(stringResource(R.string.medications_count, state.medications.size)) {
            state.medications.forEachIndexed { index, item ->
                GroupedValueRow(item.title, item.dosage.ifBlank { stringResource(R.string.concentration_not_provided) }, divider = index > 0)
            }
        }
        if (editable && !state.canSave) Text((state.recipeError ?: state.medicationError ?: state.pickupError)?.asString() ?: stringResource(R.string.review_fields_before_saving), color = MaterialTheme.colorScheme.error)
        return
    }
    ReviewSection(stringResource(R.string.medications_count, state.allMedications.size),
        onEdit = if (editable) ({ actions.goToStep(state.medicationStep) }) else null) {
        if (state.allMedications.isEmpty()) Text(stringResource(R.string.recipe_medications_optional_summary))
        state.allMedications.forEachIndexed { index, medication ->
            ReviewRecordCard(medication.title.ifBlank { stringResource(R.string.name_not_provided) }, medication.dosage,
                onEdit = if (editable) ({ actions.editMedication(index) }) else null,
                modifier = Modifier.testTag("review-medication-$index"))
        }
    }
    if (state.allMedications.isNotEmpty()) GroupedSection(stringResource(R.string.review_pickups_title),
        onEdit = if (editable) ({ actions.goToStep(if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) state.medicationStep else state.pickupStep) }) else null) {
        state.allMedications.forEachIndexed { index, medication ->
            SummaryItemGroup(medication.title, divider = index > 0,
                modifier = Modifier.padding(T.padding).testTag("review-pickup-$index")) {
                if (medication.replenishmentQuantity.isNotBlank()) Text(stringResource(R.string.pickup_quantity, medication.replenishmentQuantity))
                if (medication.hasRecurringPickup) {
                    val date = medication.lastPickupDate
                    val interval = medication.pickupIntervalDays
                    if (date != null && interval != null && interval > 0) Text(stringResource(R.string.next_pickup_date, dateText(PickupInterval(interval, medication.intervalUnit).nextAfter(date))))
                    else Text(stringResource(R.string.complete_pickup_schedule), color = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.pickup_reminder, reminderText(medication.pickupReminderDaysBefore)))
                    if (medication.sourceName.isNotBlank()) Text(stringResource(R.string.pickup_place, medication.sourceName))
                    val pickupRecipeTitle = when {
                        medication.linkToCurrentRecipe -> recipeTitle
                        medication.linkedPrescriptionId == state.addToPrescriptionId && state.addToPrescriptionId != null -> state.addToPrescriptionTitle
                        else -> state.availablePrescriptions.find { it.id == medication.linkedPrescriptionId }?.title
                    }
                    if (medication.linkToCurrentRecipe || medication.linkedPrescriptionId != null) Text(stringResource(R.string.pickup_recipe_summary, pickupRecipeTitle ?: stringResource(R.string.pickup_selected_recipe)))
                    else Text(stringResource(R.string.no_linked_prescription))
                } else Text(stringResource(R.string.recurring_pickup_disabled))
            }
        }
        if (editable && state.pickupOptionsStep != null && state.entryMode != WizardEntryMode.FULL_PRESCRIPTION) Column(Modifier.fillMaxWidth().padding(T.padding), verticalArrangement = Arrangement.spacedBy(T.small)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SecondaryActionButton(stringResource(R.string.design_edit_reminders), { actions.goToStep(requireNotNull(state.pickupOptionsStep)) }, Modifier.fillMaxWidth())
            SecondaryActionButton(stringResource(R.string.guided_edit_context), { actions.goToStep(requireNotNull(state.pickupContextStep)) }, Modifier.fillMaxWidth())
        }
    }
    if (editable && !state.canSave) Text((state.recipeError ?: state.medicationError ?: state.pickupError)?.asString() ?: stringResource(R.string.review_fields_before_saving), color = MaterialTheme.colorScheme.error)
}
@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "", singleLine: Boolean = true, required: Boolean = false) {
    FormField(value, onChange, label, placeholder, singleLine, required = required)
}

@Composable
private fun NumericField(value: String, onChange: (String) -> Unit, label: String, error: Boolean = false, placeholder: String = "") {
    FormField(value, { onChange(it.filter(Char::isDigit).take(4)) }, label, placeholder,
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
    FormChoiceField(label, date?.let { if (it == LocalDate.now()) stringResource(R.string.today) else dateText(it) } ?: stringResource(R.string.select_date), onClick = {
        val initial = date ?: LocalDate.now()
        DatePickerDialog(context, { _, year, month, day -> onSelect(LocalDate.of(year, month + 1, day)) }, initial.year, initial.monthValue - 1, initial.dayOfMonth).apply { if (!allowFuture) datePicker.maxDate = System.currentTimeMillis() }.show()
    }, required = true)
}

private fun dateText(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
@Composable
private fun reminderText(days: Int): String = if (days == 0) stringResource(R.string.reminder_on_day) else pluralStringResource(R.plurals.reminder_days_before, days, days)

@Preview(showBackground = true)
@Composable
private fun RegistrationPreview() { AppTheme { PrescriptionWizardContent(PrescriptionWizardUiState(), WizardCallbacks()) } }

@Preview(showBackground = true)
@Composable
private fun ReviewPreview() {
    AppTheme { PrescriptionWizardContent(PrescriptionWizardUiState(currentStep = 9, showMedicationEditor = false, medications = listOf(MedicationDraft(id = "preview", title = stringResource(R.string.preview_medication), dosageOverride = "50 mg", hasRecurringPickup = true, lastPickupDate = LocalDate.now().minusDays(25)))), WizardCallbacks()) }
}

@Composable
private fun RecipeRegistrationSuccess(state: PrescriptionWizardUiState, actions: WizardCallbacks, modifier: Modifier) {
    val canAdd = state.recordState == RecordState.ACTIVE
    WizardQuestionScreen(
        question = stringResource(R.string.prescription_registered),
        onBack = actions.complete,
        progress = 1f,
        primaryButtonText = stringResource(if (canAdd) R.string.registration_add_medications else R.string.view_prescription),
        primaryButtonEnabled = true,
        onPrimaryClick = if (canAdd) actions.addSavedMedication else actions.complete,
        modifier = modifier,
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.registration_complete),
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(T.control))
        Text(state.prescriptionTitle, style = MaterialTheme.typography.titleLarge)
        if (canAdd) {
            Text(stringResource(R.string.registration_add_medications_prompt), style = MaterialTheme.typography.bodyLarge)
            SecondaryActionButton(stringResource(R.string.registration_not_now), actions.complete, Modifier.fillMaxWidth())
        }
    }
}
