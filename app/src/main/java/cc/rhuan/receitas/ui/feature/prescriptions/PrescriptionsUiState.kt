package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.annotation.StringRes
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.PrescriptionItem

enum class PrescriptionFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    VALID(R.string.filter_valid),
    EXPIRED(R.string.filter_expired),
    HISTORY(R.string.filter_history),
}

enum class PrescriptionSortOrder(@StringRes val labelRes: Int) {
    EXPIRATION(R.string.sort_expiration),
    NAME_ASC(R.string.sort_name_asc),
    NAME_DESC(R.string.sort_name_desc),
    SPECIALTY(R.string.sort_specialty),
    DOCTOR(R.string.sort_doctor),
}

data class PrescriptionsUiState(
    val medications: List<cc.rhuan.receitas.domain.model.MedicationItem> = emptyList(),
    val purchaseError: Boolean = false,
    val prescriptions: List<PrescriptionItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: PrescriptionFilter = PrescriptionFilter.ALL,
    val selectedSortOrder: PrescriptionSortOrder = PrescriptionSortOrder.EXPIRATION,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val nextPurchase: cc.rhuan.receitas.domain.model.MedicationItem?
        get() = medications.filter { it.source == cc.rhuan.receitas.domain.model.MedicationSource.COMPRA_PROPRIA && it.nextAvailableRefillDate != null }
            .minByOrNull { it.nextAvailableRefillDate!! }
    val filteredPrescriptions: List<PrescriptionItem>
        get() {
            val filtered = prescriptions.filter { prescription ->
                val matchesSearch = searchQuery.isBlank() ||
                    prescription.title.contains(searchQuery, ignoreCase = true) ||
                    prescription.specialty.contains(searchQuery, ignoreCase = true) ||
                    prescription.doctorName.contains(searchQuery, ignoreCase = true)

                val matchesFilter = when (selectedFilter) {
                    PrescriptionFilter.ALL -> true
                    PrescriptionFilter.VALID -> prescription.isActive && !prescription.isExpired
                    PrescriptionFilter.EXPIRED -> prescription.isActive && prescription.isExpired
                    PrescriptionFilter.HISTORY -> !prescription.isActive
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
