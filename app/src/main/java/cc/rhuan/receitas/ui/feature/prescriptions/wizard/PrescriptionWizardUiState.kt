package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.text.TextMessage

import cc.rhuan.receitas.R

import cc.rhuan.receitas.domain.model.*
import java.time.temporal.ChronoUnit
import java.io.Serializable
import java.time.LocalDate
import java.util.UUID

enum class WizardEntryMode {
    STANDALONE_MEDICATION,
    FULL_PRESCRIPTION,
}

data class MedicationDraft(
    val id: String? = null,
    val title: String = "",
    val activeIngredient: String = "",
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
    val dosageValue: String = "",
    val dosageUnit: String = "mg",
    val dosageOverride: String = "",
    val prescribedPosology: String = "",
    val prescribedQuantity: String = "",
    val source: MedicationSource = MedicationSource.SELF_PURCHASE,
    val sourceName: String = "",
    val hasRecurringPickup: Boolean = false,
    val lastPickupDate: LocalDate? = null,
    val hasPickupHistory: Boolean = false,
    val initialPickupDate: LocalDate? = null,
    val pickupIntervalDays: Int? = 30,
    val pickupIntervalInput: String = "30",
    val intervalUnit: IntervalUnit = IntervalUnit.DAYS,
    val linkedPrescriptionId: String? = null,
    val linkToCurrentRecipe: Boolean = false,
    val trackingArchived: Boolean = false,
    val pickupReminderDaysBefore: Int = 3,
    val replenishmentQuantity: String = "",
    val availabilityNote: String = "",
) : Serializable {
    val dosage: String
        get() = when {
            dosageOverride.isNotBlank() -> dosageOverride
            dosageValue.isNotBlank() -> "$dosageValue $dosageUnit".trim()
            else -> ""
        }

    val isStep1Complete: Boolean
        get() = title.isNotBlank()

    val isComplete: Boolean
        get() = isStep1Complete

    val intervalDurationDays: Int get() = pickupIntervalDays?.takeIf { it in 1..3650 }?.let { amount ->
        val date = lastPickupDate ?: LocalDate.now()
        ChronoUnit.DAYS.between(date, PickupInterval(amount, intervalUnit).nextAfter(date)).toInt()
    } ?: 30

    // Legacy calendar intervals keep their schedule until the user edits the days field.
    val pickupIntervalDisplayInput: String get() = if (intervalUnit != IntervalUnit.DAYS && pickupIntervalDays in 1..3650) intervalDurationDays.toString() else pickupIntervalInput

    fun pickupError(today: LocalDate = LocalDate.now()): TextMessage? = when {
        !hasRecurringPickup -> null
        lastPickupDate == null -> TextMessage(R.string.last_pickup_date_required)
        lastPickupDate.isAfter(today) -> TextMessage(R.string.last_pickup_future_error)
        pickupIntervalDays == null || pickupIntervalDays !in 1..3650 -> TextMessage(R.string.interval_range_error)
        pickupReminderDaysBefore < 0 || pickupReminderDaysBefore > ChronoUnit.DAYS.between(lastPickupDate, PickupInterval(pickupIntervalDays, intervalUnit).nextAfter(lastPickupDate)) -> TextMessage(R.string.pickup_reminder_interval_validation)
        else -> null
    }
}

data class PrescriptionTypeDraft(
    val type: PrescriptionType?,
    val validityDays: Int?,
    val validityDaysInput: String,
    val validityOrigin: String,
    val useExpirationDate: Boolean,
    val manualValidityDaysInput: String? = validityDaysInput.takeIf { validityOrigin == "USER_ENTERED" },
    val manualUseExpirationDate: Boolean = useExpirationDate,
) : Serializable {
    val error: TextMessage? get() = when {
        type == null -> TextMessage(R.string.prescription_type_required)
        validityDays == null || validityDays !in 1..3650 -> TextMessage(R.string.validity_range_error)
        else -> null
    }
}

