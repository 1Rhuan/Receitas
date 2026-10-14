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
    val renewalReminderEnabled: Boolean = true,
    val renewalReminderDaysBefore: Int = 5,
    val state: RecordState = RecordState.ACTIVE,
    val previousPrescriptionId: String? = null,
    val items: List<PrescriptionMedication> = emptyList(),
    val expiresOn: LocalDate? = null,
    val validityOrigin: String = "USER_ENTERED",
) {
    val validityDurationDays: Int
        get() = expiresOn?.let { ChronoUnit.DAYS.between(issueDate, it).toInt() } ?: customValidityDays ?: type?.defaultValidityDays ?: 180

    val expirationDate: LocalDate
        get() = expiresOn ?: issueDate.plusDays(validityDurationDays.toLong())

    val daysUntilExpiration: Int
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate).toInt()

    val isActive: Boolean get() = state == RecordState.ACTIVE

    val isExpired: Boolean
        get() = LocalDate.now().isAfter(expirationDate)

    fun isValidOn(date: LocalDate): Boolean {
        return !date.isBefore(issueDate) && !date.isAfter(expirationDate)
    }
}
