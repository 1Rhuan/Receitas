package cc.rhuan.receitas.ui.feature.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.FakeMedicationRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicationsViewModel(
    private val repository: MedicationRepository = FakeMedicationRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState(isLoading = true))
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
    }

    fun loadMedications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.getMedications().collect { list ->
                    _uiState.update {
                        it.copy(
                            medications = list,
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar medicamentos",
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: MedicationFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onSortOrderSelected(sortOrder: MedicationSortOrder) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder) }
    }
}
