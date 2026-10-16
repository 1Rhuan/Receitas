package cc.rhuan.receitas.ui.feature.medications

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val texts: TextResolver,handle: SavedStateHandle, private val repository: MedicationRepository,
    private val prescriptionRepository: PrescriptionRepository) : ViewModel() {
    private val id: String = handle["medicationId"] ?: ""
    private val _uiState = MutableStateFlow(MedicationDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private var loadJob: Job? = null
    init { loadMedication() }
    fun loadMedication() {
        loadJob?.cancel()
        if (id.isBlank()) { _uiState.update { it.copy(isLoading = false, errorMessage = texts.get(R.string.medication_not_found_feedback)) }; return }
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            combine(repository.getMedications(), prescriptionRepository.getPrescriptions()) { medications, prescriptions ->
                val med = medications.find { it.id == id }
                Triple(med, prescriptions.filter { id in it.medicationIds || it.items.any { item -> item.medicationId == id } },
                    prescriptions.find { it.id == med?.linkedPrescriptionId })
            }.catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = texts.get(R.string.medication_load_error)) }
            }.collect { (med, prescriptions, pickupPrescription) ->
                _uiState.update { it.copy(medication = med, prescriptions = prescriptions,
                    pickupPrescription = pickupPrescription, isLoading = false,
                    errorMessage = if (med == null) texts.get(R.string.medication_not_found_feedback) else null) }
            }
        }
    }
    private fun execute(action: suspend () -> String) {
        viewModelScope.launch {
            try { _message.value = action() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _message.value = texts.get(R.string.action_retry_error) }
        }
    }
    fun clearMessage() { _message.value = null }
    fun pickup(date: LocalDate, early: Boolean, cycle: LocalDate?) = execute { texts.resolve(repository.registerPickup(id, date, early, cycle).message()) }
    fun correct(event: String, date: LocalDate) = execute { val r = repository.correctPickup(event, date); if (r == OperationResult.Success) texts.get(R.string.pickup_date_corrected) else texts.resolve(r.message()) }
    fun deleteEvent(event: String) = execute { val r = repository.deletePickup(event); if (r == OperationResult.Success) texts.get(R.string.pickup_deleted_feedback) else texts.resolve(r.message()) }
    fun archive(archived: Boolean) = execute { val r = repository.archiveTracking(id, archived); if (r == OperationResult.Success) { if (archived) texts.get(R.string.tracking_archived_feedback) else texts.get(R.string.tracking_reactivated_feedback) } else texts.resolve(r.message()) }
    fun delete() = execute {
        when (val result = repository.deleteMedication(id)) {
            OperationResult.Success -> { _uiState.update { it.copy(isDeleted = true) }; texts.get(R.string.medication_deleted_feedback) }
            OperationResult.Inactive -> texts.get(R.string.medication_linked_delete_error)
            else -> texts.resolve(result.message())
        }
    }
}
