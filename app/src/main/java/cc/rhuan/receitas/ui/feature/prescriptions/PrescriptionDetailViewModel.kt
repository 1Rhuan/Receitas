package cc.rhuan.receitas.ui.feature.prescriptions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.usecase.CheckRefillStatusUseCase
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
    savedStateHandle: SavedStateHandle,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val checkRefillStatusUseCase: CheckRefillStatusUseCase = CheckRefillStatusUseCase(),
) : ViewModel() {

    private val prescriptionId: String = savedStateHandle.get<String>("prescriptionId") ?: ""

    private val _uiState = MutableStateFlow(PrescriptionDetailUiState(isLoading = true))
    val uiState: StateFlow<PrescriptionDetailUiState> = _uiState.asStateFlow()

    init {
        if (prescriptionId.isNotBlank()) {
            loadDetail()
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "ID da receita inválido") }
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
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
                                medication = med,
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
                        errorMessage = "Receita não encontrada",
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun renewPrescription() {
        viewModelScope.launch {
            val prescription = _uiState.value.prescription ?: return@launch
            prescriptionRepository.renewPrescription(prescription.id, prescription.title, null)
            _uiState.update { it.copy(actionMessage = "Solicitação de renovação de receita enviada!") }
        }
    }

    fun replenishMedication(medicationId: String) {
        viewModelScope.launch {
            medicationRepository.replenishStock(medicationId, null, null)
            _uiState.update { it.copy(actionMessage = "Estoque do medicamento atualizado!") }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
