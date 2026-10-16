package cc.rhuan.receitas.ui.feature.prescriptions

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.usecase.CheckRefillStatusUseCase
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrescriptionDetailViewModel @Inject constructor(
    private val texts: TextResolver,
    savedStateHandle: SavedStateHandle,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val checkRefillStatusUseCase: CheckRefillStatusUseCase = CheckRefillStatusUseCase(),
) : ViewModel() {

    private val prescriptionId: String = savedStateHandle.get<String>("prescriptionId") ?: ""

    private val _uiState = MutableStateFlow(PrescriptionDetailUiState(isLoading = true))
    val uiState: StateFlow<PrescriptionDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        if (prescriptionId.isNotBlank()) {
            loadDetail()
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = texts.get(R.string.prescription_not_found_retry)) }
        }
    }

    fun loadDetail() {
        if (prescriptionId.isBlank()) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            combine(
                prescriptionRepository.getPrescriptions(),
                medicationRepository.getMedications(),
            ) { prescriptions, medications ->
                val prescription = prescriptions.find { it.id == prescriptionId }
                if (prescription != null) {
                    val linkedMeds = medications
                        .filter { prescription.medicationIds.contains(it.id) }
                        .map { med ->
                            PrescriptionMedicationDetail(
                                medication = med.copy(title = prescription.items.find { it.medicationId == med.id }?.title ?: med.title, dosage = prescription.items.find { it.medicationId == med.id }?.dosage ?: med.dosage, form = prescription.items.find { it.medicationId == med.id }?.form ?: med.form, prescribedPosology = prescription.items.find { it.medicationId == med.id }?.posology.orEmpty(), prescribedQuantity = prescription.items.find { it.medicationId == med.id }?.quantity.orEmpty()),
                                refillStatus = checkRefillStatusUseCase(med, prescriptions),
                            )
                        }
                    PrescriptionDetailUiState(
                        prescription = prescription,
                        medications = linkedMeds,
                        isLoading = false,
                    )
                } else {
                    PrescriptionDetailUiState(
                        isLoading = false,
                        errorMessage = texts.get(R.string.prescription_not_found),
                    )
                }
            }.catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, errorMessage = texts.get(R.string.prescription_load_error)) }
            }.collect { newState ->
                _uiState.update { newState.copy(actionMessage = it.actionMessage, isDeleted = it.isDeleted) }
            }
        }
    }

    private fun execute(action: suspend () -> String) {
        viewModelScope.launch {
            try { val message = action(); _uiState.update { it.copy(actionMessage = message) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(actionMessage = texts.get(R.string.action_retry_error)) } }
        }
    }
    fun registerPickup(id: String, date: LocalDate, early: Boolean, cycle: LocalDate?) = execute {
        texts.resolve(medicationRepository.registerPickup(id, date, early, cycle).message())
    }
    fun correctPickup(id: String, date: LocalDate) = execute {
        val result = medicationRepository.correctPickup(id, date)
        if (result == OperationResult.Success) texts.get(R.string.pickup_date_corrected) else texts.resolve(result.message())
    }
    fun deletePickup(id: String) = execute {
        val result = medicationRepository.deletePickup(id)
        if (result == OperationResult.Success) texts.get(R.string.pickup_deleted_feedback) else texts.resolve(result.message())
    }
    fun archive(archived: Boolean) = execute {
        prescriptionRepository.archivePrescription(prescriptionId, archived)
        if (archived) texts.get(R.string.prescription_archived_feedback) else texts.get(R.string.prescription_reactivated_feedback)
    }
    fun deleteRecipe() = execute {
        prescriptionRepository.deletePrescription(prescriptionId)
        _uiState.update { it.copy(isDeleted = true) }
        texts.get(R.string.prescription_deleted_feedback)
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
