package cc.rhuan.receitas.ui.feature.notifications

import androidx.annotation.StringRes
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.NotificationType

enum class NotificationFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    UNREAD(R.string.filter_unread),
    MEDICATIONS(R.string.filter_medications),
    PRESCRIPTIONS(R.string.filter_prescriptions),
}

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val snackbarMessage: String? = null,
) {
    val unreadCount: Int
        get() = notifications.count { !it.isRead }

    val filteredNotifications: List<NotificationItem>
        get() = when (selectedFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilter.MEDICATIONS -> notifications.filter {
                (it.type == NotificationType.MEDICATION_REMINDER) || (it.type == NotificationType.MEDICATION_STOCK)
            }
            NotificationFilter.PRESCRIPTIONS -> notifications.filter {
                it.type == NotificationType.PRESCRIPTION_EXPIRING
            }
        }
}
