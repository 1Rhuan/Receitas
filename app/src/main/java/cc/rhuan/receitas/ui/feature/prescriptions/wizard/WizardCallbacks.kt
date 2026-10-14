package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate

internal data class WizardCallbacks(
    val selectType: (PrescriptionType) -> Unit = {},
    val updateValidityDays: (String) -> Unit = {},
    val updateExpirationDate: (LocalDate) -> Unit = {},
    val selectDoctor: (Doctor) -> Unit = {},
    val selectMedication: (MedicationItem) -> Unit = {},
    val updatePickupInterval: (Int, String) -> Unit = { _, _ -> },
    val update: ((PrescriptionWizardUiState) -> PrescriptionWizardUiState) -> Unit = {},
    val next: () -> Unit = {}, val back: () -> Unit = {}, val close: () -> Unit = {},
    val retry: () -> Unit = {},
    val save: () -> Unit = {}, val commitMedication: () -> Unit = {}, val addMedication: () -> Unit = {},
    val editMedication: (Int) -> Unit = {}, val removeMedication: (Int) -> Unit = {},
    val cancelMedication: () -> Unit = {}, val goToStep: (Int) -> Unit = {}, val complete: () -> Unit = {},
)
