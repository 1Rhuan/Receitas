package cc.rhuan.receitas.ui.feature.home

import cc.rhuan.receitas.domain.model.DailyDoseItem
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem

data class HomeUiState(
    val userName: String = "Rhuan",
    val searchQuery: String = "",
    val prescriptions: List<PrescriptionItem> = emptyList(),
    val medicamentos: List<MedicationItem> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val dailyDoses: List<DailyDoseItem> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dismissedAlerts: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) {

    val totalDosesCount: Int
        get() = dailyDoses.size

    val takenDosesCount: Int
        get() = dailyDoses.count { it.isTaken }

    val adherencePercentage: Float
        get() = if (totalDosesCount > 0) takenDosesCount.toFloat() / totalDosesCount else 0f

    val urgentPrescriptions: List<PrescriptionItem>
        get() = filteredPrescriptions.sortedBy { it.daysUntilExpiration }

    val urgentMedications: List<MedicationItem>
        get() = filteredMedications.sortedWith(
            compareByDescending<MedicationItem> { it.isAvailable }
                .thenBy { it.daysUntilAvailable }
                .thenBy { it.title },
        )

    val activeNotifications: List<NotificationItem>
        get() = notifications.filter { !it.isRead && (it.id !in dismissedAlerts) }

    val filteredPrescriptions: List<PrescriptionItem>
        get() = if (searchQuery.isBlank()) {
            prescriptions
        } else {
            prescriptions.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.specialty.contains(searchQuery, ignoreCase = true) ||
                    it.doctorName.contains(searchQuery, ignoreCase = true)
            }
        }

    val filteredMedications: List<MedicationItem>
        get() = if (searchQuery.isBlank()) {
            medicamentos
        } else {
            medicamentos.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.dosage.contains(searchQuery, ignoreCase = true)
            }
        }
}
