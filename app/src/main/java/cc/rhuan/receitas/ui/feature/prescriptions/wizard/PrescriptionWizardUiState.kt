package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate
import java.time.LocalTime

data class MedicationDraft(
    val id: String? = null,
    val title: String = "",
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
    val dosageValue: String = "",
    val dosageUnit: String = "mg",
    val dosageOverride: String = "",
    val doseAmount: String = "1 comprimido",
    val frequencyHours: Int = 8,
    val durationDays: Int = 30,
    val firstDoseTime: LocalTime = LocalTime.of(8, 0),
    val source: MedicationSource = MedicationSource.COMPRA_PROPRIA,
    val sourceName: String = "",
    val hasRecurringPickup: Boolean = false,
    val lastPickupDate: LocalDate? = null,
    val pickupIntervalDays: Int? = 30,
    val pickupReminderDaysBefore: Int = 3,
    val currentStock: Int? = null,
    val stockAlertEnabled: Boolean = true,
    val minStockThreshold: Int = 5,
    val availabilityNote: String = ""
) {
    val dosage: String
        get() = when {
            dosageOverride.isNotBlank() -> dosageOverride
            dosageValue.isNotBlank() -> "$dosageValue $dosageUnit".trim()
            else -> ""
        }

    val isStep1Complete: Boolean
        get() = title.isNotBlank() && dosage.isNotBlank()

    val isStep2Complete: Boolean
        get() = doseAmount.isNotBlank() && frequencyHours > 0

    val isComplete: Boolean
        get() = isStep1Complete && isStep2Complete
}

data class PrescriptionWizardUiState(
    val editingPrescriptionId: String? = null,
    val currentStep: Int = 1,

    // Step 1 - 3: Medications
    val medications: List<MedicationDraft> = emptyList(),
    val currentMedicationDraft: MedicationDraft = MedicationDraft(),
    val availableMedications: List<MedicationItem> = emptyList(),
    val showAddAnotherDialog: Boolean = false,

    // Step 4: Prescription Identification & Type
    val prescriptionTitle: String = "",
    val isTitleCustomized: Boolean = false,
    val issueDate: LocalDate = LocalDate.now(),
    val isStep4Skipped: Boolean = false,
    val type: PrescriptionType? = PrescriptionType.CONTINUO,
    val validityDays: Int? = 180,
    val renewalReminderEnabled: Boolean = true,
    val renewalReminderDaysBefore: Int = 5,

    // Step 5: Doctor
    val availableDoctors: List<Doctor> = emptyList(),
    val selectedDoctorId: String? = null,
    val doctorName: String = "",
    val doctorRegistration: String = "",
    val doctorSpecialty: String = "",
    val healthFacility: String = "",
    val doctorPhone: String = "",
    val showExtraDoctorFields: Boolean = false,

    // Step 6: Notes, Photo and Review
    val notes: String = "",
    val photoUri: String? = null,

    // Status
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val progress: Float
        get() = currentStep / 6f

    val currentMedicationIndex: Int
        get() = medications.size + 1

    val hasMultipleMedications: Boolean
        get() = medications.isNotEmpty()

    val canAdvanceFromStep1: Boolean
        get() = currentMedicationDraft.isStep1Complete || medications.isNotEmpty()

    val canAdvanceFromStep2: Boolean
        get() = currentMedicationDraft.isStep2Complete || medications.isNotEmpty()

    val canAdvanceFromStep3: Boolean
        get() = true

    val canAdvanceFromStep4: Boolean
        get() = true

    val canAdvanceFromStep5: Boolean
        get() = true

    val canSave: Boolean
        get() = (medications.isNotEmpty() || currentMedicationDraft.isComplete) &&
                prescriptionTitle.isNotBlank()
}
