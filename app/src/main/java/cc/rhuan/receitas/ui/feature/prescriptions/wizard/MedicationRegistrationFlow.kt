package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.IntervalUnit
import cc.rhuan.receitas.ui.components.wizard.WizardQuestionScreen
import cc.rhuan.receitas.ui.components.*
import cc.rhuan.receitas.ui.theme.DesignTokens as T
import java.util.UUID

/** A child registration edits a private draft and returns only its confirmed medication. */
@Composable
internal fun MedicationRegistrationFlow(
    state: PrescriptionWizardUiState,
    update: (PrescriptionWizardUiState) -> Unit,
    dismiss: () -> Unit,
    complete: (MedicationDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    val advance = {
        if (state.currentStepError == null) {
            val committed = if (state.currentStep == state.medicationStep) {
                val item = state.currentMedicationDraft
                state.copy(medications = listOf(item.copy(id = item.id ?: UUID.randomUUID().toString(), title = item.title.trim())),
                    currentMedicationDraft = MedicationDraft(), showMedicationEditor = false)
            } else state
            update(committed.copy(currentStep = if (state.reviewEditStep != null) committed.maxSteps else committed.currentStep + 1,
                reviewEditStep = null, errorMessage = null, hasUnsavedChanges = true))
        }
    }
    val back = {
        if (state.currentStep == 1 && state.reviewEditStep == null) dismiss()
        else {
            val target = state.reviewEditStep?.let { state.maxSteps } ?: (state.currentStep - 1)
            update(state.copy(currentStep = target, reviewEditStep = null, errorMessage = null,
                showMedicationEditor = target == 1,
                editingMedicationIndex = if (target == 1) 0 else state.editingMedicationIndex,
                currentMedicationDraft = if (target == 1) state.medications.single() else state.currentMedicationDraft))
        }
    }
    BackHandler(onBack = back)
    PrescriptionWizardContent(state, WizardCallbacks(
        update = { update(it(state).copy(hasUnsavedChanges = true, errorMessage = null)) },
        next = advance, skip = advance, back = back, cancelMedication = dismiss,
        save = { if (state.canSave) complete(state.allMedications.single()) },
        updatePickupInterval = { index, value -> update(state.copy(medications = state.medications.mapIndexed { i, item ->
            if (i == index) item.copy(pickupIntervalInput = value, pickupIntervalDays = value.toIntOrNull(), intervalUnit = IntervalUnit.DAYS) else item
        })) },
        selectMedication = { item -> update(state.copy(currentMedicationDraft = MedicationDraft(
            id = item.id, title = item.title, activeIngredient = item.activeIngredient.orEmpty(), dosageOverride = item.dosage,
            form = item.form, intervalUnit = item.intervalUnit, linkedPrescriptionId = item.linkedPrescriptionId,
            trackingArchived = item.trackingArchived, source = item.source, sourceName = item.sourceName.orEmpty(),
            hasRecurringPickup = item.hasRecurringPickup, lastPickupDate = item.lastRefillDate,
            hasPickupHistory = item.pickupHistory.isNotEmpty(), pickupIntervalDays = item.refillCycleDays,
            pickupIntervalInput = item.refillCycleDays.toString(), pickupReminderDaysBefore = item.pickupReminderDaysBefore,
            replenishmentQuantity = item.quantity, availabilityNote = item.instructions.orEmpty()))) },
        editMedication = { _ -> update(state.copy(currentStep = 1, reviewEditStep = 1, showMedicationEditor = true,
            currentMedicationDraft = state.medications.single(), editingMedicationIndex = 0)) },
        goToStep = { step -> update(state.copy(currentStep = step,
            reviewEditStep = if (state.currentStep == state.maxSteps) step else state.reviewEditStep)) },
    ), modifier)
}

@Composable
internal fun MedicationCollectionScreen(
    state: PrescriptionWizardUiState,
    actions: WizardCallbacks,
    add: () -> Unit,
    edit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    WizardQuestionScreen(
        question = stringResource(R.string.prescription_medications_question),
        onBack = actions.back, progress = state.progress,
        primaryButtonText = stringResource(if (state.reviewEditStep != null) R.string.return_to_review else R.string.action_advance),
        primaryButtonEnabled = !state.isLoading && state.medicationError == null,
        onPrimaryClick = { actions.goToStep(if (state.reviewEditStep != null) state.maxSteps else state.optionalDetailsStep) },
        onSkip = if (state.medications.isEmpty()) actions.next else null,
        errorMessage = state.errorMessage, modifier = modifier,
    ) {
        SecondaryActionButton(stringResource(R.string.add_medication), add, Modifier.fillMaxWidth())
        if (state.medications.isNotEmpty()) GroupedSection(pluralStringResource(R.plurals.registration_medication_count, state.medications.size, state.medications.size)) {
            state.medications.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FormRecordRow(item.title, item.dosage, { edit(index) },
                    listOf(ContextAction(stringResource(R.string.action_remove), destructive = true) { actions.removeMedication(index) }))
            }
        }
    }
}
