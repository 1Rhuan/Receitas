package cc.rhuan.receitas.data.repository

import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface NotificationRepository {
    fun getNotifications(): Flow<List<NotificationItem>>
    suspend fun addNotification(notification: NotificationItem)
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun clearAll()
    suspend fun performAction(id: String, action: NotificationAction): String?
}

class FakeNotificationRepository : NotificationRepository {
    companion object {
        private val _notifications = MutableStateFlow(
            listOf(
                NotificationItem(
                    id = "1",
                    title = "Hora do Medicamento",
                    message = "Está na hora de tomar Amoxicilina (500mg) - Dose das 14:00.",
                    timestamp = "Há 10 min",
                    type = NotificationType.MEDICATION_REMINDER,
                    isRead = false,
                    targetId = "1",
                ),
                NotificationItem(
                    id = "2",
                    title = "Receita a Vencer",
                    message = "Sua receita de Tratamento Dermatológico vence em 2 dias. Solicite a renovação com seu médico.",
                    timestamp = "Há 1 hora",
                    type = NotificationType.PRESCRIPTION_EXPIRING,
                    isRead = false,
                    targetId = "2",
                ),
                NotificationItem(
                    id = "3",
                    title = "Estoque de Medicamento Baixo",
                    message = "A Losartana Potássica está com poucas unidades. Lembre-se de providenciar a reposição.",
                    timestamp = "Hoje, 09:30",
                    type = NotificationType.MEDICATION_STOCK,
                    isRead = true,
                    targetId = "2",
                ),
                NotificationItem(
                    id = "4",
                    title = "Dose Tomada",
                    message = "Você tomou Omeprazol 20mg às 07:30.",
                    timestamp = "Hoje, 07:30",
                    type = NotificationType.MEDICATION_REMINDER,
                    isRead = true,
                    actionTaken = "Tomado às 07:30",
                    targetId = "4",
                ),
                NotificationItem(
                    id = "5",
                    title = "Receita Vencida",
                    message = "A receita de Avaliação Ortopédica expirou. Agende uma consulta para nova prescrição.",
                    timestamp = "Ontem, 18:00",
                    type = NotificationType.PRESCRIPTION_EXPIRING,
                    isRead = true,
                    targetId = "5",
                ),
                NotificationItem(
                    id = "6",
                    title = "Lembrete de Atualização",
                    message = "Mantenha suas receitas e dados médicos sempre atualizados no aplicativo para receber alertas precisos.",
                    timestamp = "Há 2 dias",
                    type = NotificationType.SYSTEM_ALERT,
                    isRead = true,
                ),
            ),
        )
    }

    override fun getNotifications(): Flow<List<NotificationItem>> = _notifications.asStateFlow()

    override suspend fun addNotification(notification: NotificationItem) {
        _notifications.update { current ->
            if (current.none { it.id == notification.id }) {
                listOf(notification) + current
            } else {
                current
            }
        }
    }

    override suspend fun markAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    override suspend fun markAllAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    override suspend fun deleteNotification(id: String) {
        _notifications.update { list ->
            list.filter { it.id != id }
        }
    }

    override suspend fun clearAll() {
        _notifications.value = emptyList()
    }

    override suspend fun performAction(id: String, action: NotificationAction): String? {
        var actionMessage: String? = null
        _notifications.update { list ->
            list.map { item ->
                if (item.id == id) {
                    when (action) {
                        NotificationAction.TAKE_MEDICATION -> {
                            actionMessage = "Dose marcada como tomada!"
                            item.copy(isRead = true, actionTaken = "Tomado agora")
                        }
                        NotificationAction.SNOOZE_MEDICATION -> {
                            actionMessage = "Lembrete adiado por 15 minutos."
                            item.copy(isRead = true, actionTaken = "Adiado (+15 min)")
                        }
                        NotificationAction.RENEW_PRESCRIPTION -> {
                            actionMessage = "Solicitação de renovação iniciada."
                            item.copy(isRead = true, actionTaken = "Renovação solicitada")
                        }
                        NotificationAction.REPLENISH_STOCK -> {
                            actionMessage = "Solicitação de reposição registrada."
                            item.copy(isRead = true, actionTaken = "Reposição solicitada")
                        }
                        NotificationAction.MARK_AS_READ -> {
                            actionMessage = "Notificação marcada como lida."
                            item.copy(isRead = true)
                        }
                        else -> {
                            item.copy(isRead = true)
                        }
                    }
                } else item
            }
        }
        return actionMessage
    }
}
