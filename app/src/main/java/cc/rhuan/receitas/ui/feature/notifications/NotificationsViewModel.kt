package cc.rhuan.receitas.ui.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.DailyDoseRepository
import cc.rhuan.receitas.data.repository.FakeDailyDoseRepository
import cc.rhuan.receitas.data.repository.FakeMedicationRepository
import cc.rhuan.receitas.data.repository.FakeNotificationRepository
import cc.rhuan.receitas.data.repository.FakePrescriptionRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.NotificationAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationsViewModel(
    private val repository: NotificationRepository = FakeNotificationRepository(),
    private val prescriptionRepository: PrescriptionRepository = FakePrescriptionRepository(),
    private val medicationRepository: MedicationRepository = FakeMedicationRepository(),
    private val dailyDoseRepository: DailyDoseRepository = FakeDailyDoseRepository(),
) : ViewModel() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getNotifications().collect { items ->
                _uiState.update { currentState ->
                    currentState.copy(
                        notifications = items,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    fun onFilterSelected(filter: NotificationFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
            _uiState.update { it.copy(snackbarMessage = "Todas as notificações foram marcadas como lidas") }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            _uiState.update { it.copy(snackbarMessage = "Notificação removida") }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _uiState.update { it.copy(snackbarMessage = "Todas as notificações foram removidas") }
        }
    }

    fun performAction(id: String, action: NotificationAction) {
        viewModelScope.launch {
            val targetNotification = _uiState.value.notifications.find { it.id == id }
            val targetId = targetNotification?.targetId
            val title = targetNotification?.title
            val messageText = targetNotification?.message

            when (action) {
                NotificationAction.TAKE_MEDICATION -> {
                    dailyDoseRepository.markMatchingDoseAsTaken(targetId, id, title, messageText)
                }
                NotificationAction.SNOOZE_MEDICATION -> {
                    dailyDoseRepository.snoozeMatchingDose(targetId, id, title, messageText) { timeStr ->
                        addMinutesToTime(timeStr)
                    }
                }
                NotificationAction.RENEW_PRESCRIPTION -> {
                    prescriptionRepository.renewPrescription(targetId, title, messageText)
                }
                NotificationAction.REPLENISH_STOCK -> {
                    medicationRepository.replenishStock(targetId, title, messageText)
                }
                else -> {
                    // MARK_AS_READ, etc.
                }
            }

            val message = repository.performAction(id, action)
            if (message != null) {
                _uiState.update { it.copy(snackbarMessage = message) }
            }
        }
    }

    private fun addMinutesToTime(timeStr: String, minutesToAdd: Long = 15L): String {
        return try {
            val time = LocalTime.parse(timeStr)
            val newTime = time.plusMinutes(minutesToAdd)
            newTime.format(timeFormatter)
        } catch (_: Exception) {
            "$timeStr (+${minutesToAdd}m)"
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadNotifications()
        }
    }
}
