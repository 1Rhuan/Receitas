package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

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
    private val texts: TextResolver,
    private val repository: PrescriptionRepository,
    private val medicationRepository: cc.rhuan.receitas.data.repository.MedicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState(isLoading = true))
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var medicationJob: Job? = null

    init {
        loadPrescriptions()
    }

    fun loadPrescriptions() {
        medicationJob?.cancel()
        medicationJob = viewModelScope.launch {
            try {
                medicationRepository.getMedications().collect { list ->
                    _uiState.update { it.copy(medications = list) }
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { android.util.Log.w("PrescriptionsViewModel", "Unable to load medication context", e) }
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
                        errorMessage = texts.get(R.string.prescriptions_load_error),
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
