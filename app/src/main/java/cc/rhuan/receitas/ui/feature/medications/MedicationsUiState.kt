package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.domain.model.MedicationItem

enum class MedicationFilter(val label: String) {
    ALL("Todos"),
    AVAILABLE("Disponíveis"),
    UNAVAILABLE("Indisponíveis"),
}

enum class MedicationSortOrder(val label: String) {
    DEFAULT("Padrão"),
    NAME_ASC("Nome (A-Z)"),
    NAME_DESC("Nome (Z-A)"),
    CATEGORY("Categoria"),
}

data class MedicationsUiState(
    val medications: List<MedicationItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MedicationFilter = MedicationFilter.ALL,
    val selectedSortOrder: MedicationSortOrder = MedicationSortOrder.DEFAULT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val filteredMedications: List<MedicationItem>
        get() {
            val filtered = medications.filter { medication ->
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

            return when (selectedSortOrder) {
                MedicationSortOrder.DEFAULT -> filtered.sortedWith(
                    compareByDescending<MedicationItem> { it.isAvailable }
                        .thenBy { it.daysUntilAvailable }
                        .thenBy { it.title },
                )
                MedicationSortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
                MedicationSortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
                MedicationSortOrder.CATEGORY -> filtered.sortedWith(
                    compareBy<MedicationItem> { it.category.lowercase() }
                        .thenBy { it.title.lowercase() },
                )
            }
        }
}
