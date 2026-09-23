package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.rhuan.receitas.domain.model.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType = NotificationType.SYSTEM_ALERT,
    val isRead: Boolean = false,
    val actionTaken: String? = null,
    val targetId: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
