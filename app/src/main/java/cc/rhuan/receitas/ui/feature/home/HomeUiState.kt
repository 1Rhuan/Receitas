package cc.rhuan.receitas.ui.feature.home

import cc.rhuan.receitas.R
import cc.rhuan.receitas.text.TextResolver

import androidx.compose.ui.graphics.Color
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.ui.feature.home.components.TrackingItem
import cc.rhuan.receitas.ui.feature.home.components.TrackingStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

enum class DayEventPriority(val color: Color) {
    RED(Color(0xFFB83249)),
    ORANGE(Color(0xFFF97316)),
    GREEN(Color(0xFF10B981))
}

data class HomeStatusLabels(private val texts: TextResolver,
    val medicationRefillToday: String = texts.get(R.string.home_pickup_today),
    val medicationRefillDelayedDay: String = texts.get(R.string.home_pickup_overdue_day),
    val medicationRefillDelayedDays: String = texts.get(R.string.home_pickup_overdue_days),
    val prescriptionExpiringToday: String = texts.get(R.string.home_expires_today),
    val prescriptionExpired: String = texts.get(R.string.home_expired),
    val prescriptionExpiredDay: String = texts.get(R.string.home_expired_day),
    val prescriptionExpiredDays: String = texts.get(R.string.home_expired_days),
    val actionRenewPrescription: String = texts.get(R.string.home_renew_action),
    val actionPickUpMedication: String = texts.get(R.string.home_pickup_action),
)

