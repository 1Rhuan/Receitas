package cc.rhuan.receitas.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class MedicationItem(
    val id: String,
    val title: String,
    val dosage: String,
    val quantity: String,
    val frequency: String = "",
    val category: String = "Uso Geral",
    val isAvailable: Boolean = true,
    val instructions: String? = null,
    val activeIngredient: String? = null,
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
    val stockQuantity: Int = 0,
    val minStockThreshold: Int = 5,
    val lastRefillDate: LocalDate? = null,
    val refillCycleDays: Int = 30,
) {
    val nextAvailableRefillDate: LocalDate?
        get() = lastRefillDate?.plusDays(refillCycleDays.toLong())

    val daysUntilAvailable: Int
        get() {
            val targetDate = nextAvailableRefillDate ?: return 0
            val days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate).toInt()
            return if (days > 0) days else 0
        }

    val isAvailableForRefill: Boolean
        get() {
            val targetDate = nextAvailableRefillDate ?: return true
            return !LocalDate.now().isBefore(targetDate)
        }

    val isStockLow: Boolean
        get() = stockQuantity in 1..minStockThreshold

    val needsRefill: Boolean
        get() = stockQuantity <= minStockThreshold || daysUntilAvailable <= 3
}
