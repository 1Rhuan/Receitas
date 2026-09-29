package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.DoctorRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.PrescriptionType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrescriptionWizardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val doctorRepository: DoctorRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val prescriptionId: String? = savedStateHandle.get<String>("prescriptionId")
    private val entryModeParam: String? = savedStateHandle.get<String>("entryMode")

    private val _uiState = MutableStateFlow(
        PrescriptionWizardUiState(
            entryMode = if (entryModeParam == "STANDALONE_MEDICATION") WizardEntryMode.STANDALONE_MEDICATION else WizardEntryMode.FULL_PRESCRIPTION,
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
        if (!prescriptionId.isNullOrBlank()) {
            loadPrescriptionForEditing(prescriptionId)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            medicationRepository.getMedications().collect { meds ->
                _uiState.update { it.copy(availableMedications = meds) }
            }
        }
        viewModelScope.launch {
            doctorRepository.getDoctors().collect { doctors ->
                _uiState.update { it.copy(availableDoctors = doctors) }
            }
        }
    }

    private fun loadPrescriptionForEditing(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val prescription = prescriptionRepository.getPrescriptionById(id)
            if (prescription != null) {
                val doctor = prescription.doctorId?.let { doctorRepository.getDoctorById(it) }

                val linkedDrafts = mutableListOf<MedicationDraft>()
                for (medId in prescription.medicationIds) {
                    val medItem = medicationRepository.getMedicationById(medId)
                    if (medItem != null) {
                        linkedDrafts.add(
                            MedicationDraft(
                                id = medItem.id,
                                title = medItem.title,
                                form = medItem.form,
                                dosageOverride = medItem.dosage,
                                prescribedPosology = medItem.prescribedPosology,
                                source = medItem.source,
                                sourceName = medItem.sourceName ?: "",
                                lastPickupDate = medItem.lastRefillDate,
                                pickupIntervalDays = medItem.refillCycleDays,
                                currentStock = medItem.stockQuantity,
                                minStockThreshold = medItem.minStockThreshold,
                                availabilityNote = medItem.instructions ?: "",
                            ),
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        editingPrescriptionId = prescription.id,
                        prescriptionTitle = prescription.title,
                        isTitleCustomized = true,
                        issueDate = prescription.issueDate,
                        type = prescription.type,
                        validityDays = prescription.customValidityDurationDays,
                        notes = prescription.notes ?: "",
                        selectedDoctorId = doctor?.id ?: prescription.doctorId,
                        doctorName = doctor?.name ?: prescription.doctorName,
                        doctorRegistration = doctor?.registration ?: prescription.doctorRegistration ?: "",
                        doctorSpecialty = doctor?.specialty ?: prescription.specialty,
                        healthFacility = doctor?.healthFacility ?: "",
                        doctorPhone = doctor?.phone ?: "",
                        medications = linkedDrafts,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Receita não encontrada") }
            }
        }
    }

    // Navigation
    fun goToStep(step: Int) {
        if (step in 1.._uiState.value.maxSteps) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    fun nextStep() {
        val currentState = _uiState.value

        // Auto-generate prescription title if not customized
        if (!currentState.isTitleCustomized && currentState.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
            val firstMedName = when {
                currentState.medications.isNotEmpty() -> currentState.medications.first().title
                currentState.currentMedicationDraft.title.isNotBlank() -> currentState.currentMedicationDraft.title
                else -> ""
            }
            if (firstMedName.isNotBlank()) {
                val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                _uiState.update { it.copy(prescriptionTitle = "$firstMedName - $dateStr") }
            }
        }

        if (currentState.currentStep < currentState.maxSteps) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun skipStep() {
        nextStep()
    }

    // Draft Management
    fun updateCurrentMedicationDraft(
        title: String? = null,
        activeIngredient: String? = null,
        form: PharmaceuticalForm? = null,
        dosageValue: String? = null,
        dosageUnit: String? = null,
        dosageOverride: String? = null,
        prescribedPosology: String? = null,
        doseAmount: String? = null,
        frequencyOption: String? = null,
        durationOption: String? = null,
        source: MedicationSource? = null,
        sourceName: String? = null,
        hasRecurringPickup: Boolean? = null,
        lastPickupDate: LocalDate? = null,
        pickupIntervalDays: Int? = null,
        currentStock: Int? = null,
        stockAlertEnabled: Boolean? = null,
        minStockThreshold: Int? = null,
        availabilityNote: String? = null,
    ) {
        _uiState.update { state ->
            val draft = state.currentMedicationDraft.copy(
                title = title ?: state.currentMedicationDraft.title,
                activeIngredient = activeIngredient ?: state.currentMedicationDraft.activeIngredient,
                form = form ?: state.currentMedicationDraft.form,
                dosageValue = dosageValue ?: state.currentMedicationDraft.dosageValue,
                dosageUnit = dosageUnit ?: state.currentMedicationDraft.dosageUnit,
                dosageOverride = dosageOverride ?: state.currentMedicationDraft.dosageOverride,
                prescribedPosology = prescribedPosology ?: state.currentMedicationDraft.prescribedPosology,
                doseAmount = doseAmount ?: state.currentMedicationDraft.doseAmount,
                frequencyOption = frequencyOption ?: state.currentMedicationDraft.frequencyOption,
                durationOption = durationOption ?: state.currentMedicationDraft.durationOption,
                source = source ?: state.currentMedicationDraft.source,
                sourceName = sourceName ?: state.currentMedicationDraft.sourceName,
                hasRecurringPickup = hasRecurringPickup ?: state.currentMedicationDraft.hasRecurringPickup,
                lastPickupDate = lastPickupDate ?: state.currentMedicationDraft.lastPickupDate,
                pickupIntervalDays = pickupIntervalDays ?: state.currentMedicationDraft.pickupIntervalDays,
                currentStock = currentStock ?: state.currentMedicationDraft.currentStock,
                stockAlertEnabled = stockAlertEnabled ?: state.currentMedicationDraft.stockAlertEnabled,
                minStockThreshold = minStockThreshold ?: state.currentMedicationDraft.minStockThreshold,
                availabilityNote = availabilityNote ?: state.currentMedicationDraft.availabilityNote,
            )
            state.copy(currentMedicationDraft = draft)
        }
    }

    fun selectExistingMedication(medicationItem: MedicationItem) {
        _uiState.update { state ->
            val draft = state.currentMedicationDraft.copy(
                id = medicationItem.id,
                title = medicationItem.title,
                activeIngredient = medicationItem.activeIngredient ?: "",
                dosageOverride = medicationItem.dosage,
                form = medicationItem.form,
                prescribedPosology = medicationItem.prescribedPosology,
            )
            state.copy(currentMedicationDraft = draft)
        }
    }

    fun addCurrentDraftAndRegisterAnother() {
        val currentDraft = _uiState.value.currentMedicationDraft
        if (currentDraft.isStep1Complete) {
            _uiState.update { state ->
                state.copy(
                    medications = state.medications + currentDraft,
                    currentMedicationDraft = MedicationDraft(),
                    currentStep = 1,
                )
            }
        }
    }

    fun removeMedication(index: Int) {
        _uiState.update { state ->
            val list = state.medications.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            state.copy(medications = list)
        }
    }

    fun updatePrescriptionTitle(title: String) {
        _uiState.update { it.copy(prescriptionTitle = title, isTitleCustomized = true) }
    }

    fun updateIssueDate(date: LocalDate) {
        _uiState.update { it.copy(issueDate = date) }
    }

    fun updatePrescriptionType(type: PrescriptionType) {
        _uiState.update {
            it.copy(
                isStep4Skipped = false,
                type = type,
                validityDays = type.defaultValidityDays,
            )
        }
    }

    fun updateValidityDays(days: Int) {
        _uiState.update { it.copy(validityDays = days) }
    }

    fun updateRenewalReminderEnabled(enabled: Boolean) {
        _uiState.update { it.copy(renewalReminderEnabled = enabled) }
    }

    fun updateRenewalReminderDaysBefore(days: Int) {
        _uiState.update { it.copy(renewalReminderDaysBefore = days) }
    }

    fun selectDoctor(doctor: Doctor) {
        _uiState.update {
            it.copy(
                selectedDoctorId = doctor.id,
                doctorName = doctor.name,
                doctorRegistration = doctor.registration ?: "",
                doctorSpecialty = doctor.specialty ?: "",
                healthFacility = doctor.healthFacility ?: "",
                doctorPhone = doctor.phone ?: "",
            )
        }
    }

    fun toggleExtraDoctorFields() {
        _uiState.update { it.copy(showExtraDoctorFields = !it.showExtraDoctorFields) }
    }

    fun updateDoctorName(name: String) {
        _uiState.update { it.copy(doctorName = name) }
    }

    fun updateDoctorRegistration(reg: String) {
        _uiState.update { it.copy(doctorRegistration = reg) }
    }

    fun updateDoctorSpecialty(specialty: String) {
        _uiState.update { it.copy(doctorSpecialty = specialty) }
    }

    fun updateHealthFacility(facility: String) {
        _uiState.update { it.copy(healthFacility = facility) }
    }

    fun updateDoctorPhone(phone: String) {
        _uiState.update { it.copy(doctorPhone = phone) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updatePhotoUri(uri: String?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    // Save
    fun savePrescription() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value

                // 1. Doctor (if full prescription & doctor provided)
                var doctorId = state.selectedDoctorId
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && doctorId == null && state.doctorName.isNotBlank()) {
                    val doctors = doctorRepository.getDoctors().first()
                    val existingDoctor = doctors.find {
                        (state.doctorRegistration.isNotBlank() && it.registration == state.doctorRegistration) ||
                            it.name.equals(state.doctorName, ignoreCase = true)
                    }
                    if (existingDoctor != null) {
                        doctorId = existingDoctor.id
                    } else {
                        val newDoctor = Doctor(
                            id = UUID.randomUUID().toString(),
                            name = state.doctorName,
                            registration = state.doctorRegistration.takeIf { it.isNotBlank() },
                            specialty = state.doctorSpecialty.takeIf { it.isNotBlank() },
                            healthFacility = state.healthFacility.takeIf { it.isNotBlank() },
                            phone = state.doctorPhone.takeIf { it.isNotBlank() },
                        )
                        doctorRepository.addDoctor(newDoctor)
                        doctorId = newDoctor.id
                    }
                }

                // 2. Medications
                val savedMedicationIds = mutableListOf<String>()
                val allMedDrafts = if (state.currentMedicationDraft.isStep1Complete && !state.medications.contains(state.currentMedicationDraft)) {
                    state.medications + state.currentMedicationDraft
                } else state.medications

                for (draft in allMedDrafts) {
                    val medId = draft.id ?: UUID.randomUUID().toString()
                    savedMedicationIds.add(medId)

                    val medItem = MedicationItem(
                        id = medId,
                        title = draft.title,
                        activeIngredient = draft.activeIngredient.takeIf { it.isNotBlank() },
                        dosage = draft.dosage,
                        quantity = "${draft.currentStock ?: 0} un",
                        prescribedPosology = draft.prescribedPosology.ifBlank { draft.frequencyOption },
                        category = draft.source.label,
                        source = draft.source,
                        sourceName = draft.sourceName.takeIf { it.isNotBlank() },
                        isAvailable = true,
                        instructions = draft.availabilityNote.takeIf { it.isNotBlank() },
                        form = draft.form,
                        stockQuantity = draft.currentStock ?: 0,
                        minStockThreshold = draft.minStockThreshold,
                        lastRefillDate = draft.lastPickupDate,
                        refillCycleDays = draft.pickupIntervalDays ?: 30,
                    )

                    if (draft.id == null) {
                        medicationRepository.addMedication(medItem)
                    } else {
                        medicationRepository.updateMedication(medItem)
                    }
                }

                // 3. Prescription (only if full prescription mode)
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) {
                    val prescriptionItem = PrescriptionItem(
                        id = state.editingPrescriptionId ?: UUID.randomUUID().toString(),
                        title = state.prescriptionTitle.ifBlank { "Receita Médica" },
                        specialty = state.doctorSpecialty,
                        doctorId = doctorId,
                        doctorName = state.doctorName,
                        doctorRegistration = state.doctorRegistration.takeIf { it.isNotBlank() },
                        issueDate = state.issueDate,
                        type = if (state.isStep4Skipped) null else state.type,
                        customValidityDays = if (state.isStep4Skipped) null else state.validityDays,
                        notes = state.notes.takeIf { it.isNotBlank() },
                        medicationIds = savedMedicationIds,
                    )

                    if (state.editingPrescriptionId == null) {
                        prescriptionRepository.addPrescription(prescriptionItem)
                    } else {
                        prescriptionRepository.updatePrescription(prescriptionItem)
                    }
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Erro ao salvar") }
            }
        }
    }
}

private val PrescriptionItem.customValidityDurationDays: Int?
    get() = customValidityDays