data class HomeUiState(
    val texts: TextResolver,
    val searchQuery: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val prescriptions: List<PrescriptionItem> = emptyList(),
    val medications: List<MedicationItem> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val statusLabels: HomeStatusLabels = HomeStatusLabels(texts),
    val unreadNotificationsCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) {

    val isAppEmpty: Boolean
        get() = prescriptions.isEmpty() && medications.isEmpty()

    val isTodaySelected: Boolean
        get() = selectedDate == LocalDate.now()

    val mainTrackingItems: List<TrackingItem>
        get() {
            val today = LocalDate.now()
            val fullDateFormatter = DateTimeFormatter.ofPattern(texts.get(R.string.full_date_format), Locale.forLanguageTag("pt-BR"))
            val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val items = mutableListOf<Pair<Int, TrackingItem>>()

            if (isTodaySelected) {
                // Events due today or overdue.
                for (prescription in prescriptions.filter { it.isActive }) {
                    val expirationDate = prescription.expirationDate
                    val daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate).toInt()

                    if (daysUntilExpiration <= 0) {
                        val titleText = if (prescription.title.contains(texts.get(R.string.prescription_default_title), ignoreCase = true)) {
                            prescription.title
                        } else {
                            texts.get(R.string.home_prescription_title, prescription.title)
                        }

                        val (status, statusText, priority) = if (daysUntilExpiration < 0) {
                            val absDays = abs(daysUntilExpiration)
                            val statusStr = if (absDays > 0) String.format(if (absDays == 1) statusLabels.prescriptionExpiredDay else statusLabels.prescriptionExpiredDays, absDays) else statusLabels.prescriptionExpired
                            Triple(TrackingStatus.EXPIRED, statusStr, 1)
                        } else {
                            Triple(TrackingStatus.EXPIRES_TODAY, statusLabels.prescriptionExpiringToday, 3)
                        }

                        val dateFormattedText = if (daysUntilExpiration == 0) {
                            texts.get(R.string.home_expires_today)
                        } else {
                            texts.get(R.string.home_expired_on, expirationDate.format(shortDateFormatter))
                        }

                        items.add(
                            priority to TrackingItem.Prescription(
                                id = "p_${prescription.id}",
                                targetId = prescription.id,
                                title = titleText,
                                status = status,
                                statusLabel = statusText,
                                formattedDateText = dateFormattedText,
                                prescriberName = if (prescription.doctorName.isNotBlank()) prescription.doctorName else null,
                                userNote = prescription.notes?.takeIf { it.isNotBlank() },
                                primaryActionText = statusLabels.actionRenewPrescription,
                                rawDate = expirationDate,
                                isFutureContext = false
                            )
                        )
                    }
                }

                for (med in medications.filter { it.hasRecurringPickup && !it.trackingArchived }) {
                    val nextRefill = med.nextAvailableRefillDate ?: continue
                    val daysUntilRefill = ChronoUnit.DAYS.between(today, nextRefill).toInt()

                    if (daysUntilRefill <= 0) {
                        val titleText = if (med.dosage.isNotBlank()) "${med.title} ${med.dosage}" else med.title

                        val (status, statusText, priority) = if (daysUntilRefill < 0) {
                            val absDays = abs(daysUntilRefill)
                            val statusStr = String.format(if (absDays == 1) statusLabels.medicationRefillDelayedDay else statusLabels.medicationRefillDelayedDays, absDays)
                            Triple(TrackingStatus.PICKUP_OVERDUE, statusStr, 2)
                        } else {
                            Triple(TrackingStatus.PICKUP_TODAY, statusLabels.medicationRefillToday, 4)
                        }

                        val lastRefillText = if (med.lastRefillDate != null) {
                            texts.get(R.string.home_last_pickup, med.lastRefillDate.format(shortDateFormatter))
                        } else {
                            texts.get(R.string.home_last_pickup_missing)
                        }

                        val stockVal = med.quantity.takeIf { it.isNotBlank() }?.let { texts.get(R.string.home_pickup_quantity, it) }

                        val dateFormattedText = if (daysUntilRefill == 0) {
                            texts.get(R.string.reminder_pickup_today)
                        } else {
                            texts.get(R.string.home_pickup_pending_since, nextRefill.format(shortDateFormatter))
                        }

                        items.add(
                            priority to TrackingItem.Medication(
                                id = "m_${med.id}",
                                targetId = med.id,
                                title = titleText,
                                status = status,
                                statusLabel = statusText,
                                formattedDateText = dateFormattedText,
                                lastPickupDateText = lastRefillText,
                                stockText = stockVal,
                                userNote = med.instructions?.takeIf { it.isNotBlank() },
                                primaryActionText = statusLabels.actionPickUpMedication,
                                rawDate = nextRefill,
                                isFutureContext = false
                            )
                        )
                    }
                }
            } else {
                // Events for the selected date.
                for (prescription in prescriptions.filter { it.isActive }) {
                    if (prescription.expirationDate == selectedDate) {
                        val titleText = if (prescription.title.contains(texts.get(R.string.prescription_default_title), ignoreCase = true)) {
                            prescription.title
                        } else {
                            texts.get(R.string.home_prescription_title, prescription.title)
                        }

                        val isExpiredInPast = selectedDate.isBefore(today)
                        val (status, statusText) = when {
                            isExpiredInPast -> TrackingStatus.EXPIRED to statusLabels.prescriptionExpired
                            selectedDate == today -> TrackingStatus.EXPIRES_TODAY to statusLabels.prescriptionExpiringToday
                            else -> TrackingStatus.EXPIRES_SOON to texts.get(R.string.home_expiration_on_selected_date)
                        }

                        items.add(
                            1 to TrackingItem.Prescription(
                                id = "p_${prescription.id}",
                                targetId = prescription.id,
                                title = titleText,
                                status = status,
                                statusLabel = statusText,
                                formattedDateText = texts.get(R.string.home_expiration_date, selectedDate.format(fullDateFormatter)),
                                prescriberName = if (prescription.doctorName.isNotBlank()) prescription.doctorName else null,
                                userNote = prescription.notes?.takeIf { it.isNotBlank() },
                                primaryActionText = statusLabels.actionRenewPrescription,
                                rawDate = prescription.expirationDate,
                                isFutureContext = false
                            )
                        )
                    }
                }

                for (med in medications.filter { it.hasRecurringPickup && !it.trackingArchived }) {
                    val nextRefill = med.nextAvailableRefillDate ?: continue
                    if (nextRefill == selectedDate) {
                        val titleText = if (med.dosage.isNotBlank()) "${med.title} ${med.dosage}" else med.title
                        val lastRefillText = if (med.lastRefillDate != null) {
                            texts.get(R.string.home_last_pickup, med.lastRefillDate.format(shortDateFormatter))
                        } else {
                            texts.get(R.string.home_last_pickup_missing)
                        }
                        val stockVal = med.quantity.takeIf { it.isNotBlank() }?.let { texts.get(R.string.home_pickup_quantity, it) }

                        val isDelayedInPast = selectedDate.isBefore(today)
                        val (status, statusText) = when {
                            isDelayedInPast -> TrackingStatus.PICKUP_OVERDUE to texts.get(R.string.home_pending_since, selectedDate.format(shortDateFormatter))
                            selectedDate == today -> TrackingStatus.PICKUP_TODAY to statusLabels.medicationRefillToday
                            else -> TrackingStatus.PICKUP_SOON to texts.get(R.string.home_expected_pickup)
                        }

                        items.add(
                            2 to TrackingItem.Medication(
                                id = "m_${med.id}",
                                targetId = med.id,
                                title = titleText,
                                status = status,
                                statusLabel = statusText,
                                formattedDateText = texts.get(R.string.home_expected_pickup_date, selectedDate.format(shortDateFormatter)),
                                lastPickupDateText = lastRefillText,
                                stockText = stockVal,
                                userNote = med.instructions?.takeIf { it.isNotBlank() },
                                primaryActionText = statusLabels.actionPickUpMedication,
                                rawDate = nextRefill,
                                isFutureContext = false
                            )
                        )
                    }
                }
            }

            return items.sortedWith(
                compareBy<Pair<Int, TrackingItem>> { it.first }
                    .thenBy { it.second.title }
            ).map { it.second }
        }

    val futureTrackingItems: List<TrackingItem>
        get() {
            val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM")
            val items = mutableListOf<Pair<LocalDate, TrackingItem>>()

            for (prescription in prescriptions.filter { it.isActive }) {
                val expirationDate = prescription.expirationDate
                if (expirationDate.isAfter(selectedDate)) {
                    val daysAway = ChronoUnit.DAYS.between(selectedDate, expirationDate).toInt()
                    val titleText = if (prescription.title.contains(texts.get(R.string.prescription_default_title), ignoreCase = true)) {
                        prescription.title
                    } else {
                        texts.get(R.string.home_prescription_title, prescription.title)
                    }

                    val contextTag = if (daysAway == 1) texts.get(R.string.home_expires_in_one_day) else texts.get(R.string.home_expires_in_days, daysAway)

                    items.add(
                        expirationDate to TrackingItem.Prescription(
                            id = "future_p_${prescription.id}",
                            targetId = prescription.id,
                            title = titleText,
                            status = TrackingStatus.EXPIRES_SOON,
                            statusLabel = texts.get(R.string.home_soon),
                            formattedDateText = texts.get(R.string.home_expiration_event, expirationDate.format(shortDateFormatter)),
                            prescriberName = if (prescription.doctorName.isNotBlank()) prescription.doctorName else null,
                            userNote = prescription.notes?.takeIf { it.isNotBlank() },
                            primaryActionText = statusLabels.actionRenewPrescription,
                            rawDate = expirationDate,
                            isFutureContext = true,
                            contextTag = contextTag
                        )
                    )
                }
            }

            for (med in medications) {
                val nextRefill = med.nextAvailableRefillDate
                if (nextRefill != null && nextRefill.isAfter(selectedDate)) {
                    val daysAway = ChronoUnit.DAYS.between(selectedDate, nextRefill).toInt()
                    val titleText = if (med.dosage.isNotBlank()) "${med.title} ${med.dosage}" else med.title

                    val contextTag = if (daysAway == 1) texts.get(R.string.home_pickup_in_one_day) else texts.get(R.string.home_pickup_in_days, daysAway)
                    val stockVal = med.quantity.takeIf { it.isNotBlank() }?.let { texts.get(R.string.home_pickup_quantity, it) }

                    items.add(
                        nextRefill to TrackingItem.Medication(
                            id = "future_m_${med.id}",
                            targetId = med.id,
                            title = titleText,
                            status = TrackingStatus.PICKUP_SOON,
                            statusLabel = texts.get(R.string.home_soon),
                            formattedDateText = texts.get(R.string.home_pickup_event, nextRefill.format(shortDateFormatter)),
                            stockText = stockVal,
                            userNote = med.instructions?.takeIf { it.isNotBlank() },
                            primaryActionText = statusLabels.actionPickUpMedication,
                            rawDate = nextRefill,
                            isFutureContext = true,
                            contextTag = contextTag
                        )
                    )
                }
            }

            return items.sortedBy { it.first }.take(3).map { it.second }
        }


    fun getEventIndicatorForDate(date: LocalDate, today: LocalDate = LocalDate.now()): DayEventPriority? {
        var hasRed = false
        var hasOrange = false
        var hasGreen = false

        for (prescription in prescriptions.filter { it.isActive }) {
            val expDate = prescription.expirationDate
            if (expDate == date) {
                if (expDate < today) {
                    hasRed = true
                } else {
                    hasOrange = true
                }
            }
        }

        for (med in medications) {
            val nextRefill = med.nextAvailableRefillDate
            if (nextRefill == date) {
                if (date < today) {
                    hasRed = true
                } else {
                    hasGreen = true
                }
            }
        }

        return when {
            hasRed -> DayEventPriority.RED
            hasOrange -> DayEventPriority.ORANGE
            hasGreen -> DayEventPriority.GREEN
            else -> null
        }
    }
}
