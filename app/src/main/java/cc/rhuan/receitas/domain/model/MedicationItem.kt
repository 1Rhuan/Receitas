package cc.rhuan.receitas.domain.model

import java.time.LocalDate

data class MedicationItem(
    val id: String,
    val title: String,
    val activeIngredient: String? = null,
    val dosage: String = "",
    val quantity: String = "",
    val prescribedPosology: String = "",
    val prescribedQuantity: String = "",
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
    val source: MedicationSource = MedicationSource.SELF_PURCHASE,
    val sourceName: String? = null,
    val instructions: String? = null,
    val lastRefillDate: LocalDate? = null,
    val refillCycleDays: Int = 30,
    val hasRecurringPickup: Boolean = lastRefillDate != null,
    val pickupReminderDaysBefore: Int = 3,
    val intervalUnit: IntervalUnit = IntervalUnit.DAYS,
    val linkedPrescriptionId: String? = null,
    val trackingArchived: Boolean = false,
    val pickupHistory: List<PickupEvent> = emptyList(),
    val initialPickupDate: LocalDate? = null,
    val trackingError: String? = null,
) {
    val nextAvailableRefillDate: LocalDate?
        get() = if (hasRecurringPickup && !trackingArchived && refillCycleDays in 1..3650) lastRefillDate?.let { PickupInterval(refillCycleDays, intervalUnit).nextAfter(it) } else null

    val needsRefill: Boolean
        get() = nextAvailableRefillDate?.let { !LocalDate.now().isBefore(it.minusDays(pickupReminderDaysBefore.toLong())) } == true
}
