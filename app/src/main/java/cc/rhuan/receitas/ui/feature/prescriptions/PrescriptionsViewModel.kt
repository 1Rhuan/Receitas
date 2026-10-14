package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrescriptionsViewModel @Inject constructor(
    private val repository: PrescriptionRepository,
    private val medicationRepository: cc.rhuan.receitas.data.repository.MedicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState(isLoading = true))
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var purchaseJob: Job? = null

    init {
        loadPrescriptions()
    }

    fun loadPrescriptions() {
        purchaseJob?.cancel()
        purchaseJob = viewModelScope.launch {
            _uiState.update { it.copy(purchaseError = false) }
            try {
                medicationRepository.getMedications().collect { list ->
                    _uiState.update { it.copy(medications = list) }
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _uiState.update { it.copy(purchaseError = true) } }
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.getPrescriptions().collect { list ->
                    _uiState.update {
                        it.copy(
                            prescriptions = list,
                            isLoading = false,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Não foi possível carregar as receitas.",
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: PrescriptionFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onSortOrderSelected(sortOrder: PrescriptionSortOrder) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder) }
    }
}
