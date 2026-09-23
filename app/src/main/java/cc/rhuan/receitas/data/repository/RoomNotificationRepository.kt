package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.data.local.dao.NotificationDao
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addNotification(notification: NotificationItem) {
        notificationDao.insertNotification(notification.toEntity())
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    override suspend fun clearAll() {
        notificationDao.deleteAllNotifications()
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
                actionMessage = "Solicitação de renovação iniciada."
                entity.copy(isRead = true, actionTaken = "Renovação solicitada")
            }
            NotificationAction.REPLENISH_STOCK -> {
                actionMessage = "Solicitação de reposição registrada."
                entity.copy(isRead = true, actionTaken = "Reposição solicitada")
            }
            NotificationAction.MARK_AS_READ -> {
                actionMessage = "Notificação marcada como lida."
                entity.copy(isRead = true)
            }
            else -> {
                entity.copy(isRead = true)
            }
        }
        notificationDao.updateNotification(updatedEntity)
        return actionMessage
    }

    private fun NotificationEntity.toDomain() = NotificationItem(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
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
