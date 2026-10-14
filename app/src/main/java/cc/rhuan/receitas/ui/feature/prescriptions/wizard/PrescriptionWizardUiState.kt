package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
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
    val doseQuantity: String = "1",
    val doseUnit: String = "Comprimido",
    val doseAmount: String = "1 Comprimido",
    val frequencyOption: String = "",
    val durationOption: String = "30 dias",
    val source: MedicationSource = MedicationSource.COMPRA_PROPRIA,
    val sourceName: String = "",
    val hasRecurringPickup: Boolean = false,
    val lastPickupDate: LocalDate? = null,
    val pickupIntervalDays: Int? = 30,
    val pickupIntervalInput: String = "30",
    val pickupReminderDaysBefore: Int = 3,
    val replenishmentQuantity: String = "",
    val currentStock: Int? = null,
    val stockAlertEnabled: Boolean = true,
    val minStockThreshold: Int = 10,
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

    val isStep2Complete: Boolean
        get() = dosage.isNotBlank() || title.isNotBlank()

    val isStep3Complete: Boolean
        get() = prescribedPosology.isNotBlank() || frequencyOption.isNotBlank()

    val isComplete: Boolean
        get() = isStep1Complete

    fun pickupError(today: LocalDate = LocalDate.now()): String? = when {
        !hasRecurringPickup -> null
        lastPickupDate == null -> "Informe a data da última retirada."
        lastPickupDate.isAfter(today) -> "A última retirada não pode estar no futuro."
        pickupIntervalDays == null || pickupIntervalDays !in 1..3650 -> "Informe um intervalo de 1 a 3650 dias."
        pickupReminderDaysBefore < 0 || pickupReminderDaysBefore > pickupIntervalDays -> "A antecedência deve caber no intervalo de retirada."
        else -> null
    }
}

data class PrescriptionWizardUiState(
    val entryMode: WizardEntryMode = WizardEntryMode.FULL_PRESCRIPTION,
    val editingPrescriptionId: String? = null,
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
    val showAddAnotherDialog: Boolean = false,

    // Prescription Identification & Type
    val prescriptionTitle: String = "",
    val isTitleCustomized: Boolean = false,
    val issueDate: LocalDate = LocalDate.now(),
    val isStep4Skipped: Boolean = false,
    val type: PrescriptionType? = PrescriptionType.CONTINUO,
    val validityDays: Int? = 180,
    val validityDaysInput: String = "180",
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
    val initialLoadFailed: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
) : Serializable {
    val maxSteps: Int get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION) 3 else 4
    val medicationStep: Int get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION) 1 else 2
    val pickupStep: Int get() = maxSteps - 1
    val stepLabels: List<String> get() = if (entryMode == WizardEntryMode.STANDALONE_MEDICATION) listOf("Medicamento", "Retirada", "Revisão") else listOf("Receita", "Medicamentos", "Retiradas", "Revisão")
    val progress: Float get() = currentStep.toFloat() / maxSteps
    val currentMedicationIndex: Int get() = (editingMedicationIndex ?: medications.size) + 1
    val hasMultipleMedications: Boolean get() = medications.isNotEmpty()
    val allMedications: List<MedicationDraft> get() {
        if (!showMedicationEditor || currentMedicationDraft == MedicationDraft()) return medications
        val index = editingMedicationIndex
        return if (index != null && index in medications.indices) medications.mapIndexed { i, draft -> if (i == index) currentMedicationDraft else draft }
        else medications + currentMedicationDraft
    }
    val recipeError: String? get() = when {
        initialLoadFailed -> "Não foi possível carregar a receita. Tente novamente antes de editar."
        entryMode == WizardEntryMode.STANDALONE_MEDICATION -> null
        issueDate.isAfter(LocalDate.now()) -> "A data de emissão não pode estar no futuro."
        validityDays == null || validityDays !in 1..3650 -> "Informe uma validade de 1 a 3650 dias."
        renewalReminderEnabled && renewalReminderDaysBefore !in 0..validityDays -> "A antecedência do lembrete deve caber na validade."
        else -> null
    }
    val medicationError: String? get() = when {
        allMedications.isEmpty() -> "Adicione pelo menos um medicamento."
        allMedications.any { !it.isComplete } -> "Informe o nome de cada medicamento."
        allMedications.mapNotNull { it.id }.distinct().size != allMedications.mapNotNull { it.id }.size -> "Esse medicamento já está na receita. Edite o item existente."
        else -> null
    }
    val pickupError: String? get() = allMedications.firstNotNullOfOrNull { draft -> draft.pickupError()?.let { draft.title + ": " + it } }
    val currentStepError: String? get() = when (currentStep) {
        medicationStep -> medicationError
        pickupStep -> pickupError
        maxSteps -> recipeError ?: medicationError ?: pickupError
        else -> recipeError
    }
    val canAdvanceCurrentStep: Boolean get() = currentStepError == null && !isLoading
    val canSave: Boolean get() = recipeError == null && medicationError == null && pickupError == null && !isLoading
}
