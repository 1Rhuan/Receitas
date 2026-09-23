package cc.rhuan.receitas.domain.model

import java.time.LocalDate

sealed class RefillStatus {
    object OK : RefillStatus()
    data class RefillAvailable(val nextDate: LocalDate?) : RefillStatus()
    data class LowStock(val remainingStock: Int) : RefillStatus()
    data class PrescriptionExpired(val prescriptionId: String, val prescriptionTitle: String) : RefillStatus()
    data class PrescriptionExpiresBeforeRefill(
        val prescriptionId: String,
        val prescriptionTitle: String,
        val expirationDate: LocalDate,
        val nextRefillDate: LocalDate,
    ) : RefillStatus()
    object NoPrescriptionLinked : RefillStatus()
}
