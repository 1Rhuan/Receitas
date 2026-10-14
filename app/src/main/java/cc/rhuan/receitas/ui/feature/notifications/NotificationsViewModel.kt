package cc.rhuan.receitas.ui.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.*
import cc.rhuan.receitas.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    init { refresh() }
    private fun loadNotifications() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.notifications.isEmpty(), errorMessage = null) }
            repository.getNotifications().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível carregar os alertas.") }
            }.collect { items -> _uiState.update { it.copy(notifications = items, isLoading = false, errorMessage = null) } }
        }
    }
    private fun execute(action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action() } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(snackbarMessage = "Não foi possível atualizar os alertas. Tente novamente.") } }
        }
    }
    private fun obsolete() { _uiState.update { it.copy(snackbarMessage = "Este lembrete não está mais disponível. Atualize a lista.") } }
    fun onFilterSelected(filter: NotificationFilter) { _uiState.update { it.copy(selectedFilter = filter) } }
    fun markAsRead(id: String) = execute { repository.markAsRead(id) }
    fun markAllAsRead() = execute { repository.markAllAsRead(); _uiState.update { it.copy(snackbarMessage = "Todos os alertas foram marcados como lidos") } }
    fun deleteNotification(id: String) = execute { repository.deleteNotification(id); _uiState.update { it.copy(snackbarMessage = "Alerta removido") } }
    fun clearAll() = execute { repository.clearAll(); _uiState.update { it.copy(snackbarMessage = "Todos os alertas foram removidos") } }
    fun openRenewal(id: String, onReady: (String) -> Unit) = execute {
        repository.refresh()
        val alert = repository.getNotifications().first().find { it.id == id }
        val target = alert?.targetId
        if (alert == null || alert.resolvedAt != null || alert.targetType != "PRESCRIPTION" || target == null || prescriptionRepository.getPrescriptionById(target)?.isActive != true) { obsolete(); return@execute }
        onReady(target)
    }
    private suspend fun activePickup(id: String): Pair<MedicationItem, LocalDate>? {
        repository.refresh()
        val alert = repository.getNotifications().first().find { it.id == id }
        if (alert == null || alert.resolvedAt != null || alert.targetType != "PICKUP") return null
        val target = alert.targetId ?: return null
        val cycle = alert.cycleKey?.substringAfterLast(':')?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val med = medicationRepository.getMedicationById(target) ?: return null
        return if (med.nextAvailableRefillDate == cycle) med to cycle else null
    }
    fun preparePickup(id: String, onReady: (MedicationItem) -> Unit) = execute {
        val active = activePickup(id)
        if (active == null) obsolete() else onReady(active.first)
    }
    fun registerPickup(id: String, date: LocalDate, early: Boolean) = execute {
        val active = activePickup(id)
        if (active == null) { obsolete(); return@execute }
        val result = medicationRepository.registerPickup(active.first.id, date, early, active.second)
        repository.refresh()
        _uiState.update { it.copy(snackbarMessage = result.message()) }
    }
    fun dismissSnackbar() { _uiState.update { it.copy(snackbarMessage = null) } }
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        loadNotifications()
        _uiState.update { it.copy(isRefreshing = true) }
        execute { try { repository.refresh() } finally { _uiState.update { it.copy(isRefreshing = false) } } }
    }
}
