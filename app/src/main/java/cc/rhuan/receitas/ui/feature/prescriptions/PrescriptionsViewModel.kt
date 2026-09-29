package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrescriptionsViewModel @Inject constructor(
    private val repository: PrescriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState(isLoading = true))
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    init {
        loadPrescriptions()
    }

    fun loadPrescriptions() {
        viewModelScope.launch {
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
                        errorMessage = e.message ?: "Erro ao carregar receitas",
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
