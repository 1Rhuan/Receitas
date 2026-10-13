package cc.rhuan.receitas.domain.model

enum class NotificationType {
    MEDICATION_REMINDER,
    PRESCRIPTION_EXPIRING,
    MEDICATION_STOCK,
    MEDICATION_PICKUP,
    SYSTEM_ALERT,
}

enum class NotificationAction {
    TAKE_MEDICATION,
    SNOOZE_MEDICATION,
    VIEW_PRESCRIPTION,
    RENEW_PRESCRIPTION,
    VIEW_MEDICATION,
    REPLENISH_STOCK,
    MARK_AS_READ,
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val actionTaken: String? = null,
    val targetId: String? = null,
) {
    val isScheduledReminder: Boolean get() = id.startsWith("prescription:") || id.startsWith("pickup:")
    val isObsolete: Boolean get() = actionTaken == "Lembrete encerrado"
    val phase: ReminderPhase? get() = if (!isScheduledReminder) null else when (id.substringAfterLast(':')) {
        "renewal" -> ReminderPhase.RENEWAL
        "today" -> ReminderPhase.EXPIRES_TODAY
        "expired" -> ReminderPhase.EXPIRED
        "upcoming" -> ReminderPhase.PICKUP_UPCOMING
        "due" -> ReminderPhase.PICKUP_DUE
        else -> null
    }
}

enum class ReminderPhase { RENEWAL, EXPIRES_TODAY, EXPIRED, PICKUP_UPCOMING, PICKUP_DUE }
