package cc.rhuan.receitas.domain.usecase

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import cc.rhuan.receitas.domain.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Reminder(val id: String, val targetId: String, val title: String, val message: String,
    val type: NotificationType, val phase: ReminderPhase, val targetType: String, val cycleKey: String)
class EvaluateRemindersUseCase(private val texts: TextResolver) {
    operator fun invoke(prescriptions: List<PrescriptionItem>, medications: List<MedicationItem>, today: LocalDate): List<Reminder> = buildList {
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        prescriptions.filter { it.isActive && it.renewalReminderEnabled && !today.isBefore(it.issueDate) }.forEach { p ->
            val expiry = p.expirationDate
            val phase = when {
                today.isAfter(expiry) -> ReminderPhase.EXPIRED
                today == expiry -> ReminderPhase.EXPIRES_TODAY
                !today.isBefore(expiry.minusDays(p.renewalReminderDaysBefore.coerceAtLeast(0).toLong())) -> ReminderPhase.RENEWAL
                else -> null
            }
            if (phase != null) {
                val suffix = when (phase) { ReminderPhase.EXPIRED -> "expired"; ReminderPhase.EXPIRES_TODAY -> "today"; else -> "renewal" }
                val cycle = "prescription:" + p.id + ":" + expiry
                add(Reminder(cycle + ":" + suffix, p.id,
                    when (phase) { ReminderPhase.EXPIRED -> texts.get(R.string.reminder_prescription_expired); ReminderPhase.EXPIRES_TODAY -> texts.get(R.string.reminder_prescription_expires_today); else -> texts.get(R.string.reminder_renewal_title) },
                    texts.get(R.string.reminder_expiration_message, p.title, expiry.format(format)), NotificationType.PRESCRIPTION_EXPIRING, phase, "PRESCRIPTION", cycle))
            }
        }
        medications.filter { it.hasRecurringPickup && !it.trackingArchived }.forEach { m ->
            val pickup = m.nextAvailableRefillDate ?: return@forEach
            val prescription = prescriptions.find { it.id == m.linkedPrescriptionId }
            // Warn as soon as the conflict is known, allowing time to renew before pickup.
            if (prescription != null && prescription.isActive && !today.isBefore(prescription.issueDate)
                && prescription.expirationDate.isBefore(pickup)) {
                val cycle = "pickup:${m.id}:$pickup"
                add(Reminder("$cycle:conflict:${prescription.id}:${prescription.expirationDate}", m.id,
                    texts.get(R.string.reminder_prescription_conflict),
                    texts.get(R.string.reminder_conflict_message, m.title, prescription.title, prescription.expirationDate.format(format), pickup.format(format)),
                    NotificationType.MEDICATION_PICKUP, ReminderPhase.PRESCRIPTION_PICKUP_CONFLICT, "PICKUP", cycle))
            }
            val phase = when {
                !today.isBefore(pickup) -> ReminderPhase.PICKUP_DUE
                !today.isBefore(pickup.minusDays(m.pickupReminderDaysBefore.coerceAtLeast(0).toLong())) -> ReminderPhase.PICKUP_UPCOMING
                else -> null
            }
            if (phase != null) {
                val cycle = "pickup:" + m.id + ":" + pickup
                add(Reminder(cycle + if (phase == ReminderPhase.PICKUP_DUE) ":due" else ":upcoming", m.id,
                    if (phase == ReminderPhase.PICKUP_DUE) { if (pickup == today) texts.get(R.string.reminder_pickup_today) else texts.get(R.string.reminder_pickup_pending) } else texts.get(R.string.reminder_pickup_title),
                    texts.get(R.string.reminder_pickup_message, m.title, pickup.format(format)), NotificationType.MEDICATION_PICKUP, phase, "PICKUP", cycle))
            }
        }
    }
}
