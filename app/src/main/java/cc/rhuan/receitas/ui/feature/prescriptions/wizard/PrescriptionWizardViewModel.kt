package cc.rhuan.receitas.ui.feature.prescriptions.wizard

import cc.rhuan.receitas.text.TextMessage

import cc.rhuan.receitas.text.TextResolver

import cc.rhuan.receitas.R

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
    private val texts: TextResolver,
    private val savedStateHandle: SavedStateHandle,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicationRepository: MedicationRepository,
    private val doctorRepository: DoctorRepository,
    private val registrationRepository: PrescriptionRegistrationRepository,
) : ViewModel() {

    private val prescriptionId: String? = savedStateHandle.get<String>("prescriptionId")
    private val renewalOf: String? = savedStateHandle.get<String>("renewalOf")
    private val medicationId: String? = savedStateHandle.get<String>("medicationId")
    private val addToPrescriptionId: String? = savedStateHandle.get<String>("addToPrescriptionId")
    private val entryModeParam: String? = savedStateHandle.get<String>("entryMode")

    private val restoredState = savedStateHandle.get<PrescriptionWizardUiState>("registration_draft")
    private val _uiState = MutableStateFlow(
        withOriginPrescription(restoredState?.copy(isLoading = false) ?: PrescriptionWizardUiState(
            editingPrescriptionId = prescriptionId,
            addToPrescriptionId = addToPrescriptionId,
            isLoading = !prescriptionId.isNullOrBlank() || !renewalOf.isNullOrBlank() || !medicationId.isNullOrBlank() || addToPrescriptionId != null,
            previousPrescriptionId = renewalOf,
            editingMedicationId = medicationId,
            entryMode = if (entryModeParam == "STANDALONE_MEDICATION") WizardEntryMode.STANDALONE_MEDICATION else WizardEntryMode.FULL_PRESCRIPTION,
        )),
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
        if ((restoredState == null || restoredState.initialLoadFailed) && addToPrescriptionId != null && restoredState?.isSaved != true) loadPrescriptionContext(addToPrescriptionId)
    }

    private fun loadPrescriptionContext(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, initialLoadFailed = false) }
            try {
                val recipe = prescriptionRepository.getPrescriptionById(id) ?: throw BusinessRuleViolation(TextMessage(R.string.prescription_not_found))
                requireBusiness(recipe.state == RecordState.ACTIVE) { TextMessage(R.string.reactivate_prescription_before_adding) }
                _uiState.update { it.copy(isLoading = false, addToPrescriptionTitle = recipe.title, existingPrescriptionMedicationIds = recipe.medicationIds, errorMessage = null) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = if (e is BusinessRuleViolation) texts.resolve(e.userMessage) else texts.get(R.string.prescription_load_retry)) } }
        }
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
                _uiState.update { it.copy(errorMessage = texts.get(R.string.medication_suggestions_load_error)) }
            }.collect { meds ->
                _uiState.update { it.copy(availableMedications = meds) }
            }
        }
        viewModelScope.launch {
            doctorRepository.getDoctors().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(errorMessage = texts.get(R.string.doctor_suggestions_load_error)) }
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
                require(prescription.state != RecordState.REPLACED) { texts.get(R.string.prescription_replaced) }
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
                        showMedicationEditor = linkedDrafts.isEmpty(),
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
                _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = texts.get(R.string.prescription_not_found)) }
            }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = texts.get(R.string.prescription_load_retry)) } }
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
                val medication = medicationRepository.getMedicationById(id) ?: error(texts.get(R.string.medication_not_found))
                val draft = medicationDraft(medication)
                _uiState.update { it.copy(medications = listOf(draft), showMedicationEditor = false, isLoading = false) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, initialLoadFailed = true, errorMessage = texts.get(R.string.medication_load_retry)) } }
        }
    }
    private fun persistDraft(state: PrescriptionWizardUiState) {
        savedStateHandle["registration_draft"] = state.copy(isLoading = false, availableDoctors = emptyList(), availableMedications = emptyList(), availablePrescriptions = emptyList())
    }

    fun retryInitialLoad() { (prescriptionId ?: renewalOf)?.let(::loadPrescriptionForEditing); medicationId?.let(::loadMedicationForEditing); addToPrescriptionId?.let(::loadPrescriptionContext) }

    // Navigation keeps a review return point instead of forcing the user through every step again.
    fun goToStep(step: Int) {
        if (_uiState.value.isLoading || _uiState.value.typeEditor != null || step !in 1.._uiState.value.maxSteps) return
        if (step == _uiState.value.pickupStep && _uiState.value.showMedicationEditor && !commitMedicationDraft()) return
        _uiState.update { it.copy(currentStep = step, reviewEditStep = if (it.currentStep == it.maxSteps) step else it.reviewEditStep, errorMessage = null) }
    }
    fun nextStep() {
        val state = _uiState.value
        if (state.isLoading || state.typeEditor != null) return
        state.currentStepError?.let { error -> _uiState.update { it.copy(errorMessage = texts.resolve(error)) }; return }
        if (state.currentStep == state.medicationStep && state.showMedicationEditor && !commitMedicationDraft()) return
        _uiState.update {
            val target = if (it.reviewEditStep != null) it.maxSteps else (it.currentStep + 1).coerceAtMost(it.maxSteps)
            it.copy(currentStep = target, reviewEditStep = null, errorMessage = null)
        }
    }
    fun skipStep() {
        if (_uiState.value.canSkipCurrentStep) nextStep()
    }

    fun previousStep() {
        if (_uiState.value.isLoading) return
        if (_uiState.value.typeEditor != null) { cancelTypeEditor(); return }
        _uiState.update { it.copy(currentStep = if (it.reviewEditStep != null) it.maxSteps else (it.currentStep - 1).coerceAtLeast(1), reviewEditStep = null, errorMessage = null) }
    }
    private fun change(transform: (PrescriptionWizardUiState) -> PrescriptionWizardUiState) {
        if (_uiState.value.isLoading || _uiState.value.isSaved) return
        _uiState.update { withOriginPrescription(transform(it)).copy(hasUnsavedChanges = true, errorMessage = null) }
    }

    private fun withOriginPrescription(state: PrescriptionWizardUiState): PrescriptionWizardUiState {
        val origin = state.addToPrescriptionId ?: return state
        return state.copy(medications = state.medications.map { it.copy(linkedPrescriptionId = origin, linkToCurrentRecipe = false) },
            currentMedicationDraft = state.currentMedicationDraft.copy(linkedPrescriptionId = origin, linkToCurrentRecipe = false))
    }

    fun openTypeEditor() {
        val state = _uiState.value
        if (state.isLoading || state.isSaved || state.initialLoadFailed || state.entryMode != WizardEntryMode.FULL_PRESCRIPTION || state.typeEditor != null) return
        _uiState.update { it.copy(typeEditor = PrescriptionTypeDraft(it.type, it.validityDays, it.validityDaysInput, it.validityOrigin, it.validityDateMode), errorMessage = null) }
    }

    fun updateIssueDate(date: LocalDate) = change {
        val days = if (it.validityDateMode && it.validityDays != null) {
            java.time.temporal.ChronoUnit.DAYS.between(date, it.issueDate.plusDays(it.validityDays.toLong())).toInt()
        } else it.validityDays
        it.copy(issueDate = date, validityDays = days, validityDaysInput = days?.toString() ?: it.validityDaysInput,
            renewalReminderDaysBefore = if (days != null && days > 0) it.renewalReminderDaysBefore.coerceIn(0, days) else it.renewalReminderDaysBefore)
    }

    fun cancelTypeEditor() { _uiState.update { it.copy(typeEditor = null, errorMessage = null) } }

    fun confirmTypeEditor() {
        val state = _uiState.value
        val original = state.typeEditor ?: return
        val suggested = original.type?.defaultValidityDays
        val draft = if (suggested != null) original.copy(validityDays = suggested, validityDaysInput = suggested.toString(), validityOrigin = "SUGGESTED", useExpirationDate = false) else original
        if (state.isLoading) return
        draft.error?.let { error -> _uiState.update { it.copy(errorMessage = texts.resolve(error)) }; return }
        change { it.copy(type = draft.type, validityDays = draft.validityDays, validityDaysInput = draft.validityDaysInput,
            validityOrigin = draft.validityOrigin, validityDateMode = draft.useExpirationDate,
            renewalReminderDaysBefore = it.renewalReminderDaysBefore.coerceIn(0, requireNotNull(draft.validityDays)), typeEditor = null,
            currentStep = when {
                it.reviewEditStep != null -> it.maxSteps
                it.currentStep == it.recipeIdentityStep -> requireNotNull(it.renewalOptionsStep)
                else -> it.currentStep
            }, reviewEditStep = null) }
    }

    fun setValidityDateMode(enabled: Boolean) {
        _uiState.update { state ->
            val draft = state.typeEditor
            val days = draft?.validityDays ?: java.time.temporal.ChronoUnit.DAYS.between(state.issueDate, LocalDate.now()).toInt().coerceAtLeast(1)
            state.copy(typeEditor = draft?.copy(useExpirationDate = enabled, manualUseExpirationDate = enabled,
                validityDays = if (enabled) days else draft.validityDays,
                validityDaysInput = if (enabled) days.toString() else draft.validityDaysInput), errorMessage = null)
        }
    }

    fun selectPrescriptionType(type: PrescriptionType) {
        val draft = _uiState.value.typeEditor
        if (draft != null) {
            val selected = when {
                type == draft.type && type.defaultValidityDays == null -> draft
                type.defaultValidityDays == null && draft.manualValidityDaysInput != null -> draft.copy(type = type,
                    validityDaysInput = draft.manualValidityDaysInput, validityDays = draft.manualValidityDaysInput.toIntOrNull(),
                    validityOrigin = "USER_ENTERED", useExpirationDate = draft.manualUseExpirationDate)
                else -> draft.copy(type = type, validityDays = type.defaultValidityDays,
                    validityDaysInput = type.defaultValidityDays?.toString().orEmpty(),
                    validityOrigin = if (type.defaultValidityDays == null) "USER_ENTERED" else "SUGGESTED",
                    useExpirationDate = if (type.defaultValidityDays == null) draft.useExpirationDate else false)
            }
            _uiState.update { it.copy(typeEditor = selected, errorMessage = null) }
            return
        }
        change {
        if (type.defaultValidityDays == null && (type == it.type || it.validityOrigin == "USER_ENTERED")) {
            it.copy(type = type, validityOrigin = "USER_ENTERED")
        }
        else it.copy(type = type, validityDays = type.defaultValidityDays,
            validityDaysInput = type.defaultValidityDays?.toString().orEmpty(),
            validityOrigin = if (type.defaultValidityDays == null) "USER_ENTERED" else "SUGGESTED",
            renewalReminderDaysBefore = it.renewalReminderDaysBefore.coerceAtMost(type.defaultValidityDays ?: 3650))
        }
    }

    fun updateValidityDays(value: String) {
        if (_uiState.value.typeEditor != null) {
            _uiState.update { it.copy(typeEditor = it.typeEditor?.copy(validityDaysInput = value, validityDays = value.toIntOrNull(), validityOrigin = "USER_ENTERED", useExpirationDate = false, manualValidityDaysInput = value, manualUseExpirationDate = false), errorMessage = null) }
            return
        }
        change {
        it.copy(validityDaysInput = value, validityDays = value.toIntOrNull(), validityOrigin = "USER_ENTERED", validityDateMode = false)
        }
    }

    fun updateExpirationDate(date: LocalDate) {
        if (_uiState.value.typeEditor != null) {
            _uiState.update {
                val days = java.time.temporal.ChronoUnit.DAYS.between(it.issueDate, date).toInt()
                it.copy(typeEditor = it.typeEditor?.copy(validityDays = days, validityDaysInput = days.toString(), validityOrigin = "USER_ENTERED", useExpirationDate = true, manualValidityDaysInput = days.toString(), manualUseExpirationDate = true), errorMessage = null)
            }
            return
        }
        change {
        val days = java.time.temporal.ChronoUnit.DAYS.between(it.issueDate, date).toInt()
        it.copy(validityDays = days, validityDaysInput = days.toString(), validityOrigin = "USER_ENTERED", validityDateMode = true)
        }
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
        it.copy(pickupIntervalInput = value, pickupIntervalDays = value.toIntOrNull(), intervalUnit = IntervalUnit.DAYS)
    }

    fun updateForm(transform: (PrescriptionWizardUiState) -> PrescriptionWizardUiState) = change(transform)
    fun commitMedicationDraft(): Boolean {
        val state = _uiState.value
        if (!state.showMedicationEditor) return true
        val draft = state.currentMedicationDraft
        if (draft == MedicationDraft(linkedPrescriptionId = state.addToPrescriptionId) && (state.medications.isNotEmpty() || state.entryMode == WizardEntryMode.FULL_PRESCRIPTION)) {
            change { it.copy(showMedicationEditor = it.medications.isEmpty()) }
            return true
        }
        if (!draft.isComplete) { _uiState.update { it.copy(errorMessage = texts.get(R.string.medication_name_required)) }; return false }
        val index = state.editingMedicationIndex
        if (draft.id in state.existingPrescriptionMedicationIds || state.medications.withIndex().any { it.index != index && draft.id != null && it.value.id == draft.id }) {
            _uiState.update { it.copy(errorMessage = texts.get(R.string.medication_already_added)) }
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
        if (state.isLoading || state.isSaved || state.typeEditor != null) return
        val validation = state.recipeError ?: state.medicationError ?: state.pickupError
        if (validation != null) { _uiState.update { it.copy(errorMessage = texts.resolve(validation)) }; return }
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
                        linkedPrescriptionId = state.addToPrescriptionId ?: if (draft.linkToCurrentRecipe) state.editingPrescriptionId ?: state.registrationId else draft.linkedPrescriptionId,
                        trackingArchived = draft.trackingArchived, initialPickupDate = draft.initialPickupDate,
                    )
                }
                val recipe = if (state.entryMode == WizardEntryMode.FULL_PRESCRIPTION) PrescriptionItem(
                    id = state.editingPrescriptionId ?: state.registrationId,
                    title = state.prescriptionTitle.trim().ifBlank { (drafts.firstOrNull()?.title ?: texts.get(R.string.prescription_default_title)) + " — " + state.issueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) },
                    specialty = state.doctorSpecialty, doctorId = doctorId, doctorName = state.doctorName,
                    doctorRegistration = state.doctorRegistration.takeIf { it.isNotBlank() }, issueDate = state.issueDate,
                    type = state.type, customValidityDays = state.validityDays, notes = state.notes.takeIf { it.isNotBlank() },
                    medicationIds = medications.map { it.id }, renewalReminderEnabled = state.renewalReminderEnabled,
                    renewalReminderDaysBefore = state.renewalReminderDaysBefore,
                    previousPrescriptionId = state.previousPrescriptionId, validityOrigin = state.validityOrigin, state = state.recordState,
                ) else null
                if (state.addToPrescriptionId != null) registrationRepository.addToPrescription(state.addToPrescriptionId, medications)
                else registrationRepository.save(recipe, medications, newDoctor)
                _uiState.update { it.copy(isLoading = false, isSaved = true, savedPrescriptionId = recipe?.id, prescriptionTitle = recipe?.title ?: it.prescriptionTitle, hasUnsavedChanges = false) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, errorMessage = if (e is BusinessRuleViolation) texts.resolve(e.userMessage) else texts.get(R.string.registration_save_retry)) } }
        }
    }
}
