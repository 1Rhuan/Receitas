package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow
interface NotificationRepository {
    suspend fun refresh()
    fun getNotifications(): Flow<List<NotificationItem>>
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun clearAll()
}
