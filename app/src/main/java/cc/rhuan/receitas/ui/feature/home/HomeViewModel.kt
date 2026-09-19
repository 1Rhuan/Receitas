package cc.rhuan.receitas.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.FakeMedicationRepository
import cc.rhuan.receitas.data.repository.FakeReceitaRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.ReceitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val receitaRepository: ReceitaRepository = FakeReceitaRepository(),
    private val medicationRepository: MedicationRepository = FakeMedicationRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                combine(
                    receitaRepository.getReceitas(),
                    medicationRepository.getMedications(),
                ) { receitas, medicamentos ->
                    HomeUiState(
                        receitas = receitas,
                        medicamentos = medicamentos,
                        isLoading = false,
                        isRefreshing = false,
                        dismissedAlerts = _uiState.value.dismissedAlerts,
                    )
                }.collect { newState ->
                    _uiState.value = newState
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

    fun dismissAlert(id: String) {
        _uiState.update { it.copy(dismissedAlerts = it.dismissedAlerts + id) }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadData()
        }
    }
}
