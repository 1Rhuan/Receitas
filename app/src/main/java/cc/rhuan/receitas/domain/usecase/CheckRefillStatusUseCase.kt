package cc.rhuan.receitas.domain.usecase

import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.RefillStatus
import java.time.LocalDate
import javax.inject.Inject

class CheckRefillStatusUseCase @Inject constructor() {

    operator fun invoke(
        medication: MedicationItem,
        prescriptions: List<PrescriptionItem>,
    ): RefillStatus {
        val linkedPrescription = prescriptions.find { prescription ->
            prescription.medicationIds.contains(medication.id)
        } ?: return RefillStatus.NoPrescriptionLinked

        if (linkedPrescription.isExpired) {
            return RefillStatus.PrescriptionExpired(
                prescriptionId = linkedPrescription.id,
                prescriptionTitle = linkedPrescription.title,
            )
        }

        val nextRefillDate = medication.nextAvailableRefillDate ?: LocalDate.now()

        if (!linkedPrescription.isValidOn(nextRefillDate)) {
            return RefillStatus.PrescriptionExpiresBeforeRefill(
                prescriptionId = linkedPrescription.id,
                prescriptionTitle = linkedPrescription.title,
                expirationDate = linkedPrescription.expirationDate,
                nextRefillDate = nextRefillDate,
            )
        }

        if (medication.isAvailableForRefill && medication.stockQuantity <= medication.minStockThreshold) {
            return RefillStatus.RefillAvailable(medication.nextAvailableRefillDate)
        }

        if (medication.isStockLow) {
            return RefillStatus.LowStock(medication.stockQuantity)
        }

        return RefillStatus.OK
    }
}
