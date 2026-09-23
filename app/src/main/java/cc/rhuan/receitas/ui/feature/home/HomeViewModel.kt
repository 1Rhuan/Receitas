package cc.rhuan.receitas.ui.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.DailyDoseRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.DailyDoseItem
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationAction
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.NotificationType
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.usecase.GenerateNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
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
    private val dailyDoseRepository: DailyDoseRepository,
) : AndroidViewModel(application) {

    private val generateNotificationsUseCase: GenerateNotificationsUseCase = GenerateNotificationsUseCase()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
                    dailyDoseRepository.getDailyDoses(),
                ) { prescriptions, medicamentos, notifications, dailyDoses ->
                    HomeData(prescriptions, medicamentos, notifications, dailyDoses)
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
                            dailyDoses = data.dailyDoses,
                            unreadNotificationsCount = unreadCount,
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                }
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

    fun markDoseAsTaken(doseId: String) {
        viewModelScope.launch {
            dailyDoseRepository.markDoseAsTaken(doseId)
            _uiState.update { it.copy(snackbarMessage = "Dose marcada como tomada!") }

            val targetDose = _uiState.value.dailyDoses.find { it.id == doseId }
            if (targetDose != null) {
                val activeNotif = _uiState.value.notifications.find { notif ->
                    !notif.isRead && (notif.type == NotificationType.MEDICATION_REMINDER) &&
                        ((notif.targetId == targetDose.medicationId) || (notif.targetId == targetDose.id) ||
                            notif.message.contains(targetDose.medicationName, ignoreCase = true))
                }
                activeNotif?.let { notif ->
                    notificationRepository.performAction(notif.id, NotificationAction.TAKE_MEDICATION)
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

            val message = notificationRepository.performAction(id, action)
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
    val dailyDoses: List<DailyDoseItem>,
)
