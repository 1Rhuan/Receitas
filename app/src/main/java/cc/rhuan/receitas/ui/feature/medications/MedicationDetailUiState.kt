package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem

data class MedicationDetailUiState(val medication: MedicationItem? = null, val isLoading: Boolean = true,
    val errorMessage: String? = null, val isDeleted: Boolean = false,
    val prescriptions: List<PrescriptionItem> = emptyList(),
    val pickupPrescription: PrescriptionItem? = null)
