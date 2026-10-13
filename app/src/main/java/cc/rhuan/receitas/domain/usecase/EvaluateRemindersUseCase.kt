package cc.rhuan.receitas.domain.usecase

import cc.rhuan.receitas.data.local.entity.MedicationEntity
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity
import cc.rhuan.receitas.domain.model.NotificationType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Reminder(val id: String, val targetId: String, val title: String, val message: String, val type: NotificationType)

class EvaluateRemindersUseCase {
    operator fun invoke(prescriptions: List<PrescriptionEntity>, medications: List<MedicationEntity>, today: LocalDate): List<Reminder> = buildList {
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        prescriptions.filter { it.renewalReminderEnabled }.forEach { p ->
            val expiry = p.issueDate.plusDays((p.validityDays ?: p.type?.defaultValidityDays ?: 180).toLong())
            val phase = when {
                today.isAfter(expiry) -> "expired"
                today == expiry -> "today"
                !today.isBefore(expiry.minusDays(p.renewalReminderDaysBefore.coerceAtLeast(0).toLong())) -> "renewal"
                else -> null
            }
            if (phase != null) add(Reminder(
                "prescription:" + p.id + ":" + expiry + ":" + phase, p.id,
                when (phase) { "expired" -> "Receita vencida"; "today" -> "Receita vence hoje"; else -> "Lembrete de renovação" },
                p.title + ": validade até " + expiry.format(format) + ". Providencie a renovação.", NotificationType.PRESCRIPTION_EXPIRING,
            ))
        }
        medications.filter { it.hasRecurringPickup }.forEach { m ->
            val interval = m.pickupIntervalDays ?: return@forEach
            if (interval <= 0) return@forEach
            val pickup = m.lastPickupDate?.plusDays(interval.toLong()) ?: return@forEach
            val phase = when {
                !today.isBefore(pickup) -> "due"
                !today.isBefore(pickup.minusDays(m.pickupReminderDaysBefore.coerceAtLeast(0).toLong())) -> "upcoming"
                else -> null
            }
            if (phase != null) add(Reminder(
                "pickup:" + m.id + ":" + pickup + ":" + phase, m.id,
                if (phase == "due") "Data de retirada alcançada" else "Lembrete de retirada",
                m.title + ": retirada prevista para " + pickup.format(format) + ". Confirme a disponibilidade com a farmácia.", NotificationType.MEDICATION_PICKUP,
            ))
        }
    }
}
