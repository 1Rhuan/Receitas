package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.NotificationDao
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.domain.model.NotificationItem
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import cc.rhuan.receitas.notifications.ReminderSynchronizer
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { list ->
            list.map { it.toDomain() }.filter { it.isScheduledReminder }
        }
    }

    override suspend fun markAsRead(id: String) {
        ReminderSynchronizer.mutex.withLock {
            suppress(listOf(id), deleted = false)
            notificationDao.markAsRead(id)
        }
    }

    override suspend fun markAllAsRead() {
        ReminderSynchronizer.mutex.withLock {
            suppress(notificationDao.getAllNotifications().first().map { it.id }, deleted = false)
            notificationDao.markAllAsRead()
        }
    }

    override suspend fun deleteNotification(id: String) {
        ReminderSynchronizer.mutex.withLock {
            suppress(listOf(id), deleted = true)
            notificationDao.deleteNotification(id)
        }
    }

    override suspend fun clearAll() {
        ReminderSynchronizer.mutex.withLock {
            suppress(notificationDao.getAllNotifications().first().map { it.id }, deleted = true)
            notificationDao.deleteAllNotifications()
        }
    }

    override suspend fun refresh() { ReminderSynchronizer.sync(context) }

    @android.annotation.SuppressLint("ApplySharedPref", "UseKtx")
    private suspend fun suppress(ids: List<String>, deleted: Boolean) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        val editor = context.getSharedPreferences("delivered_reminders", Context.MODE_PRIVATE).edit()
        ids.forEach { id -> editor.putBoolean(id, true); if (deleted) editor.putBoolean("dismissed:" + id, true) }
        // Durable suppression must succeed before deleting Room history; run blocking I/O off the UI thread.
        check(editor.commit()) { "Não foi possível preservar a supressão dos alertas" }
        val manager = context.getSystemService(NotificationManager::class.java)
        ids.forEach { manager.cancel("reminder:" + it, 0) }
    }

    private fun NotificationEntity.toDomain() = NotificationItem(
        id = id,
        title = title,
        message = message,
        timestamp = runCatching { java.time.LocalDate.parse(timestamp).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(timestamp),
        type = type,
        isRead = isRead,
        actionTaken = actionTaken,
        targetId = targetId, phase = phase?.let { cc.rhuan.receitas.domain.model.ReminderPhase.entries.firstOrNull { p -> p.name == it } },
        targetType = targetType, cycleKey = cycleKey, resolvedAt = resolvedAt, resolutionReason = resolutionReason
    )

}
