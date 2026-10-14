package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.domain.model.MedicationItem

data class MedicationDetailUiState(val medication: MedicationItem? = null, val isLoading: Boolean = true,
    val errorMessage: String? = null, val isDeleted: Boolean = false)
