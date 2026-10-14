package cc.rhuan.receitas.ui.feature.home

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

data class HomeStatusLabels(
    val medicationRefillToday: String = "Retirada hoje",
    val medicationRefillDelayedDay: String = "Retirada pendente há %d dia",
    val medicationRefillDelayedDays: String = "Retirada pendente há %d dias",
    val prescriptionExpiringToday: String = "Vence hoje",
    val prescriptionExpired: String = "Vencida",
    val prescriptionExpiredDay: String = "Venceu há %d dia",
    val prescriptionExpiredDays: String = "Venceu há %d dias",
    val actionRenewPrescription: String = "Registrar nova receita",
    val actionRegisterRenewal: String = "Registrar nova receita",
    val actionPickUpMedication: String = "Registrar retirada",
    val actionRegisterPickup: String = "Registrar retirada",
)

data class HomeUiState(
    val searchQuery: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val prescriptions: List<PrescriptionItem> = emptyList(),
    val medicamentos: List<MedicationItem> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val statusLabels: HomeStatusLabels = HomeStatusLabels(),
    val unreadNotificationsCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) {

    val isAppEmpty: Boolean
        get() = prescriptions.isEmpty() && medicamentos.isEmpty()

    val isTodaySelected: Boolean
        get() = selectedDate == LocalDate.now()

    val mainTrackingItems: List<TrackingItem>
        get() {
            val today = LocalDate.now()
            val fullDateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
            val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val items = mutableListOf<Pair<Int, TrackingItem>>()

            if (isTodaySelected) {
                // 1. QUANDO HOJE ESTIVER SELECIONADO:
                for (prescription in prescriptions.filter { it.isActive }) {
                    val expirationDate = prescription.expirationDate
                    val daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate).toInt()

                    if (daysUntilExpiration <= 0) {
                        val titleText = if (prescription.title.contains("Receita", ignoreCase = true)) {
                            prescription.title
                        } else {
                            "Receita de ${prescription.title}"
                        }

                        val (status, statusText, priority) = if (daysUntilExpiration < 0) {
                            val absDays = abs(daysUntilExpiration)
                            val statusStr = if (absDays > 0) String.format(if (absDays == 1) statusLabels.prescriptionExpiredDay else statusLabels.prescriptionExpiredDays, absDays) else statusLabels.prescriptionExpired
                            Triple(TrackingStatus.EXPIRED, statusStr, 1)
                        } else {
                            Triple(TrackingStatus.EXPIRES_TODAY, statusLabels.prescriptionExpiringToday, 3)
                        }

                        val dateFormattedText = if (daysUntilExpiration == 0) {
                            "Vence hoje"
                        } else {
                            "Venceu em ${expirationDate.format(shortDateFormatter)}"
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
                                secondaryActionText = statusLabels.actionRegisterRenewal,
                                rawDate = expirationDate,
                                isFutureContext = false
                            )
                        )
                    }
                }

                for (med in medicamentos.filter { it.hasRecurringPickup && !it.trackingArchived }) {
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
                            "Última retirada: ${med.lastRefillDate.format(shortDateFormatter)}"
                        } else {
                            "Última retirada: não registrada"
                        }

                        val stockVal = med.quantity.takeIf { it.isNotBlank() }?.let { "Quantidade por retirada: $it" }

                        val dateFormattedText = if (daysUntilRefill == 0) {
                            "Retirada prevista para hoje"
                        } else {
                            "Retirada pendente desde ${nextRefill.format(shortDateFormatter)}"
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
                                secondaryActionText = statusLabels.actionRegisterPickup,
                                rawDate = nextRefill,
                                isFutureContext = false
                            )
                        )
                    }
                }
            } else {
                // 2. QUANDO OUTRA DATA ESTIVER SELECIONADA:
                for (prescription in prescriptions) {
                    if (prescription.expirationDate == selectedDate) {
                        val titleText = if (prescription.title.contains("Receita", ignoreCase = true)) {
                            prescription.title
                        } else {
                            "Receita de ${prescription.title}"
                        }

                        val isExpiredInPast = selectedDate.isBefore(today)
                        val (status, statusText) = when {
                            isExpiredInPast -> TrackingStatus.EXPIRED to statusLabels.prescriptionExpired
                            selectedDate == today -> TrackingStatus.EXPIRES_TODAY to statusLabels.prescriptionExpiringToday
                            else -> TrackingStatus.EXPIRES_SOON to "Vence nesta data"
                        }

                        items.add(
                            1 to TrackingItem.Prescription(
                                id = "p_${prescription.id}",
                                targetId = prescription.id,
                                title = titleText,
                                status = status,
                                statusLabel = statusText,
                                formattedDateText = "Vencimento: ${selectedDate.format(fullDateFormatter)}",
                                prescriberName = if (prescription.doctorName.isNotBlank()) prescription.doctorName else null,
                                userNote = prescription.notes?.takeIf { it.isNotBlank() },
                                primaryActionText = statusLabels.actionRenewPrescription,
                                secondaryActionText = statusLabels.actionRegisterRenewal,
                                rawDate = prescription.expirationDate,
                                isFutureContext = false
                            )
                        )
                    }
                }

                for (med in medicamentos) {
                    val nextRefill = med.nextAvailableRefillDate ?: today
                    if (nextRefill == selectedDate) {
                        val titleText = if (med.dosage.isNotBlank()) "${med.title} ${med.dosage}" else med.title
                        val lastRefillText = if (med.lastRefillDate != null) {
                            "Última retirada: ${med.lastRefillDate.format(shortDateFormatter)}"
                        } else {
                            "Última retirada: não registrada"
                        }
                        val stockVal = med.quantity.takeIf { it.isNotBlank() }?.let { "Quantidade por retirada: $it" }

                        val isDelayedInPast = selectedDate.isBefore(today)
                        val (status, statusText) = when {
                            isDelayedInPast -> TrackingStatus.PICKUP_OVERDUE to "Pendente desde ${selectedDate.format(shortDateFormatter)}"
                            selectedDate == today -> TrackingStatus.PICKUP_TODAY to statusLabels.medicationRefillToday
                            else -> TrackingStatus.PICKUP_SOON to "Retirada prevista"
                        }

                        items.add(
                            2 to TrackingItem.Medication(
                                id = "m_${med.id}",
                                targetId = med.id,
                                title = titleText,
                                status = status,
                                statusLabel = statusText,
                                formattedDateText = "Retirada prevista para ${selectedDate.format(shortDateFormatter)}",
                                lastPickupDateText = lastRefillText,
                                stockText = stockVal,
                                userNote = med.instructions?.takeIf { it.isNotBlank() },
                                primaryActionText = statusLabels.actionPickUpMedication,
                                secondaryActionText = statusLabels.actionRegisterPickup,
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

            for (prescription in prescriptions) {
                val expirationDate = prescription.expirationDate
                if (expirationDate.isAfter(selectedDate)) {
                    val daysAway = ChronoUnit.DAYS.between(selectedDate, expirationDate).toInt()
                    val titleText = if (prescription.title.contains("Receita", ignoreCase = true)) {
                        prescription.title
                    } else {
                        "Receita de ${prescription.title}"
                    }

                    val contextTag = if (daysAway == 1) "Vence em 1 dia" else "Vence em $daysAway dias"

                    items.add(
                        expirationDate to TrackingItem.Prescription(
                            id = "future_p_${prescription.id}",
                            targetId = prescription.id,
                            title = titleText,
                            status = TrackingStatus.EXPIRES_SOON,
                            statusLabel = "Em breve",
                            formattedDateText = "${expirationDate.format(shortDateFormatter)} · Vencimento da receita",
                            prescriberName = if (prescription.doctorName.isNotBlank()) prescription.doctorName else null,
                            userNote = prescription.notes?.takeIf { it.isNotBlank() },
                            primaryActionText = statusLabels.actionRenewPrescription,
                            secondaryActionText = statusLabels.actionRegisterRenewal,
                            rawDate = expirationDate,
                            isFutureContext = true,
                            contextTag = contextTag
                        )
                    )
                }
            }

            for (med in medicamentos) {
                val nextRefill = med.nextAvailableRefillDate
                if (nextRefill != null && nextRefill.isAfter(selectedDate)) {
                    val daysAway = ChronoUnit.DAYS.between(selectedDate, nextRefill).toInt()
                    val titleText = if (med.dosage.isNotBlank()) "${med.title} ${med.dosage}" else med.title

                    val contextTag = if (daysAway == 1) "Retirada em 1 dia" else "Retirada em $daysAway dias"
                    val stockVal = med.quantity.takeIf { it.isNotBlank() }?.let { "Quantidade por retirada: $it" }

                    items.add(
                        nextRefill to TrackingItem.Medication(
                            id = "future_m_${med.id}",
                            targetId = med.id,
                            title = titleText,
                            status = TrackingStatus.PICKUP_SOON,
                            statusLabel = "Em breve",
                            formattedDateText = "${nextRefill.format(shortDateFormatter)} · Retirada de medicamento",
                            stockText = stockVal,
                            userNote = med.instructions?.takeIf { it.isNotBlank() },
                            primaryActionText = statusLabels.actionPickUpMedication,
                            secondaryActionText = statusLabels.actionRegisterPickup,
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

        for (prescription in prescriptions) {
            val expDate = prescription.expirationDate
            if (expDate == date) {
                if (expDate < today) {
                    hasRed = true
                } else {
                    hasOrange = true
                }
            }
        }

        for (med in medicamentos) {
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
