package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.domain.model.MedicationItem

data class MedicationsUiState(
    val medications: List<MedicationItem> = emptyList(),
    val filteredMedications: List<MedicationItem> = emptyList(),
    val searchQuery: String = "",
    val selectedSourceFilter: String = "TODOS",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
)
