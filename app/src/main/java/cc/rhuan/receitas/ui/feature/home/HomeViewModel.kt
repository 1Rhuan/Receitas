package cc.rhuan.receitas.ui.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.usecase.GenerateNotificationsUseCase
import cc.rhuan.receitas.ui.feature.home.components.TrackingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val notificationRepository: NotificationRepository,
) : AndroidViewModel(application) {

    private val generateNotificationsUseCase: GenerateNotificationsUseCase = GenerateNotificationsUseCase()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadDataJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                combine(
                    prescriptionRepository.getPrescriptions(),
                    medicationRepository.getMedications(),
                    notificationRepository.getNotifications(),
                ) { prescriptions, medicamentos, notifications ->
                    HomeData(prescriptions, medicamentos, notifications)
                }.collect { data ->
                    generateNotificationsUseCase(
                        context = getApplication(),
                        medications = data.medicamentos,
                        prescriptions = data.prescriptions,
                        notificationRepository = notificationRepository,
                    )

                    val unreadCount = data.notifications.count { !it.isRead }
                    _uiState.update { currentState ->
                        currentState.copy(
                            prescriptions = data.prescriptions,
                            medicamentos = data.medicamentos,
                            notifications = data.notifications,
                            unreadNotificationsCount = unreadCount,
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Erro ao carregar dados do início",
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onQuickAction(item: TrackingItem) {
        viewModelScope.launch {
            when (item) {
                is TrackingItem.Prescription -> {
                    prescriptionRepository.renewPrescription(item.targetId, item.title, null)
                    _uiState.update { it.copy(snackbarMessage = "Receita marcada como renovada!") }
                }
                is TrackingItem.Medication -> {
                    medicationRepository.replenishStock(item.targetId, item.title, null)
                    _uiState.update { it.copy(snackbarMessage = "Medicamento marcado como retirado!") }
                }
            }
        }
    }

    fun onSecondaryActionWithDate(item: TrackingItem, customDate: LocalDate) {
        viewModelScope.launch {
            val dateStr = customDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            when (item) {
                is TrackingItem.Prescription -> {
                    prescriptionRepository.renewPrescription(item.targetId, item.title, null, customDate)
                    _uiState.update { it.copy(snackbarMessage = "Renovação registrada em $dateStr!") }
                }
                is TrackingItem.Medication -> {
                    medicationRepository.replenishStock(item.targetId, item.title, null, customDate)
                    _uiState.update { it.copy(snackbarMessage = "Retirada registrada em $dateStr!") }
                }
            }
        }
    }

    fun performNotificationAction(id: String, action: NotificationAction) {
        viewModelScope.launch {
            val targetNotification = _uiState.value.notifications.find { it.id == id }
            val targetId = targetNotification?.targetId
            val title = targetNotification?.title
            val messageText = targetNotification?.message

            when (action) {
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

            val message = notificationRepository.performAction(id, action)
            if (message != null) {
                _uiState.update { it.copy(snackbarMessage = message) }
            }
        }
    }

    fun dismissNotification(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(dismissedAlerts = it.dismissedAlerts + id) }
            notificationRepository.markAsRead(id)
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun refreshData() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadData()
    }
}

private data class HomeData(
    val prescriptions: List<PrescriptionItem>,
    val medicamentos: List<MedicationItem>,
    val notifications: List<NotificationItem>,
)
