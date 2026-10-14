package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.DoctorRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRegistrationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.domain.model.*
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
    private val renewalOf: String? = savedStateHandle.get<String>("renewalOf")
    private val medicationId: String? = savedStateHandle.get<String>("medicationId")
    private val entryModeParam: String? = savedStateHandle.get<String>("entryMode")

    private val restoredState = savedStateHandle.get<PrescriptionWizardUiState>("registration_draft")
    private val _uiState = MutableStateFlow(
        restoredState?.copy(isLoading = false) ?: PrescriptionWizardUiState(
            editingPrescriptionId = prescriptionId,
            isLoading = !prescriptionId.isNullOrBlank() || !renewalOf.isNullOrBlank() || !medicationId.isNullOrBlank(),
            previousPrescriptionId = renewalOf,
            editingMedicationId = medicationId,
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
        if (restoredState == null && renewalOf != null) loadPrescriptionForEditing(renewalOf)
        if (restoredState == null && medicationId != null) loadMedicationForEditing(medicationId)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            prescriptionRepository.getPrescriptions().catch { if (it is CancellationException) throw it }.collect { recipes ->
                _uiState.update { it.copy(availablePrescriptions = recipes.filter { p -> p.isActive }) }
            }
        }
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
                require(prescription.state != RecordState.REPLACED) { "Receita substituída" }
                val doctor = prescription.doctorId?.let { doctorRepository.getDoctorById(it) }

                val linkedDrafts = mutableListOf<MedicationDraft>()
                for (medId in prescription.medicationIds) {
                    val medItem = medicationRepository.getMedicationById(medId)
                    if (medItem != null) {
                        linkedDrafts.add(
                            MedicationDraft(
                                id = medItem.id,
                                title = prescription.items.find { it.medicationId == medId }?.title?.takeIf { it.isNotBlank() } ?: medItem.title,
                                activeIngredient = medItem.activeIngredient ?: "",
                                form = prescription.items.find { it.medicationId == medId }?.form ?: medItem.form,
                                dosageOverride = prescription.items.find { it.medicationId == medId }?.dosage ?: medItem.dosage,
                                prescribedPosology = prescription.items.find { it.medicationId == medId }?.posology.orEmpty(),
                                prescribedQuantity = prescription.items.find { it.medicationId == medId }?.quantity.orEmpty(),
                                intervalUnit = medItem.intervalUnit, linkedPrescriptionId = medItem.linkedPrescriptionId,
                                trackingArchived = medItem.trackingArchived,
                                source = medItem.source,
                                sourceName = medItem.sourceName ?: "",
                                hasRecurringPickup = medItem.hasRecurringPickup,
                                lastPickupDate = medItem.lastRefillDate, hasPickupHistory = medItem.pickupHistory.isNotEmpty(),
                                pickupIntervalInput = medItem.refillCycleDays.toString(),
                                pickupReminderDaysBefore = medItem.pickupReminderDaysBefore,
                                pickupIntervalDays = medItem.refillCycleDays,
                                replenishmentQuantity = medItem.quantity,
                                availabilityNote = medItem.instructions ?: "",
                            ),
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        editingPrescriptionId = if (renewalOf != null) null else prescription.id,
                        registrationId = if (renewalOf != null) it.registrationId else prescription.id,
                        previousPrescriptionId = if (renewalOf != null) renewalOf else prescription.previousPrescriptionId,
                        showMedicationEditor = false,
                        prescriptionTitle = prescription.title,
                        isTitleCustomized = true,
                        issueDate = if (renewalOf != null) LocalDate.now() else prescription.issueDate,
                        validityOrigin = prescription.validityOrigin, recordState = if (renewalOf != null) RecordState.ACTIVE else prescription.state,
                        type = prescription.type,
                        validityDays = prescription.validityDurationDays,
                        validityDaysInput = prescription.validityDurationDays.toString(),
                        renewalReminderEnabled = prescription.renewalReminderEnabled,
                        renewalReminderDaysBefore = prescription.renewalReminderDaysBefore,
                        notes = prescription.notes ?: "",
                        selectedDoctorId = doctor?.id ?: prescription.doctorId,
                        doctorName = prescription.doctorName,
                        doctorRegistration = prescription.doctorRegistration ?: "",
                        doctorSpecialty = prescription.specialty,
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

    private suspend fun medicationDraft(m: MedicationItem) = MedicationDraft(id = m.id, title = m.title,
        activeIngredient = m.activeIngredient.orEmpty(), form = m.form, dosageOverride = m.dosage,
        source = m.source, sourceName = m.sourceName.orEmpty(), hasRecurringPickup = m.hasRecurringPickup,
        lastPickupDate = m.lastRefillDate, hasPickupHistory = m.pickupHistory.isNotEmpty(), pickupIntervalDays = m.refillCycleDays, pickupIntervalInput = m.refillCycleDays.toString(),
        intervalUnit = m.intervalUnit, pickupReminderDaysBefore = m.pickupReminderDaysBefore, replenishmentQuantity = m.quantity,
        linkedPrescriptionId = m.linkedPrescriptionId, trackingArchived = m.trackingArchived, availabilityNote = m.instructions.orEmpty())
    private fun loadMedicationForEditing(id: String) {
        viewModelScope.launch {
            try {
                val medication = medicationRepository.getMedicationById(id) ?: error("Medicamento não encontrado")
                val draft = medicationDraft(medication)
                _uiState.update { it.copy(medications = listOf(draft), showMedicationEditor = false, isLoading = false) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = "Não foi possível carregar o medicamento. Tente novamente.") } }
        }
    }
    private fun persistDraft(state: PrescriptionWizardUiState) {
        savedStateHandle["registration_draft"] = state.copy(isLoading = false, availableDoctors = emptyList(), availableMedications = emptyList(), availablePrescriptions = emptyList())
    }

    fun retryInitialLoad() { (prescriptionId ?: renewalOf)?.let(::loadPrescriptionForEditing); medicationId?.let(::loadMedicationForEditing) }

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
    private fun change(transform: (PrescriptionWizardUiState) -> PrescriptionWizardUiState) {
        if (_uiState.value.isLoading || _uiState.value.isSaved) return
        _uiState.update { transform(it).copy(hasUnsavedChanges = true, errorMessage = null) }
    }

    fun selectPrescriptionType(type: PrescriptionType) = change {
        it.copy(type = type, validityDays = type.defaultValidityDays,
            validityDaysInput = type.defaultValidityDays.toString(), validityOrigin = "SUGGESTED",
            renewalReminderDaysBefore = it.renewalReminderDaysBefore.coerceAtMost(type.defaultValidityDays))
    }

    fun updateValidityDays(value: String) = change {
        it.copy(validityDaysInput = value, validityDays = value.toIntOrNull(), validityOrigin = "USER_ENTERED")
    }

    fun updateExpirationDate(date: LocalDate) = change {
        val days = java.time.temporal.ChronoUnit.DAYS.between(it.issueDate, date).toInt()
        it.copy(validityDays = days, validityDaysInput = days.toString(), validityOrigin = "USER_ENTERED")
    }

    fun selectDoctor(doctor: Doctor) = change {
        it.copy(selectedDoctorId = doctor.id, doctorName = doctor.name,
            doctorRegistration = doctor.registration.orEmpty(), doctorSpecialty = doctor.specialty.orEmpty(),
            healthFacility = doctor.healthFacility.orEmpty(), doctorPhone = doctor.phone.orEmpty())
    }

    fun selectExistingMedication(medication: MedicationItem) = change {
        it.copy(currentMedicationDraft = MedicationDraft(id = medication.id, title = medication.title,
            activeIngredient = medication.activeIngredient.orEmpty(), dosageOverride = medication.dosage,
            form = medication.form, prescribedPosology = "", intervalUnit = medication.intervalUnit,
            linkedPrescriptionId = medication.linkedPrescriptionId, trackingArchived = medication.trackingArchived,
            source = medication.source, sourceName = medication.sourceName.orEmpty(),
            hasRecurringPickup = medication.hasRecurringPickup, lastPickupDate = medication.lastRefillDate,
            hasPickupHistory = medication.pickupHistory.isNotEmpty(), pickupIntervalDays = medication.refillCycleDays,
            pickupIntervalInput = medication.refillCycleDays.toString(),
            pickupReminderDaysBefore = medication.pickupReminderDaysBefore,
            replenishmentQuantity = medication.quantity, availabilityNote = medication.instructions.orEmpty()))
    }

    fun updatePickupInterval(index: Int, value: String) = updateMedicationPickup(index) {
        it.copy(pickupIntervalInput = value, pickupIntervalDays = value.toIntOrNull())
    }

    fun updateForm(transform: (PrescriptionWizardUiState) -> PrescriptionWizardUiState) = change(transform)
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
    fun removeMedication(index: Int) {
        change { state -> state.copy(medications = state.medications.filterIndexed { i, _ -> i != index }, currentMedicationDraft = MedicationDraft(), editingMedicationIndex = null, showMedicationEditor = state.medications.size <= 1) }
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
                        else false
                    }
                    doctorId = existing?.id ?: UUID.randomUUID().toString()
                    newDoctor = Doctor(doctorId, state.doctorName.trim(), state.doctorRegistration.takeIf { it.isNotBlank() }, state.doctorSpecialty.takeIf { it.isNotBlank() }, state.healthFacility.takeIf { it.isNotBlank() }, state.doctorPhone.takeIf { it.isNotBlank() })
                }
                val medications = drafts.map { draft ->
                    MedicationItem(
                        id = requireNotNull(draft.id), title = draft.title.trim(), activeIngredient = draft.activeIngredient.takeIf { it.isNotBlank() },
                        dosage = draft.dosage, form = draft.form, prescribedPosology = draft.prescribedPosology, prescribedQuantity = draft.prescribedQuantity,
                        quantity = draft.replenishmentQuantity.trim(), source = draft.source,
                        sourceName = draft.sourceName.takeIf { it.isNotBlank() },
                        instructions = draft.availabilityNote.takeIf { it.isNotBlank() },
                        lastRefillDate = if (draft.hasRecurringPickup) draft.lastPickupDate else null,
                        refillCycleDays = draft.pickupIntervalDays ?: 30, hasRecurringPickup = draft.hasRecurringPickup,
                        pickupReminderDaysBefore = draft.pickupReminderDaysBefore, intervalUnit = draft.intervalUnit,
                        linkedPrescriptionId = if (draft.linkToCurrentRecipe) state.editingPrescriptionId ?: state.registrationId else draft.linkedPrescriptionId,
                        trackingArchived = draft.trackingArchived, initialPickupDate = draft.initialPickupDate,
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
                    previousPrescriptionId = state.previousPrescriptionId, validityOrigin = state.validityOrigin, state = state.recordState,
                ) else null
                registrationRepository.save(recipe, medications, newDoctor)
                _uiState.update { it.copy(isLoading = false, isSaved = true, savedPrescriptionId = recipe?.id, prescriptionTitle = recipe?.title ?: it.prescriptionTitle, hasUnsavedChanges = false) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, errorMessage = if (e is BusinessRuleViolation) e.message else "Não foi possível salvar. Seus dados continuam aqui; tente novamente.") } }
        }
    }
}
