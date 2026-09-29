package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate

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
    val doseAmount: String = "1 comprimido",
    val frequencyOption: String = "3 vezes ao dia (8 em 8 horas)",
    val durationOption: String = "30 dias",
    val source: MedicationSource = MedicationSource.COMPRA_PROPRIA,
    val sourceName: String = "",
    val hasRecurringPickup: Boolean = false,
    val lastPickupDate: LocalDate? = null,
    val pickupIntervalDays: Int? = 30,
    val pickupReminderDaysBefore: Int = 3,
    val currentStock: Int? = 30,
    val stockAlertEnabled: Boolean = true,
    val minStockThreshold: Int = 10,
    val availabilityNote: String = "",
) {
    val dosage: String
        get() = when {
            dosageOverride.isNotBlank() -> dosageOverride
            dosageValue.isNotBlank() -> "$dosageValue $dosageUnit".trim()
            else -> ""
        }

    val isStep1Complete: Boolean
        get() = title.isNotBlank()

    val isStep2Complete: Boolean
        get() = dosage.isNotBlank() || title.isNotBlank()

    val isStep3Complete: Boolean
        get() = prescribedPosology.isNotBlank() || frequencyOption.isNotBlank()

    val isComplete: Boolean
        get() = isStep1Complete && isStep2Complete
}

data class PrescriptionWizardUiState(
    val entryMode: WizardEntryMode = WizardEntryMode.FULL_PRESCRIPTION,
    val editingPrescriptionId: String? = null,
    val currentStep: Int = 1,

    // Drafts and Medications
    val medications: List<MedicationDraft> = emptyList(),
    val currentMedicationDraft: MedicationDraft = MedicationDraft(),
    val availableMedications: List<MedicationItem> = emptyList(),
    val showAddAnotherDialog: Boolean = false,

    // Prescription Identification & Type
    val prescriptionTitle: String = "",
    val isTitleCustomized: Boolean = false,
    val issueDate: LocalDate = LocalDate.now(),
    val isStep4Skipped: Boolean = false,
    val type: PrescriptionType? = PrescriptionType.CONTINUO,
    val validityDays: Int? = 180,
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
    val showExtraDoctorFields: Boolean = false,

    // Notes, Photo and Review
    val notes: String = "",
    val photoUri: String? = null,

    // Status
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
) {
    val maxSteps: Int
        get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION) 6 else 10

    val progress: Float
        get() = currentStep.toFloat() / maxSteps.toFloat()

    val currentMedicationIndex: Int
        get() = medications.size + 1

    val hasMultipleMedications: Boolean
        get() = medications.isNotEmpty()

    val canAdvanceCurrentStep: Boolean
        get() = when (currentStep) {
            1 -> currentMedicationDraft.title.isNotBlank() || medications.isNotEmpty()
            2 -> currentMedicationDraft.dosage.isNotBlank() || medications.isNotEmpty()
            3 -> currentMedicationDraft.prescribedPosology.isNotBlank() || currentMedicationDraft.frequencyOption.isNotBlank() || medications.isNotEmpty()
            4 -> true
            5 -> true
            else -> true
        }

    val canSave: Boolean
        get() = (medications.isNotEmpty() || currentMedicationDraft.title.isNotBlank())
}
