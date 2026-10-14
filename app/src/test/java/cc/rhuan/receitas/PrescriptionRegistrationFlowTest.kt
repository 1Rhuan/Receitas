package cc.rhuan.receitas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.*
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.prescriptions.wizard.*
import java.time.LocalDate
import java.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PrescriptionRegistrationFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val models = mutableListOf<PrescriptionWizardViewModel>()
    private lateinit var meds: Medications
    private lateinit var recipes: Recipes
    private lateinit var registration: Registration
    @Before fun setup() { Dispatchers.setMain(dispatcher); meds = Medications(); recipes = Recipes(); registration = Registration() }
    @After fun tearDown() { models.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun model(handle: SavedStateHandle = SavedStateHandle()) = PrescriptionWizardViewModel(handle, recipes, meds, Doctors(), registration).also { models.add(it) }
    private fun add(vm: PrescriptionWizardViewModel, name: String) { vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = name)) }; assertTrue(vm.commitMedicationDraft()) }

    @Test fun fullFlowReachesReviewWithSeveralMedicationsAndReturnsFromEdits() = runTest {
        val vm = model(); runCurrent()
        vm.nextStep(); assertEquals(2, vm.uiState.value.currentStep)
        add(vm, "Losartana"); vm.startNewMedication(); add(vm, "Metformina")
        vm.nextStep(); assertEquals(3, vm.uiState.value.currentStep)
        vm.nextStep(); assertEquals(4, vm.uiState.value.currentStep)
        vm.editMedication(0); vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(dosageOverride = "50 mg")) }
        vm.nextStep()
        assertEquals(4, vm.uiState.value.currentStep)
        assertEquals(listOf("Losartana", "Metformina"), vm.uiState.value.allMedications.map { it.title })
        assertEquals("50 mg", vm.uiState.value.allMedications.first().dosage)
        vm.goToStep(1); vm.previousStep(); assertEquals(4, vm.uiState.value.currentStep)
    }
    @Test fun backKeepsAnUnfinishedMedicationEditVisibleInReview() = runTest {
        val vm = model(); runCurrent(); vm.nextStep(); add(vm, "Losartana"); vm.nextStep(); vm.nextStep()
        vm.editMedication(0); vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = "")) }
        vm.previousStep()
        assertEquals(4, vm.uiState.value.currentStep)
        assertFalse(vm.uiState.value.canSave)
        vm.savePrescription(); runCurrent(); assertEquals(0, registration.attempts)
        vm.editMedication(0)
        assertEquals(2, vm.uiState.value.currentStep)
    }
    @Test fun pickupRequiresRealDateAndPositiveIntervalButNotDoseOrStock() = runTest {
        val vm = model(); runCurrent(); vm.nextStep(); add(vm, "Losartana"); vm.nextStep()
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true) }; vm.nextStep()
        assertEquals(3, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(lastPickupDate = LocalDate.now(), pickupIntervalDays = 0) }; vm.nextStep()
        assertEquals(3, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(pickupIntervalDays = 30) }; vm.nextStep()
        assertEquals(4, vm.uiState.value.currentStep)
        assertTrue(vm.uiState.value.canSave)
    }
    @Test fun retryAndDoubleTapPreserveIdsAndReminderSettings() = runTest {
        val vm = model(); runCurrent(); vm.nextStep(); add(vm, "Losartana"); vm.nextStep(); vm.nextStep()
        vm.updateForm { it.copy(renewalReminderEnabled = false, renewalReminderDaysBefore = 7) }
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true, lastPickupDate = LocalDate.now(), pickupReminderDaysBefore = 1) }
        registration.fail = true
        vm.savePrescription(); vm.savePrescription(); runCurrent()
        assertEquals(1, registration.attempts); assertFalse(vm.uiState.value.isSaved)
        val firstRecipe = registration.recipe!!.id; val firstMed = registration.medications.single().id
        registration.fail = false; vm.savePrescription(); runCurrent()
        assertEquals(firstRecipe, registration.recipe!!.id); assertEquals(firstMed, registration.medications.single().id)
        assertFalse(registration.recipe!!.renewalReminderEnabled)
        assertEquals(1, registration.medications.single().pickupReminderDaysBefore)
        assertEquals("", registration.medications.single().prescribedPosology)
        assertTrue(vm.uiState.value.isSaved)
        vm.savePrescription(); runCurrent(); assertEquals(2, registration.attempts)
    }
    @Test fun draftRestoresReviewAndSerializesWithoutSuggestionObjects() = runTest {
        val handle = SavedStateHandle()
        val vm = model(handle); runCurrent(); vm.nextStep(); add(vm, "Losartana"); vm.nextStep(); vm.nextStep(); runCurrent()
        val snapshot = handle.get<PrescriptionWizardUiState>("registration_draft")!!
        val bytes = ByteArrayOutputStream().also { ObjectOutputStream(it).use { out -> out.writeObject(snapshot) } }.toByteArray()
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as PrescriptionWizardUiState }
        val recreated = model(SavedStateHandle(mapOf("registration_draft" to restored))); runCurrent()
        assertEquals(4, recreated.uiState.value.currentStep)
        assertEquals("Losartana", recreated.uiState.value.allMedications.single().title)
        assertEquals(vm.uiState.value.registrationId, recreated.uiState.value.registrationId)
    }
    @Test fun standaloneMedicationHasThreeStepsAndDoesNotCreatePrescription() = runTest {
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION"))); runCurrent()
        add(vm, "Losartana"); vm.nextStep(); vm.nextStep(); vm.savePrescription(); runCurrent()
        assertEquals(3, vm.uiState.value.maxSteps)
        assertNull(registration.recipe); assertTrue(vm.uiState.value.isSaved)
    }
    @Test fun editingKeepsRenewalAndPickupPreferences() = runTest {
        meds.items.value = listOf(MedicationItem("m", "Losartana", hasRecurringPickup = false, pickupReminderDaysBefore = 1))
        recipes.items.value = listOf(PrescriptionItem("p", "Receita", "", medicationIds = listOf("m"), renewalReminderEnabled = false, renewalReminderDaysBefore = 9))
        val vm = model(SavedStateHandle(mapOf("prescriptionId" to "p"))); runCurrent()
        assertFalse(vm.uiState.value.renewalReminderEnabled)
        assertEquals(9, vm.uiState.value.renewalReminderDaysBefore)
        assertFalse(vm.uiState.value.medications.single().hasRecurringPickup)
        vm.savePrescription(); runCurrent()
        assertEquals("p", registration.recipe!!.id)
        assertFalse(registration.recipe!!.renewalReminderEnabled)
    }
    @Test fun idsArePersistedBeforeTheSaveFinishes() = runTest {
        val handle = SavedStateHandle()
        val vm = model(handle); runCurrent(); vm.nextStep()
        vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = "Losartana")) }
        registration.gate = CompletableDeferred()
        vm.savePrescription(); runCurrent()
        val snapshot = handle.get<PrescriptionWizardUiState>("registration_draft")!!
        assertEquals(registration.medications.single().id, snapshot.medications.single().id)
        assertFalse(snapshot.isLoading)
        registration.gate!!.complete(Unit); runCurrent()
        assertTrue(vm.uiState.value.isSaved)
    }
    @Test fun missingRecipeCannotBeOverwrittenByAnEmptyEdit() = runTest {
        val vm = model(SavedStateHandle(mapOf("prescriptionId" to "missing"))); runCurrent()
        assertTrue(vm.uiState.value.initialLoadFailed)
        vm.savePrescription(); runCurrent(); assertEquals(0, registration.attempts)
    }
    private class Registration : PrescriptionRegistrationRepository {
        var gate: CompletableDeferred<Unit>? = null
        var attempts = 0; var fail = false; var recipe: PrescriptionItem? = null; var medications = emptyList<MedicationItem>()
        override suspend fun save(prescription: PrescriptionItem?, medications: List<MedicationItem>, newDoctor: Doctor?) {
            attempts++; recipe = prescription; this.medications = medications
            gate?.await()
            if (fail) error("Falha simulada")
        }
    }
    private class Medications : MedicationRepository {
        val items = MutableStateFlow<List<MedicationItem>>(emptyList())
        override fun getMedications(): Flow<List<MedicationItem>> = items
        override suspend fun getMedicationById(id: String) = items.value.find { it.id == id }
        override suspend fun replenishStock(id: String?, title: String?, message: String?, customDate: LocalDate) {}
        override suspend fun addMedication(medication: MedicationItem) {}
        override suspend fun updateMedication(medication: MedicationItem) {}
    }
    private class Recipes : PrescriptionRepository {
        val items = MutableStateFlow<List<PrescriptionItem>>(emptyList())
        override fun getPrescriptions(): Flow<List<PrescriptionItem>> = items
        override suspend fun getPrescriptionById(id: String) = items.value.find { it.id == id }
        override suspend fun renewPrescription(id: String?, title: String?, message: String?, customDate: LocalDate) {}
        override suspend fun addPrescription(prescription: PrescriptionItem) {}
        override suspend fun updatePrescription(prescription: PrescriptionItem) {}
        override suspend fun deletePrescription(id: String) {}
    }
    private class Doctors : DoctorRepository {
        override fun getDoctors(): Flow<List<Doctor>> = flowOf(listOf(Doctor("suggestion", "Profissional sugerido")))
        override suspend fun getDoctorById(id: String): Doctor? = null
        override suspend fun addDoctor(doctor: Doctor) {}
        override suspend fun updateDoctor(doctor: Doctor) {}
    }
}
