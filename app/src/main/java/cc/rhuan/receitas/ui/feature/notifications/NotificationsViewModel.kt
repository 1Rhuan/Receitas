package cc.rhuan.receitas.ui.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.FakeNotificationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.domain.model.NotificationAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository = FakeNotificationRepository(),
) : ViewModel() {

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
            val message = repository.performAction(id, action)
            if (message != null) {
                _uiState.update { it.copy(snackbarMessage = message) }
            }
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
