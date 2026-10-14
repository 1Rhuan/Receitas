package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.AppDatabase
import kotlinx.coroutines.flow.first

/** Called inside the business transaction; delivery cancellation follows the normal synchronizer. */
internal suspend fun AppDatabase.resolveReminders(targetId: String, targetType: String, reason: String, label: String) {
    notificationDao().getAllNotifications().first().filter { it.targetId == targetId && it.targetType == targetType && it.resolvedAt == null }.forEach {
        notificationDao().updateNotification(it.copy(isRead = true, actionTaken = label, resolvedAt = System.currentTimeMillis(), resolutionReason = reason))
    }
}
