package cc.rhuan.receitas.ui.feature.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.FakeReceitaRepository
import cc.rhuan.receitas.data.repository.ReceitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val repository: ReceitaRepository = FakeReceitaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState(isLoading = true))
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadReceitas()
    }

    fun loadReceitas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.getReceitas().collect { list ->
                    _uiState.update {
                        it.copy(
                            receitas = list,
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar receitas"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: ReceitaFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
