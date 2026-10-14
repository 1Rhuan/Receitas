package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.DoctorRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRegistrationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.Doctor
import cc.rhuan.receitas.domain.model.MedicationItem
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionItem
import cc.rhuan.receitas.domain.model.PrescriptionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrescriptionWizardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val doctorRepository: DoctorRepository,
    private val registrationRepository: PrescriptionRegistrationRepository,
) : ViewModel() {

    private val prescriptionId: String? = savedStateHandle.get<String>("prescriptionId")
    private val entryModeParam: String? = savedStateHandle.get<String>("entryMode")

    private val restoredState = savedStateHandle.get<PrescriptionWizardUiState>("registration_draft")
    private val _uiState = MutableStateFlow(
        restoredState?.copy(isLoading = false) ?: PrescriptionWizardUiState(
            editingPrescriptionId = prescriptionId,
            isLoading = !prescriptionId.isNullOrBlank(),
            entryMode = if (entryModeParam == "STANDALONE_MEDICATION") WizardEntryMode.STANDALONE_MEDICATION else WizardEntryMode.FULL_PRESCRIPTION,
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
        viewModelScope.launch {
            _uiState.collect { state ->
                if (!state.isLoading) persistDraft(state)
            }
        }
        if ((restoredState == null || restoredState.initialLoadFailed) && !prescriptionId.isNullOrBlank() && restoredState?.isSaved != true) {
            loadPrescriptionForEditing(prescriptionId)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            medicationRepository.getMedications().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = "Não foi possível carregar os medicamentos cadastrados.") }
            }.collect { meds ->
                _uiState.update { it.copy(availableMedications = meds) }
            }
        }
        viewModelScope.launch {
            doctorRepository.getDoctors().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = "Não foi possível carregar os profissionais cadastrados.") }
            }.collect { doctors ->
                _uiState.update { it.copy(availableDoctors = doctors) }
            }
        }
    }

    private fun loadPrescriptionForEditing(id: String) {
        viewModelScope.launch {
            try {
            _uiState.update { it.copy(isLoading = true, initialLoadFailed = false) }
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
                                activeIngredient = medItem.activeIngredient ?: "",
                                form = medItem.form,
                                dosageOverride = medItem.dosage,
                                prescribedPosology = medItem.prescribedPosology,
                                source = medItem.source,
                                sourceName = medItem.sourceName ?: "",
                                hasRecurringPickup = medItem.hasRecurringPickup,
                                lastPickupDate = medItem.lastRefillDate,
                                pickupIntervalInput = medItem.refillCycleDays.toString(),
                                pickupReminderDaysBefore = medItem.pickupReminderDaysBefore,
                                pickupIntervalDays = medItem.refillCycleDays,
                                replenishmentQuantity = medItem.quantity,
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
                        registrationId = prescription.id,
                        showMedicationEditor = false,
                        prescriptionTitle = prescription.title,
                        isTitleCustomized = true,
                        issueDate = prescription.issueDate,
                        type = prescription.type,
                        validityDays = prescription.validityDurationDays,
                        validityDaysInput = prescription.validityDurationDays.toString(),
                        renewalReminderEnabled = prescription.renewalReminderEnabled,
                        renewalReminderDaysBefore = prescription.renewalReminderDaysBefore,
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
                _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = "Receita não encontrada") }
            }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = "Não foi possível carregar a receita. Tente novamente.") } }
        }
    }

    private fun persistDraft(state: PrescriptionWizardUiState) {
        savedStateHandle["registration_draft"] = state.copy(isLoading = false, availableDoctors = emptyList(), availableMedications = emptyList())
    }

    fun retryInitialLoad() { prescriptionId?.let(::loadPrescriptionForEditing) }

    // Navigation keeps a review return point instead of forcing the user through every step again.
    fun goToStep(step: Int) {
        if (_uiState.value.isLoading || step !in 1.._uiState.value.maxSteps) return
        if (step == _uiState.value.pickupStep && _uiState.value.showMedicationEditor && !commitMedicationDraft()) return
        _uiState.update { it.copy(currentStep = step, reviewEditStep = if (it.currentStep == it.maxSteps) step else it.reviewEditStep, errorMessage = null) }
    }
    fun nextStep() {
        val state = _uiState.value
        if (state.isLoading) return
        state.currentStepError?.let { error -> _uiState.update { it.copy(errorMessage = error) }; return }
        if (state.currentStep == state.medicationStep && state.showMedicationEditor) commitMedicationDraft()
        _uiState.update {
            val target = if (it.reviewEditStep != null) it.maxSteps else (it.currentStep + 1).coerceAtMost(it.maxSteps)
            it.copy(currentStep = target, reviewEditStep = null, errorMessage = null)
        }
    }
    fun previousStep() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(currentStep = if (it.reviewEditStep != null) it.maxSteps else (it.currentStep - 1).coerceAtLeast(1), reviewEditStep = null, errorMessage = null) }
    }
    fun skipStep() = nextStep()
    private fun change(transform: (PrescriptionWizardUiState) -> PrescriptionWizardUiState) {
        if (_uiState.value.isLoading || _uiState.value.isSaved) return
        _uiState.update { transform(it).copy(hasUnsavedChanges = true, errorMessage = null) }
    }
    fun updateForm(transform: (PrescriptionWizardUiState) -> PrescriptionWizardUiState) = change(transform)
    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }
    fun commitMedicationDraft(): Boolean {
        val state = _uiState.value
        if (!state.showMedicationEditor) return true
        val draft = state.currentMedicationDraft
        if (draft == MedicationDraft() && state.medications.isNotEmpty()) {
            change { it.copy(showMedicationEditor = false) }
            return true
        }
        if (!draft.isComplete) { _uiState.update { it.copy(errorMessage = "Informe o nome do medicamento.") }; return false }
        val index = state.editingMedicationIndex
        if (state.medications.withIndex().any { it.index != index && draft.id != null && it.value.id == draft.id }) {
            _uiState.update { it.copy(errorMessage = "Esse medicamento já foi adicionado. Edite o item existente.") }
            return false
        }
        val completed = draft.copy(id = draft.id ?: UUID.randomUUID().toString(), title = draft.title.trim())
        change {
            val list = it.medications.toMutableList()
            if (index != null && index in list.indices) list[index] = completed else list.add(completed)
            it.copy(medications = list, currentMedicationDraft = MedicationDraft(), showMedicationEditor = false, editingMedicationIndex = null)
        }
        return true
    }
    fun startNewMedication() {
        if (_uiState.value.showMedicationEditor && !commitMedicationDraft()) return
        change { it.copy(showMedicationEditor = true, currentMedicationDraft = MedicationDraft(), editingMedicationIndex = null) }
    }
    fun editMedication(index: Int) {
        val state = _uiState.value
        if (index !in state.allMedications.indices) return
        val selected = state.allMedications[index]
        if (state.showMedicationEditor && state.editingMedicationIndex == index) {
            _uiState.update { it.copy(currentStep = it.medicationStep, reviewEditStep = if (state.currentStep == state.maxSteps) it.medicationStep else it.reviewEditStep, errorMessage = null) }
            return
        }
        if (state.showMedicationEditor && !commitMedicationDraft()) return
        change { it.copy(currentMedicationDraft = selected, editingMedicationIndex = index, showMedicationEditor = true, currentStep = it.medicationStep, reviewEditStep = if (state.currentStep == state.maxSteps) it.medicationStep else it.reviewEditStep) }
    }
    fun cancelMedicationEditor() {
        change { it.copy(currentMedicationDraft = MedicationDraft(), editingMedicationIndex = null, showMedicationEditor = it.medications.isEmpty()) }
    }
    fun updateMedicationPickup(index: Int, transform: (MedicationDraft) -> MedicationDraft) {
        change { state -> state.copy(medications = state.medications.mapIndexed { i, draft -> if (i == index) transform(draft) else draft }) }
    }
    fun updateValidityInput(value: String) { change { it.copy(validityDaysInput = value, validityDays = value.toIntOrNull()) } }

    // Draft Management
    fun updateCurrentMedicationDraft(
        title: String? = null,
        activeIngredient: String? = null,
        form: PharmaceuticalForm? = null,
        dosageValue: String? = null,
        dosageUnit: String? = null,
        dosageOverride: String? = null,
        prescribedPosology: String? = null,
        doseQuantity: String? = null,
        doseUnit: String? = null,
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
        change { state ->
            val newDoseQuantity = doseQuantity ?: state.currentMedicationDraft.doseQuantity
            val newDoseUnit = doseUnit ?: state.currentMedicationDraft.doseUnit
            val computedDoseAmount = doseAmount ?: "$newDoseQuantity $newDoseUnit".trim()

            val draft = state.currentMedicationDraft.copy(
                title = title ?: state.currentMedicationDraft.title,
                activeIngredient = activeIngredient ?: state.currentMedicationDraft.activeIngredient,
                form = form ?: state.currentMedicationDraft.form,
                dosageValue = dosageValue ?: state.currentMedicationDraft.dosageValue,
                dosageUnit = dosageUnit ?: state.currentMedicationDraft.dosageUnit,
                dosageOverride = dosageOverride ?: state.currentMedicationDraft.dosageOverride,
                prescribedPosology = prescribedPosology ?: state.currentMedicationDraft.prescribedPosology,
                doseQuantity = newDoseQuantity,
                doseUnit = newDoseUnit,
                doseAmount = computedDoseAmount,
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
        change { state ->
            val draft = state.currentMedicationDraft.copy(
                id = medicationItem.id,
                title = medicationItem.title,
                activeIngredient = medicationItem.activeIngredient ?: "",
                dosageOverride = medicationItem.dosage,
                form = medicationItem.form,
                prescribedPosology = medicationItem.prescribedPosology,
                source = medicationItem.source,
                sourceName = medicationItem.sourceName ?: "",
                hasRecurringPickup = medicationItem.hasRecurringPickup,
                lastPickupDate = medicationItem.lastRefillDate,
                pickupIntervalDays = medicationItem.refillCycleDays,
                pickupIntervalInput = medicationItem.refillCycleDays.toString(),
                pickupReminderDaysBefore = medicationItem.pickupReminderDaysBefore,
                replenishmentQuantity = medicationItem.quantity,
                currentStock = medicationItem.stockQuantity,
                minStockThreshold = medicationItem.minStockThreshold,
                availabilityNote = medicationItem.instructions ?: "",
            )
            state.copy(currentMedicationDraft = draft)
        }
    }

    fun addCurrentDraftAndRegisterAnother() = startNewMedication()
    fun removeMedication(index: Int) {
        change { state -> state.copy(medications = state.medications.filterIndexed { i, _ -> i != index }, currentMedicationDraft = MedicationDraft(), editingMedicationIndex = null, showMedicationEditor = state.medications.size <= 1) }
    }

    fun updatePrescriptionTitle(title: String) {
        change { it.copy(prescriptionTitle = title, isTitleCustomized = true) }
    }

    fun updateIssueDate(date: LocalDate) {
        change { it.copy(issueDate = date) }
    }

    fun updatePrescriptionType(type: PrescriptionType) {
        change {
            it.copy(
                isStep4Skipped = false,
                type = type,
                validityDays = type.defaultValidityDays,
                validityDaysInput = type.defaultValidityDays.toString(),
                renewalReminderDaysBefore = it.renewalReminderDaysBefore.coerceAtMost(type.defaultValidityDays),
            )
        }
    }

    fun updateValidityDays(days: Int) {
        change { it.copy(validityDays = days, validityDaysInput = days.toString()) }
    }

    fun updateRenewalReminderEnabled(enabled: Boolean) {
        change { it.copy(renewalReminderEnabled = enabled) }
    }

    fun updateRenewalReminderDaysBefore(days: Int) {
        change { it.copy(renewalReminderDaysBefore = days) }
    }

    fun selectDoctor(doctor: Doctor) {
        change {
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
        change { it.copy(showExtraDoctorFields = !it.showExtraDoctorFields) }
    }

    fun updateDoctorName(name: String) {
        change { it.copy(doctorName = name, selectedDoctorId = null) }
    }

    fun updateDoctorRegistration(reg: String) {
        change { it.copy(doctorRegistration = reg, selectedDoctorId = null) }
    }

    fun updateDoctorSpecialty(specialty: String) {
        change { it.copy(doctorSpecialty = specialty) }
    }

    fun updateHealthFacility(facility: String) {
        change { it.copy(healthFacility = facility) }
    }

    fun updateDoctorPhone(phone: String) {
        change { it.copy(doctorPhone = phone) }
    }

    fun updateNotes(notes: String) {
        change { it.copy(notes = notes) }
    }

    fun updatePhotoUri(uri: String?) {
        change { it.copy(photoUri = uri) }
    }

    fun savePrescription() {
        val state = _uiState.value
        if (state.isLoading || state.isSaved) return
        val validation = state.recipeError ?: state.medicationError ?: state.pickupError
        if (validation != null) { _uiState.update { it.copy(errorMessage = validation) }; return }
        val drafts = state.allMedications.map { it.copy(id = it.id ?: UUID.randomUUID().toString()) }
        // Keep IDs before the first write so retrying after failure never creates duplicate records.
        _uiState.update { it.copy(medications = drafts, currentMedicationDraft = MedicationDraft(), showMedicationEditor = false, editingMedicationIndex = null, isLoading = true, errorMessage = null) }
        persistDraft(_uiState.value)
        viewModelScope.launch {
            try {
                var doctorId = state.selectedDoctorId
                var newDoctor: Doctor? = null
                if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION && state.doctorName.isNotBlank()) {
                    val existing = doctorId?.let { doctorRepository.getDoctorById(it) } ?: doctorRepository.getDoctors().first().find { doctor ->
                        if (state.doctorRegistration.isNotBlank()) doctor.registration == state.doctorRegistration.trim()
                        else doctor.name.equals(state.doctorName.trim(), ignoreCase = true)
                    }
                    doctorId = existing?.id ?: UUID.randomUUID().toString()
                    newDoctor = Doctor(doctorId, state.doctorName.trim(), state.doctorRegistration.takeIf { it.isNotBlank() }, state.doctorSpecialty.takeIf { it.isNotBlank() }, state.healthFacility.takeIf { it.isNotBlank() }, state.doctorPhone.takeIf { it.isNotBlank() })
                }
                val medications = drafts.map { draft ->
                    MedicationItem(
                        id = requireNotNull(draft.id), title = draft.title.trim(), activeIngredient = draft.activeIngredient.takeIf { it.isNotBlank() },
                        dosage = draft.dosage, form = draft.form, prescribedPosology = draft.prescribedPosology,
                        quantity = draft.replenishmentQuantity.trim(), source = draft.source,
                        sourceName = draft.sourceName.takeIf { it.isNotBlank() }, stockQuantity = draft.currentStock ?: 0,
                        minStockThreshold = draft.minStockThreshold, instructions = draft.availabilityNote.takeIf { it.isNotBlank() },
                        lastRefillDate = if (draft.hasRecurringPickup) draft.lastPickupDate else null,
                        refillCycleDays = draft.pickupIntervalDays ?: 30, hasRecurringPickup = draft.hasRecurringPickup,
                        pickupReminderDaysBefore = draft.pickupReminderDaysBefore,
                    )
                }
                val recipe = if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) PrescriptionItem(
                    id = state.editingPrescriptionId ?: state.registrationId,
                    title = state.prescriptionTitle.trim().ifBlank { drafts.first().title + " — " + state.issueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) },
                    specialty = state.doctorSpecialty, doctorId = doctorId, doctorName = state.doctorName,
                    doctorRegistration = state.doctorRegistration.takeIf { it.isNotBlank() }, issueDate = state.issueDate,
                    type = state.type, customValidityDays = state.validityDays, notes = state.notes.takeIf { it.isNotBlank() },
                    medicationIds = medications.map { it.id }, renewalReminderEnabled = state.renewalReminderEnabled,
                    renewalReminderDaysBefore = state.renewalReminderDaysBefore,
                ) else null
                registrationRepository.save(recipe, medications, newDoctor)
                _uiState.update { it.copy(isLoading = false, isSaved = true, savedPrescriptionId = recipe?.id, prescriptionTitle = recipe?.title ?: it.prescriptionTitle, hasUnsavedChanges = false) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível salvar. Seus dados continuam aqui; tente novamente.") } }
        }
    }
}
