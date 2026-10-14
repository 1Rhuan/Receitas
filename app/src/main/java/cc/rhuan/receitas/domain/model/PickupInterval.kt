package cc.rhuan.receitas.domain.model

import java.time.LocalDate
import java.io.Serializable

enum class IntervalUnit(val label: String) { DAYS("Dias"), WEEKS("Semanas"), MONTHS("Meses"), YEARS("Anos") }
data class PickupInterval(val amount: Int, val unit: IntervalUnit = IntervalUnit.DAYS) : Serializable {
    init { require(amount in 1..3650) { "Informe um intervalo de 1 a 3650." } }
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
fun OperationResult.message(): String = when (this) {
    OperationResult.Success -> "Retirada registrada."
    OperationResult.NotFound -> "Registro não encontrado."
    OperationResult.InvalidDate -> "Selecione uma data igual ou anterior a hoje."
    OperationResult.AlreadyRecorded -> "Já existe uma retirada nesta data."
    OperationResult.Inactive -> "O acompanhamento está desativado."
    OperationResult.ConfirmationRequired -> "Confirme a retirada antecipada."
    OperationResult.InvalidPrescription -> "Escolha uma receita ativa ou remova o vínculo antes de reativar o acompanhamento."
    OperationResult.ObsoleteCycle -> "Os dados deste acompanhamento mudaram. Atualize a tela e tente novamente."
}
