package cc.rhuan.receitas.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PrescriptionItem(
    val id: String,
    val title: String,
    val specialty: String,
    val doctorId: String? = null,
    val doctorName: String = "",
    val doctorRegistration: String? = null,
    val issueDate: LocalDate = LocalDate.now(),
    val type: PrescriptionType? = null,
    val customValidityDays: Int? = null,
    val notes: String? = null,
    val medicationIds: List<String> = emptyList(),
) {
    val validityDurationDays: Int
        get() = customValidityDays ?: type?.defaultValidityDays ?: 180

    val expirationDate: LocalDate
        get() = issueDate.plusDays(validityDurationDays.toLong())

    val daysUntilExpiration: Int
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate).toInt()

    val isExpired: Boolean
        get() = LocalDate.now().isAfter(expirationDate)

    fun isValidOn(date: LocalDate): Boolean {
        return !date.isAfter(expirationDate)
    }
}
