package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.RefillStatus

data class PrescriptionMedicationDetail(
    val medication: MedicationItem,
    val refillStatus: RefillStatus,
)

data class PrescriptionDetailUiState(
    val prescription: PrescriptionItem? = null,
    val medications: List<PrescriptionMedicationDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val isDeleted: Boolean = false,
)
