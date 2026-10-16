package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.domain.model.*
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val texts: TextResolver,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState())
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadMedications()
    }

    fun loadMedications() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            medicationRepository.getMedications().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = texts.get(R.string.medications_load_error)) }
            }.collect { meds ->
                _uiState.update { state ->
                    val filtered = applyFilter(meds, state.searchQuery, state.selectedSourceFilter)
                    state.copy(
                        medications = meds,
                        filteredMedications = filtered,
                        errorMessage = null,
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

    fun onSourceFilterSelected(sourceCode: String) {
        _uiState.update { state ->
            val filtered = applyFilter(state.medications, state.searchQuery, sourceCode)
            state.copy(selectedSourceFilter = sourceCode, filteredMedications = filtered)
        }
    }

    fun registerPickup(medicationId: String, date: LocalDate, early: Boolean, cycle: LocalDate?) {
        viewModelScope.launch {
            try { val result = medicationRepository.registerPickup(medicationId, date, early, cycle)
                _uiState.update { it.copy(snackbarMessage = texts.resolve(result.message())) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(snackbarMessage = texts.get(R.string.pickup_record_error)) } }
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
                "ALL" -> true
                else -> med.source.name.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesSource
        }
    }
}
