package cc.rhuan.receitas.ui.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.ReminderPhase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getNotifications().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, snackbarMessage = "Não foi possível carregar os alertas.") }
            }.collect { items ->
                _uiState.update { it.copy(notifications = items, isLoading = false) }
            }
        }
        refresh()
    }
    private fun execute(action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action() } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(snackbarMessage = "Não foi possível atualizar os alertas. Tente novamente.") } }
        }
    }
    fun onFilterSelected(filter: NotificationFilter) { _uiState.update { it.copy(selectedFilter = filter) } }
    fun markAsRead(id: String) = execute { repository.markAsRead(id) }
    fun markAllAsRead() = execute {
        repository.markAllAsRead()
        _uiState.update { it.copy(snackbarMessage = "Todos os alertas foram marcados como lidos") }
    }
    fun deleteNotification(id: String) = execute {
        repository.deleteNotification(id)
        _uiState.update { it.copy(snackbarMessage = "Alerta removido") }
    }
    fun clearAll() = execute {
        repository.clearAll()
        _uiState.update { it.copy(snackbarMessage = "Todos os alertas foram removidos") }
    }
    fun performAction(id: String, action: NotificationAction) = execute {
        // Recheck the current cycle before recording an action from an old card.
        repository.refresh()
        val notification = repository.getNotifications().first().find { it.id == id } ?: return@execute
        if (notification.isObsolete || notification.actionTaken != null) return@execute
        val targetId = notification.targetId ?: return@execute
        when (action) {
            NotificationAction.RENEW_PRESCRIPTION -> {
                if (notification.phase !in listOf(ReminderPhase.RENEWAL, ReminderPhase.EXPIRES_TODAY, ReminderPhase.EXPIRED)) return@execute
                prescriptionRepository.renewPrescription(targetId, null, null)
            }
            NotificationAction.REPLENISH_STOCK -> {
                if (notification.phase !in listOf(ReminderPhase.PICKUP_UPCOMING, ReminderPhase.PICKUP_DUE)) return@execute
                medicationRepository.replenishStock(targetId, null, null)
            }
            NotificationAction.MARK_AS_READ -> repository.markAsRead(id)
            else -> return@execute
        }
        val message = repository.performAction(id, action)
        repository.refresh()
        _uiState.update { it.copy(snackbarMessage = message) }
    }
    fun dismissSnackbar() { _uiState.update { it.copy(snackbarMessage = null) } }
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        execute {
            try { repository.refresh() }
            finally { _uiState.update { it.copy(isRefreshing = false) } }
        }
    }
}
