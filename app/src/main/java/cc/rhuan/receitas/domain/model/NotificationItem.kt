package cc.rhuan.receitas.domain.model

// Retired types remain readable for existing history; they generate no new alerts.
enum class NotificationType { MEDICATION_REMINDER, PRESCRIPTION_EXPIRING, MEDICATION_STOCK, MEDICATION_PICKUP, SYSTEM_ALERT }
enum class NotificationAction { VIEW_PRESCRIPTION, RENEW_PRESCRIPTION, VIEW_MEDICATION, REGISTER_PICKUP, MARK_AS_READ }
enum class ReminderPhase { RENEWAL, EXPIRES_TODAY, EXPIRED, PICKUP_UPCOMING, PICKUP_DUE }
data class NotificationItem(val id: String, val title: String, val message: String, val timestamp: String,
    val type: NotificationType, val isRead: Boolean = false, val actionTaken: String? = null, val targetId: String? = null,
    val phase: ReminderPhase? = null, val targetType: String? = null, val cycleKey: String? = null,
    val resolvedAt: Long? = null, val resolutionReason: String? = null) {
    val isScheduledReminder: Boolean get() = phase != null && targetType in listOf("PRESCRIPTION", "PICKUP")
    val isObsolete: Boolean get() = resolutionReason == "OBSOLETE"
}