data class PrescriptionWizardUiState(
    val entryMode: WizardEntryMode = WizardEntryMode.FULL_PRESCRIPTION,
    val medicationBelongsToPrescription: Boolean = false,
    val editingPrescriptionId: String? = null,
    val previousPrescriptionId: String? = null,
    val recordState: RecordState = RecordState.ACTIVE,
    val editingMedicationId: String? = null,
    val addToPrescriptionId: String? = null,
    val addToPrescriptionTitle: String = "",
    val existingPrescriptionMedicationIds: List<String> = emptyList(),
    val currentStep: Int = 1,
    val registrationId: String = UUID.randomUUID().toString(),
    val savedPrescriptionId: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val reviewEditStep: Int? = null,
    val editingMedicationIndex: Int? = null,
    val showMedicationEditor: Boolean = true,

    // Drafts and Medications
    val medications: List<MedicationDraft> = emptyList(),
    val currentMedicationDraft: MedicationDraft = MedicationDraft(),
    val availableMedications: List<MedicationItem> = emptyList(),
    val availablePrescriptions: List<PrescriptionItem> = emptyList(),

    // Prescription Identification & Type
    val prescriptionTitle: String = "",
    val isTitleCustomized: Boolean = false,
    val issueDate: LocalDate = LocalDate.now(),
    val type: PrescriptionType? = PrescriptionType.CONTINUOUS_USE,
    val validityDays: Int? = 180,
    val validityDaysInput: String = "180",
    val validityOrigin: String = "SUGGESTED",
    val validityDateMode: Boolean = false,
    val typeEditor: PrescriptionTypeDraft? = null,
    val renewalReminderEnabled: Boolean = true,
    val renewalReminderDaysBefore: Int = 5,

    // Doctor
    val availableDoctors: List<Doctor> = emptyList(),
    val selectedDoctorId: String? = null,
    val doctorName: String = "",
    val doctorRegistration: String = "",
    val doctorSpecialty: String = "",
    val healthFacility: String = "",
    val doctorPhone: String = "",

    // Notes and Review
    val notes: String = "",

    // Status
    val isLoading: Boolean = false,
    val initialLoadFailed: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
) : Serializable {
    val pickupOptionsStep: Int? get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION && allMedications.any { it.hasRecurringPickup }) pickupStep + 1 else null
    val pickupContextStep: Int? get() = pickupOptionsStep?.plus(1)
    val recipeNameStep: Int? get() = if (entryMode == WizardEntryMode.FULL_PRESCRIPTION) 1 else null
    val recipeIdentityStep: Int? get() = if (entryMode == WizardEntryMode.FULL_PRESCRIPTION) 2 else null
    val renewalOptionsStep: Int? get() = if (entryMode == WizardEntryMode.FULL_PRESCRIPTION) 3 else null
    val recipeDetailsStep: Int? get() = if (entryMode == WizardEntryMode.FULL_PRESCRIPTION) optionalDetailsStep else null
    val optionalDetailsStep: Int get() = if (entryMode == WizardEntryMode.FULL_PRESCRIPTION) 4 else (pickupContextStep ?: pickupStep) + 1
    val maxSteps: Int get() = optionalDetailsStep + 1
    // Medication steps belong exclusively to the medication registration flow.
    val medicationStep: Int get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION) 1 else -1
    val pickupStep: Int get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION) medicationStep + 1 else -2
    val progress: Float get() = currentStep.toFloat() / maxSteps
    val canSkipCurrentStep: Boolean get() {
        if (isLoading || isSaved || initialLoadFailed || typeEditor != null || currentStep == maxSteps) return false
        return when (currentStep) {
            recipeNameStep -> false
            optionalDetailsStep -> true
            medicationStep -> entryMode == WizardEntryMode.FULL_PRESCRIPTION && medicationError == null
            renewalOptionsStep -> recipeError == null
            pickupStep -> medicationError == null && allMedications.none { it.hasRecurringPickup }
            pickupOptionsStep, pickupContextStep -> medicationError == null && pickupError == null
            else -> false
        }
    }
    val allMedications: List<MedicationDraft> get() {
        if (!showMedicationEditor || currentMedicationDraft == MedicationDraft(linkedPrescriptionId = addToPrescriptionId)) return medications
        val index = editingMedicationIndex
        return if (index != null && index in medications.indices) medications.mapIndexed { i, draft -> if (i == index) currentMedicationDraft else draft }
        else medications + currentMedicationDraft
    }
    val recipeError: TextMessage? get() = when {
        initialLoadFailed -> TextMessage(R.string.prescription_load_before_edit_error)
        entryMode == WizardEntryMode.STANDALONE_MEDICATION -> null
        prescriptionTitle.isBlank() -> TextMessage(R.string.prescription_name_required)
        issueDate.isAfter(LocalDate.now()) -> TextMessage(R.string.prescription_issue_future_error)
        validityDays == null || validityDays !in 1..3650 -> TextMessage(R.string.validity_range_error)
        renewalReminderEnabled && renewalReminderDaysBefore !in 0..validityDays -> TextMessage(R.string.renewal_reminder_validity_error)
        else -> null
    }
    val medicationError: TextMessage? get() = when {
        allMedications.isEmpty() && entryMode == WizardEntryMode.STANDALONE_MEDICATION -> TextMessage(R.string.medication_required)
        allMedications.any { !it.isComplete } -> TextMessage(R.string.medication_names_required)
        allMedications.any { it.id in existingPrescriptionMedicationIds } -> TextMessage(R.string.medication_already_in_prescription)
        allMedications.mapNotNull { it.id }.distinct().size != allMedications.mapNotNull { it.id }.size -> TextMessage(R.string.medication_already_in_prescription)
        else -> null
    }
    val pickupError: TextMessage? get() = allMedications.firstNotNullOfOrNull { draft -> draft.pickupError()?.let { TextMessage(R.string.named_validation_error, listOf(draft.title, it)) } }
    val currentStepError: TextMessage? get() = when (currentStep) {
        maxSteps -> recipeError ?: medicationError ?: pickupError
        medicationStep -> medicationError
        pickupStep -> allMedications.firstNotNullOfOrNull { draft ->
            // Reminder lead time is configured on the next screen, after the schedule.
            draft.copy(pickupReminderDaysBefore = 0).pickupError()?.let { TextMessage(R.string.named_validation_error, listOf(draft.title, it)) }
        }
        pickupOptionsStep -> pickupError
        pickupContextStep -> pickupError
        renewalOptionsStep -> recipeError
        recipeIdentityStep -> copy(renewalReminderEnabled = false).recipeError
        recipeDetailsStep -> null
        recipeNameStep -> if (prescriptionTitle.isBlank()) TextMessage(R.string.prescription_name_required) else null
        else -> if (issueDate.isAfter(LocalDate.now())) TextMessage(R.string.prescription_issue_future_error) else null
    }
    val canSave: Boolean get() = recipeError == null && medicationError == null && pickupError == null && !isLoading
}
