package cc.rhuan.receitas.ui.feature.medications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(handle: SavedStateHandle, private val repository: MedicationRepository) : ViewModel() {
    private val id: String = handle["medicationId"] ?: ""
    private val _uiState = MutableStateFlow(MedicationDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private var loadJob: Job? = null
    init { loadMedication() }
    fun loadMedication() {
        loadJob?.cancel()
        if (id.isBlank()) { _uiState.update { it.copy(isLoading = false, errorMessage = "Medicamento não encontrado.") }; return }
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getMedications().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível carregar o medicamento.") }
            }.collect { medications ->
                val med = medications.find { it.id == id }
                _uiState.update { it.copy(medication = med, isLoading = false, errorMessage = if (med == null) "Medicamento não encontrado." else null) }
            }
        }
    }
    private fun execute(action: suspend () -> String) {
        viewModelScope.launch {
            try { _message.value = action() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _message.value = "Não foi possível concluir esta ação. Tente novamente." }
        }
    }
    fun clearMessage() { _message.value = null }
    fun pickup(date: LocalDate, early: Boolean, cycle: LocalDate?) = execute { repository.registerPickup(id, date, early, cycle).message() }
    fun correct(event: String, date: LocalDate) = execute { val r = repository.correctPickup(event, date); if (r == OperationResult.Success) "Data corrigida." else r.message() }
    fun deleteEvent(event: String) = execute { val r = repository.deletePickup(event); if (r == OperationResult.Success) "Retirada excluída. A próxima previsão foi recalculada." else r.message() }
    fun archive(archived: Boolean) = execute { val r = repository.archiveTracking(id, archived); if (r == OperationResult.Success) { if (archived) "Acompanhamento arquivado." else "Acompanhamento reativado." } else r.message() }
    fun delete() = execute {
        when (val result = repository.deleteMedication(id)) {
            OperationResult.Success -> { _uiState.update { it.copy(isDeleted = true) }; "Medicamento excluído." }
            OperationResult.Inactive -> "Este medicamento ainda está vinculado a receitas. Remova-o dessas receitas ou exclua as receitas antes de excluir o medicamento."
            else -> result.message()
        }
    }
}
