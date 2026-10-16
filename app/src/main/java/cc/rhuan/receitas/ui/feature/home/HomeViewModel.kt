package cc.rhuan.receitas.ui.feature.home

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.message
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.NotificationItem
import cc.rhuan.receitas.domain.model.PrescriptionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
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
    private val texts: TextResolver,
    application: Application,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val notificationRepository: NotificationRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState(texts = texts))
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
                ) { prescriptions, medications, notifications ->
                    HomeData(prescriptions, medications, notifications)
                }.collect { data ->

                    val unreadCount = data.notifications.count { !it.isRead }
                    _uiState.update { currentState ->
                        currentState.copy(
                            prescriptions = data.prescriptions,
                            medications = data.medications,
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
                        errorMessage = texts.get(R.string.tracking_load_error),
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

    fun registerPickup(id: String, date: LocalDate, confirmedEarly: Boolean, expectedCycle: LocalDate?) {
        viewModelScope.launch {
            try { val result = medicationRepository.registerPickup(id, date, confirmedEarly, expectedCycle)
                _uiState.update { it.copy(snackbarMessage = texts.resolve(result.message())) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(snackbarMessage = texts.get(R.string.pickup_record_error)) } }
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
    val medications: List<MedicationItem>,
    val notifications: List<NotificationItem>,
)
