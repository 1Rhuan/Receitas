package cc.rhuan.receitas.domain.usecase

import android.content.Context
import cc.rhuan.receitas.R
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.RefillStatus

class GenerateNotificationsUseCase(
    private val checkRefillStatusUseCase: CheckRefillStatusUseCase = CheckRefillStatusUseCase(),
) {

    suspend operator fun invoke(
        context: Context,
        medications: List<MedicationItem>,
        prescriptions: List<PrescriptionItem>,
        notificationRepository: NotificationRepository,
    ) {
        for (prescription in prescriptions) {
            val linkedMeds = medications.filter { prescription.medicationIds.contains(it.id) }
            if (linkedMeds.isEmpty()) continue

            // 1. Receitas que vencerão antes da data de reposição
            val expiringBeforeRefillMeds = linkedMeds.filter { med ->
                checkRefillStatusUseCase(med, prescriptions) is RefillStatus.PrescriptionExpiresBeforeRefill
            }

            if (expiringBeforeRefillMeds.isNotEmpty()) {
                val notifId = "refill_exp_${prescription.id}"
                val title = context.getString(R.string.notif_renewal_title)
                val message = if (expiringBeforeRefillMeds.size == 1) {
                    val medName = expiringBeforeRefillMeds.first().title
                    context.getString(R.string.notif_renewal_msg_single, prescription.title, medName)
                } else {
                    val medsListStr = expiringBeforeRefillMeds.joinToString(", ") { it.title }
                    context.getString(R.string.notif_renewal_msg_multiple, prescription.title, medsListStr)
                }

                val notif = NotificationItem(
                    id = notifId,
                    title = title,
                    message = message,
                    timestamp = context.getString(R.string.now),
                    type = NotificationType.PRESCRIPTION_EXPIRING,
                    isRead = false,
                    targetId = prescription.id,
                )
                notificationRepository.addNotification(notif)
            }

            // 2. Receitas vencidas
            val expiredMeds = linkedMeds.filter { med ->
                checkRefillStatusUseCase(med, prescriptions) is RefillStatus.PrescriptionExpired
            }

            if (expiredMeds.isNotEmpty()) {
                val notifId = "exp_${prescription.id}"
                val title = context.getString(R.string.notif_expired_title)
                val message = if (expiredMeds.size == 1) {
                    val medName = expiredMeds.first().title
                    context.getString(R.string.notif_expired_msg_single, prescription.title, medName)
                } else {
                    val medsListStr = expiredMeds.joinToString(", ") { it.title }
                    context.getString(R.string.notif_expired_msg_multiple, prescription.title, medsListStr)
                }

                val notif = NotificationItem(
                    id = notifId,
                    title = title,
                    message = message,
                    timestamp = context.getString(R.string.now),
                    type = NotificationType.PRESCRIPTION_EXPIRING,
                    isRead = false,
                    targetId = prescription.id,
                )
                notificationRepository.addNotification(notif)
            }
        }

        // 3. Reposição liberada na farmácia
        val refillAvailableMeds = medications.filter { med ->
            checkRefillStatusUseCase(med, prescriptions) is RefillStatus.RefillAvailable
        }

        if (refillAvailableMeds.isNotEmpty()) {
            if (refillAvailableMeds.size == 1) {
                val med = refillAvailableMeds.first()
                val notif = NotificationItem(
                    id = "avail_${med.id}",
                    title = context.getString(R.string.notif_refill_title),
                    message = context.getString(R.string.notif_refill_msg_single, med.title),
                    timestamp = context.getString(R.string.now),
                    type = NotificationType.MEDICATION_STOCK,
                    isRead = false,
                    targetId = med.id,
                )
                notificationRepository.addNotification(notif)
            } else {
                val medsListStr = refillAvailableMeds.joinToString(", ") { it.title }
                val notif = NotificationItem(
                    id = "avail_group_multiple",
                    title = context.getString(R.string.notif_refill_title),
                    message = context.getString(R.string.notif_refill_msg_multiple, medsListStr),
                    timestamp = context.getString(R.string.now),
                    type = NotificationType.MEDICATION_STOCK,
                    isRead = false,
                    targetId = refillAvailableMeds.first().id,
                )
                notificationRepository.addNotification(notif)
            }
        }
    }
}
