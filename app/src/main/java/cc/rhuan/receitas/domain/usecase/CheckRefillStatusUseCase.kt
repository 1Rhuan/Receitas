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
        today: LocalDate = LocalDate.now(),
    ): RefillStatus {
        if (!medication.hasRecurringPickup || medication.trackingArchived) return RefillStatus.OK
        val linkedPrescription = prescriptions.find { prescription ->
            prescription.id == medication.linkedPrescriptionId
        } ?: return RefillStatus.NoPrescriptionLinked

        if (!linkedPrescription.isActive || !linkedPrescription.isValidOn(today)) {
            return RefillStatus.PrescriptionExpired(
                prescriptionId = linkedPrescription.id,
                prescriptionTitle = linkedPrescription.title,
            )
        }

        if (!medication.hasRecurringPickup) return RefillStatus.OK

        val nextRefillDate = maxOf(medication.nextAvailableRefillDate ?: today, today)

        if (!linkedPrescription.isValidOn(nextRefillDate)) {
            return RefillStatus.PrescriptionExpiresBeforeRefill(
                prescriptionId = linkedPrescription.id,
                prescriptionTitle = linkedPrescription.title,
                expirationDate = linkedPrescription.expirationDate,
                nextRefillDate = nextRefillDate,
            )
        }

        if (medication.nextAvailableRefillDate?.let { !today.isBefore(it) } == true) {
            return RefillStatus.RefillAvailable(medication.nextAvailableRefillDate)
        }

        return RefillStatus.OK
    }
}
