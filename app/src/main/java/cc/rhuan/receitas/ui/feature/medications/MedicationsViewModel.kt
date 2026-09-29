package cc.rhuan.receitas.ui.feature.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.domain.model.MedicationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState())
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
    }

    fun loadMedications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            medicationRepository.getMedications().collect { meds ->
                _uiState.update { state ->
                    val filtered = applyFilter(meds, state.searchQuery, state.selectedSourceFilter)
                    state.copy(
                        medications = meds,
                        filteredMedications = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilter(state.medications, query, state.selectedSourceFilter)
            state.copy(searchQuery = query, filteredMedications = filtered)
        }
    }

    fun onSourceFilterSelected(sourceLabel: String) {
        _uiState.update { state ->
            val filtered = applyFilter(state.medications, state.searchQuery, sourceLabel)
            state.copy(selectedSourceFilter = sourceLabel, filteredMedications = filtered)
        }
    }

    fun replenishStock(medicationId: String) {
        viewModelScope.launch {
            medicationRepository.replenishStock(id = medicationId, title = null, message = null)
            _uiState.update { it.copy(snackbarMessage = "Estoque atualizado com sucesso!") }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun applyFilter(
        list: List<MedicationItem>,
        query: String,
        filter: String
    ): List<MedicationItem> {
        return list.filter { med ->
            val matchesQuery = query.isBlank() ||
                med.title.contains(query, ignoreCase = true) ||
                (med.activeIngredient?.contains(query, ignoreCase = true) == true) ||
                med.dosage.contains(query, ignoreCase = true)

            val matchesSource = when (filter) {
                "TODOS" -> true
                "Estoque Baixo" -> med.isStockLow || med.stockQuantity == 0
                else -> med.category.equals(filter, ignoreCase = true) || med.source.label.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesSource
        }
    }
}
