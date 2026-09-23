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
import cc.rhuan.receitas.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
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
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val prescriptionId: String? = savedStateHandle.get<String>("prescriptionId")

    private val _uiState = MutableStateFlow(PrescriptionWizardUiState())
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
                                source = MedicationSource.fromLabel(medItem.category),
                                lastPickupDate = medItem.lastRefillDate,
                                pickupIntervalDays = medItem.refillCycleDays,
                                currentStock = medItem.stockQuantity,
                                minStockThreshold = medItem.minStockThreshold,
                                availabilityNote = medItem.instructions ?: ""
                            )
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
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Receita não encontrada") }
            }
        }
    }

    // Navigation
    fun goToStep(step: Int) {
        if (step in 1..6) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    fun nextStep() {
        val currentState = _uiState.value

        // Auto-generate title if not customized when completing medication setup
        if (!currentState.isTitleCustomized) {
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

        if (currentState.currentStep < 6) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun skipStep() {
        val currentState = _uiState.value
        when (currentState.currentStep) {
            4 -> {
                // Skip Step 4 (Prescription Type & Validity)
                _uiState.update {
                    it.copy(
                        isStep4Skipped = true,
                        type = null,
                        validityDays = null,
                        renewalReminderEnabled = false
                    )
                }
            }
            5 -> {
                // Skip Step 5 (Doctor)
                _uiState.update {
                    it.copy(
                        selectedDoctorId = null,
                        doctorName = "",
                        doctorRegistration = "",
                        doctorSpecialty = "",
                        healthFacility = "",
                        doctorPhone = ""
                    )
                }
            }
        }
        nextStep()
    }

    // Medication Draft Management
    fun updateCurrentMedicationDraft(
        title: String? = null,
        form: PharmaceuticalForm? = null,
        dosageValue: String? = null,
        dosageUnit: String? = null,
        dosageOverride: String? = null,
        doseAmount: String? = null,
        frequencyHours: Int? = null,
        durationDays: Int? = null,
        firstDoseTime: LocalTime? = null,
        source: MedicationSource? = null,
        sourceName: String? = null,
        hasRecurringPickup: Boolean? = null,
        lastPickupDate: LocalDate? = null,
        pickupIntervalDays: Int? = null,
        currentStock: Int? = null,
        availabilityNote: String? = null
    ) {
        _uiState.update { state ->
            val draft = state.currentMedicationDraft.copy(
                title = title ?: state.currentMedicationDraft.title,
                form = form ?: state.currentMedicationDraft.form,
                dosageValue = dosageValue ?: state.currentMedicationDraft.dosageValue,
                dosageUnit = dosageUnit ?: state.currentMedicationDraft.dosageUnit,
                dosageOverride = dosageOverride ?: state.currentMedicationDraft.dosageOverride,
                doseAmount = doseAmount ?: state.currentMedicationDraft.doseAmount,
                frequencyHours = frequencyHours ?: state.currentMedicationDraft.frequencyHours,
                durationDays = durationDays ?: state.currentMedicationDraft.durationDays,
                firstDoseTime = firstDoseTime ?: state.currentMedicationDraft.firstDoseTime,
                source = source ?: state.currentMedicationDraft.source,
                sourceName = sourceName ?: state.currentMedicationDraft.sourceName,
                hasRecurringPickup = hasRecurringPickup ?: state.currentMedicationDraft.hasRecurringPickup,
                lastPickupDate = lastPickupDate ?: state.currentMedicationDraft.lastPickupDate,
                pickupIntervalDays = pickupIntervalDays ?: state.currentMedicationDraft.pickupIntervalDays,
                currentStock = currentStock ?: state.currentMedicationDraft.currentStock,
                availabilityNote = availabilityNote ?: state.currentMedicationDraft.availabilityNote
            )
            state.copy(currentMedicationDraft = draft)
        }
    }

    fun selectExistingMedication(medicationItem: MedicationItem) {
        _uiState.update { state ->
            val draft = state.currentMedicationDraft.copy(
                id = medicationItem.id,
                title = medicationItem.title,
                dosageOverride = medicationItem.dosage,
                form = medicationItem.form
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
                    currentStep = 1
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

    // Step 4: Prescription Identification & Type
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
                validityDays = type.defaultValidityDays
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

    // Step 5: Doctor
    fun selectDoctor(doctor: Doctor) {
        _uiState.update {
            it.copy(
                selectedDoctorId = doctor.id,
                doctorName = doctor.name,
                doctorRegistration = doctor.registration ?: "",
                doctorSpecialty = doctor.specialty ?: "",
                healthFacility = doctor.healthFacility ?: "",
                doctorPhone = doctor.phone ?: ""
            )
        }
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

    fun toggleExtraDoctorFields() {
        _uiState.update { it.copy(showExtraDoctorFields = !it.showExtraDoctorFields) }
    }

    // Step 6: Notes and Photo
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updatePhotoUri(uri: String?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    // Save Prescription
    fun savePrescription() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value

                // 1. Doctor
                var doctorId = state.selectedDoctorId
                if (doctorId == null && state.doctorName.isNotBlank()) {
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
                            phone = state.doctorPhone.takeIf { it.isNotBlank() }
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
                        dosage = draft.dosage,
                        quantity = "${draft.currentStock ?: 0} un",
                        frequency = "A cada ${draft.frequencyHours} horas",
                        category = draft.source.label,
                        isAvailable = true,
                        instructions = draft.availabilityNote.takeIf { it.isNotBlank() },
                        form = draft.form,
                        stockQuantity = draft.currentStock ?: 0,
                        minStockThreshold = draft.minStockThreshold,
                        lastRefillDate = draft.lastPickupDate,
                        refillCycleDays = draft.pickupIntervalDays ?: 30
                    )

                    if (draft.id == null) {
                        medicationRepository.addMedication(medItem)
                    } else {
                        medicationRepository.updateMedication(medItem)
                    }

                    // Schedule Alarm
                    val scheduleId = "sched_$medId"
                    var scheduledDateTime = LocalDateTime.of(LocalDate.now(), draft.firstDoseTime)
                    if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                        scheduledDateTime = scheduledDateTime.plusDays(1)
                    }
                    AlarmScheduler.scheduleDoseAlarm(
                        context = appContext,
                        scheduleId = scheduleId,
                        medicationId = medId,
                        medicationTitle = draft.title,
                        dosage = draft.dosage,
                        scheduledDateTime = scheduledDateTime
                    )
                }

                // 3. Prescription
                val prescriptionItem = PrescriptionItem(
                    id = state.editingPrescriptionId ?: UUID.randomUUID().toString(),
                    title = state.prescriptionTitle,
                    specialty = state.doctorSpecialty,
                    doctorId = doctorId,
                    doctorName = state.doctorName,
                    doctorRegistration = state.doctorRegistration.takeIf { it.isNotBlank() },
                    issueDate = state.issueDate,
                    type = if (state.isStep4Skipped) null else state.type,
                    customValidityDays = if (state.isStep4Skipped) null else state.validityDays,
                    notes = state.notes.takeIf { it.isNotBlank() },
                    medicationIds = savedMedicationIds
                )

                if (state.editingPrescriptionId == null) {
                    prescriptionRepository.addPrescription(prescriptionItem)
                } else {
                    prescriptionRepository.updatePrescription(prescriptionItem)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Erro ao salvar receita") }
            }
        }
    }
}

private val PrescriptionItem.customValidityDurationDays: Int?
    get() = customValidityDays
