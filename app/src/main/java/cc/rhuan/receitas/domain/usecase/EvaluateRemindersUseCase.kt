package cc.rhuan.receitas.domain.usecase

import cc.rhuan.receitas.domain.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Reminder(val id: String, val targetId: String, val title: String, val message: String,
    val type: NotificationType, val phase: ReminderPhase, val targetType: String, val cycleKey: String)
class EvaluateRemindersUseCase {
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
                    when (phase) { ReminderPhase.EXPIRED -> "Receita vencida"; ReminderPhase.EXPIRES_TODAY -> "Receita vence hoje"; else -> "Lembrete de renovação" },
                    p.title + ": validade até " + expiry.format(format) + ". Providencie uma nova receita.", NotificationType.PRESCRIPTION_EXPIRING, phase, "PRESCRIPTION", cycle))
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
                    "Receita vence antes da retirada",
                    "${m.title}: a receita ${prescription.title} vence em ${prescription.expirationDate.format(format)}, antes da retirada prevista para ${pickup.format(format)}. Providencie uma nova receita.",
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
                    if (phase == ReminderPhase.PICKUP_DUE) { if (pickup == today) "Retirada prevista para hoje" else "Retirada pendente" } else "Lembrete de retirada",
                    m.title + ": retirada prevista para " + pickup.format(format) + ". Confirme a disponibilidade com a farmácia.", NotificationType.MEDICATION_PICKUP, phase, "PICKUP", cycle))
            }
        }
    }
}
