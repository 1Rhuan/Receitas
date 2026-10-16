package cc.rhuan.receitas.domain.model

import cc.rhuan.receitas.text.TextMessage

import cc.rhuan.receitas.R

import java.time.LocalDate
import java.io.Serializable

enum class IntervalUnit { DAYS, WEEKS, MONTHS, YEARS }
data class PickupInterval(val amount: Int, val unit: IntervalUnit = IntervalUnit.DAYS) : Serializable {
    init { require(amount in 1..3650) { "Pickup interval must be between 1 and 3650" } }
    fun nextAfter(date: LocalDate): LocalDate = when (unit) {
        IntervalUnit.DAYS -> date.plusDays(amount.toLong())
        IntervalUnit.WEEKS -> date.plusWeeks(amount.toLong())
        IntervalUnit.MONTHS -> date.plusMonths(amount.toLong())
        IntervalUnit.YEARS -> date.plusYears(amount.toLong())
    }
}
enum class RecordState { ACTIVE, ARCHIVED, REPLACED }
data class PrescriptionMedication(val medicationId: String, val posology: String = "", val quantity: String = "", val title: String = "", val dosage: String = "", val form: PharmaceuticalForm = PharmaceuticalForm.TABLET)
data class PickupEvent(val id: String, val occurredOn: LocalDate, val recordedAt: Long)
sealed interface OperationResult {
    data object Success : OperationResult
    data object NotFound : OperationResult
    data object InvalidDate : OperationResult
    data object AlreadyRecorded : OperationResult
    data object Inactive : OperationResult
    data object ConfirmationRequired : OperationResult
    data object InvalidPrescription : OperationResult
    data object ObsoleteCycle : OperationResult
}
fun OperationResult.message(): TextMessage = TextMessage(when (this) {
    OperationResult.Success -> R.string.pickup_recorded_feedback
    OperationResult.NotFound -> R.string.record_not_found
    OperationResult.InvalidDate -> R.string.pickup_date_not_future
    OperationResult.AlreadyRecorded -> R.string.pickup_duplicate_date
    OperationResult.Inactive -> R.string.tracking_inactive
    OperationResult.ConfirmationRequired -> R.string.early_pickup_confirmation_required
    OperationResult.InvalidPrescription -> R.string.tracking_active_prescription_required
    OperationResult.ObsoleteCycle -> R.string.tracking_changed_retry
})
