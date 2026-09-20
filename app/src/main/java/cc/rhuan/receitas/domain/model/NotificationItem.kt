package cc.rhuan.receitas.domain.model

enum class NotificationType {
    MEDICATION_REMINDER,
    PRESCRIPTION_EXPIRING,
    MEDICATION_STOCK,
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
)
