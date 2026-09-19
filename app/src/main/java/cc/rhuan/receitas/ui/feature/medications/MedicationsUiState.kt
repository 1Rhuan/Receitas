package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.domain.model.MedicationItem

enum class MedicationFilter(val label: String) {
    ALL("Todos"),
    AVAILABLE("Disponíveis"),
    UNAVAILABLE("Indisponíveis")
}

data class MedicationsUiState(
    val medications: List<MedicationItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MedicationFilter = MedicationFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredMedications: List<MedicationItem>
        get() = medications.filter { medication ->
            val matchesSearch = searchQuery.isBlank() ||
                medication.title.contains(searchQuery, ignoreCase = true) ||
                medication.dosage.contains(searchQuery, ignoreCase = true) ||
                medication.category.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                MedicationFilter.ALL -> true
                MedicationFilter.AVAILABLE -> medication.isAvailable
                MedicationFilter.UNAVAILABLE -> !medication.isAvailable
            }

            matchesSearch && matchesFilter
        }
}
