package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.domain.model.PrescriptionItem

enum class PrescriptionFilter(val label: String) {
    ALL("Todas"),
    VALID("Válidas"),
    EXPIRED("Vencidas"),
}

enum class PrescriptionSortOrder(val label: String) {
    EXPIRATION("Validade"),
    NAME_ASC("Nome (A-Z)"),
    NAME_DESC("Nome (Z-A)"),
    SPECIALTY("Especialidade"),
    DOCTOR("Médico"),
}

data class PrescriptionsUiState(
    val prescriptions: List<PrescriptionItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: PrescriptionFilter = PrescriptionFilter.ALL,
    val selectedSortOrder: PrescriptionSortOrder = PrescriptionSortOrder.EXPIRATION,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val filteredPrescriptions: List<PrescriptionItem>
        get() {
            val filtered = prescriptions.filter { prescription ->
                val matchesSearch = searchQuery.isBlank() ||
                    prescription.title.contains(searchQuery, ignoreCase = true) ||
                    prescription.specialty.contains(searchQuery, ignoreCase = true) ||
                    prescription.doctorName.contains(searchQuery, ignoreCase = true)

                val matchesFilter = when (selectedFilter) {
                    PrescriptionFilter.ALL -> true
                    PrescriptionFilter.VALID -> !prescription.isExpired
                    PrescriptionFilter.EXPIRED -> prescription.isExpired
                }

                matchesSearch && matchesFilter
            }

            return when (selectedSortOrder) {
                PrescriptionSortOrder.EXPIRATION -> filtered.sortedBy { it.daysUntilExpiration }
                PrescriptionSortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
                PrescriptionSortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
                PrescriptionSortOrder.SPECIALTY -> filtered.sortedWith(
                    compareBy<PrescriptionItem> { it.specialty.lowercase() }
                        .thenBy { it.title.lowercase() },
                )
                PrescriptionSortOrder.DOCTOR -> filtered.sortedWith(
                    compareBy<PrescriptionItem> { it.doctorName.lowercase() }
                        .thenBy { it.title.lowercase() },
                )
            }
        }
}
