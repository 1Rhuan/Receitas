package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.NotificationDao
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import cc.rhuan.receitas.notifications.ReminderSynchronizer
import kotlinx.coroutines.sync.withLock
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

    override suspend fun addNotification(notification: NotificationItem) {
        notificationDao.insertNotification(notification.toEntity())
    }

    override suspend fun markAsRead(id: String) {
        ReminderSynchronizer.mutex.withLock {
            suppress(id, deleted = false)
            notificationDao.markAsRead(id)
        }
    }

    override suspend fun markAllAsRead() {
        ReminderSynchronizer.mutex.withLock {
            notificationDao.getAllNotifications().first().forEach { suppress(it.id, deleted = false) }
            notificationDao.markAllAsRead()
        }
    }

    override suspend fun deleteNotification(id: String) {
        ReminderSynchronizer.mutex.withLock {
            suppress(id, deleted = true)
            notificationDao.deleteNotification(id)
        }
    }

    override suspend fun clearAll() {
        ReminderSynchronizer.mutex.withLock {
            notificationDao.getAllNotifications().first().forEach { suppress(it.id, deleted = true) }
            notificationDao.deleteAllNotifications()
        }
    }

    override suspend fun refresh() { ReminderSynchronizer.sync(context) }

    private fun suppress(id: String, deleted: Boolean) {
        val editor = context.getSharedPreferences("delivered_reminders", Context.MODE_PRIVATE).edit().putBoolean(id, true)
        if (deleted) editor.putBoolean("dismissed:" + id, true)
        editor.commit()
        context.getSystemService(NotificationManager::class.java).cancel("reminder:" + id, 0)
    }

    override suspend fun performAction(id: String, action: NotificationAction): String? {
        var actionMessage: String? = null
        val entity = notificationDao.getNotificationById(id) ?: return null

        val updatedEntity = when (action) {
            NotificationAction.TAKE_MEDICATION -> {
                actionMessage = "Dose marcada como tomada!"
                entity.copy(isRead = true, actionTaken = "Tomado agora")
            }
            NotificationAction.SNOOZE_MEDICATION -> {
                actionMessage = "Lembrete adiado por 15 minutos."
                entity.copy(isRead = true, actionTaken = "Adiado (+15 min)")
            }
            NotificationAction.RENEW_PRESCRIPTION -> {
                actionMessage = "Renovação registrada."
                entity.copy(isRead = true, actionTaken = "Renovação registrada")
            }
            NotificationAction.REPLENISH_STOCK -> {
                actionMessage = "Retirada registrada."
                entity.copy(isRead = true, actionTaken = "Retirada registrada")
            }
            NotificationAction.MARK_AS_READ -> {
                actionMessage = "Notificação marcada como lida."
                entity.copy(isRead = true)
            }
            else -> {
                entity.copy(isRead = true)
            }
        }
        ReminderSynchronizer.mutex.withLock {
            suppress(id, deleted = false)
            notificationDao.updateNotification(updatedEntity)
        }
        return actionMessage
    }

    private fun NotificationEntity.toDomain() = NotificationItem(
        id = id,
        title = title,
        message = message,
        timestamp = runCatching { java.time.LocalDate.parse(timestamp).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(timestamp),
        type = type,
        isRead = isRead,
        actionTaken = actionTaken,
        targetId = targetId
    )

    private fun NotificationItem.toEntity() = NotificationEntity(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        type = type,
        isRead = isRead,
        actionTaken = actionTaken,
        targetId = targetId
    )
}
