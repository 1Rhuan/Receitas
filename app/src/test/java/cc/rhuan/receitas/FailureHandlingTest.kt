package cc.rhuan.receitas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rhuan.receitas.data.repository.*
import cc.rhuan.receitas.domain.model.*
import cc.rhuan.receitas.ui.feature.medications.*
import cc.rhuan.receitas.ui.feature.prescriptions.*
import cc.rhuan.receitas.ui.feature.notifications.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FailureHandlingTest {
    private val dispatcher = StandardTestDispatcher()
    private val models = mutableListOf<ViewModel>()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun close() { models.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun <T : ViewModel> track(vm: T): T = vm.also { models.add(it) }
    @Test fun medicationLoadFailureCanBeRetriedWithoutDuplicateCollectors() = runTest {
        val repository = Medications()
        repository.failure = true
        val vm = track(MedicationsViewModel(TestTexts, repository)); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
        repository.failure = false
        vm.loadMedications(); runCurrent()
        assertNull(vm.uiState.value.errorMessage)
        assertEquals(1, vm.uiState.value.medications.size)
        vm.loadMedications(); vm.loadMedications(); runCurrent()
        assertEquals(1, repository.activeCollectors)
    }
    @Test fun prescriptionReloadKeepsOnlyOneSubscription() = runTest {
        val repository = Recipes()
        val medications = Medications()
        val vm = track(PrescriptionsViewModel(TestTexts, repository, medications)); runCurrent()
        vm.loadPrescriptions(); vm.loadPrescriptions(); runCurrent()
        assertEquals(1, repository.activeCollectors)
        assertEquals(1, medications.activeCollectors)
        assertFalse(vm.uiState.value.isLoading)
    }
    @Test fun missingMedicationIdEndsLoadingWithoutCrashing() = runTest {
        val vm = track(MedicationDetailViewModel(TestTexts, SavedStateHandle(), Medications(), Recipes())); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
    }
    @Test fun missingMedicationIsNotAnInfiniteSpinner() = runTest {
        val vm = track(MedicationDetailViewModel(TestTexts, SavedStateHandle(mapOf("medicationId" to "missing")), Medications(), Recipes())); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.medication)
        assertNotNull(vm.uiState.value.errorMessage)
    }
    @Test fun medicationDetailFailureCanRecover() = runTest {
        val repository = Medications().apply { failure = true }
        val vm = track(MedicationDetailViewModel(TestTexts, SavedStateHandle(mapOf("medicationId" to "m")), repository, Recipes())); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
        repository.failure = false
        vm.loadMedication(); runCurrent()
        assertEquals("m", vm.uiState.value.medication!!.id)
        assertNull(vm.uiState.value.errorMessage)
    }
    @Test fun medicationDetailObservesRecipeMembershipSeparatelyFromPickupRecipe() = runTest {
        val member = PrescriptionItem("member", "Receita de origem", "", medicationIds = listOf("m"))
        val pickup = PrescriptionItem("pickup", "Receita para retiradas", "")
        val updates = MutableStateFlow(listOf(member, pickup))
        val recipes = Recipes().apply { this.updates = updates }
        val meds = Medications().apply { item = MedicationItem("m", "Medicamento", linkedPrescriptionId = "pickup") }
        val vm = track(MedicationDetailViewModel(TestTexts, SavedStateHandle(mapOf("medicationId" to "m")), meds, recipes))
        runCurrent()
        assertEquals(listOf(member), vm.uiState.value.prescriptions)
        assertEquals(pickup, vm.uiState.value.pickupPrescription)
        updates.value = listOf(member.copy(title = "Receita renomeada"))
        runCurrent()
        assertEquals("Receita renomeada", vm.uiState.value.prescriptions.single().title)
        assertNull(vm.uiState.value.pickupPrescription)
        updates.value = emptyList()
        runCurrent()
        assertTrue(vm.uiState.value.prescriptions.isEmpty())
    }
    @Test fun medicationInRecipeWithoutPickupLinkStillShowsRecipe() = runTest {
        val recipe = PrescriptionItem("member", "Receita de origem", "",
            items = listOf(PrescriptionMedication(medicationId = "m")))
        val recipes = Recipes().apply { updates = MutableStateFlow(listOf(recipe)) }
        val vm = track(MedicationDetailViewModel(TestTexts, SavedStateHandle(mapOf("medicationId" to "m")), Medications(), recipes))
        runCurrent()
        assertNull(vm.uiState.value.medication!!.linkedPrescriptionId)
        assertEquals(listOf(recipe), vm.uiState.value.prescriptions)
    }
    @Test fun prescriptionDetailFailureCanRecover() = runTest {
        val recipes = Recipes().apply { failure = true }
        val vm = track(PrescriptionDetailViewModel(TestTexts, SavedStateHandle(mapOf("prescriptionId" to "p")), recipes, Medications())); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
        recipes.failure = false
        vm.loadDetail(); runCurrent()
        assertEquals("p", vm.uiState.value.prescription!!.id)
        assertNull(vm.uiState.value.errorMessage)
    }
    @Test fun malformedAlertDoesNotPerformAnActionSilently() = runTest {
        val notifications = Notices()
        notifications.items = listOf(NotificationItem("bad", "Alerta", "Texto", "Hoje", NotificationType.MEDICATION_PICKUP,
            targetId = "m", phase = ReminderPhase.PICKUP_DUE, targetType = "PICKUP", cycleKey = "invalid-date"))
        val medications = Medications()
        val vm = track(NotificationsViewModel(TestTexts, notifications, Recipes(), medications)); runCurrent()
        var opened = false
        vm.preparePickup("bad") { opened = true }; runCurrent()
        assertFalse(opened)
        assertEquals(0, medications.recordings)
        assertNotNull(vm.uiState.value.snackbarMessage)
    }
    @Test fun notificationLoadFailureCanBeRetried() = runTest {
        val notices = Notices().apply { failure = true }
        val vm = track(NotificationsViewModel(TestTexts, notices, Recipes(), Medications())); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
        notices.failure = false
        vm.refresh(); runCurrent()
        assertNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isRefreshing)
    }
    @Test fun replacedRecipeCannotOpenRenewalFromAnOldAlert() = runTest {
        val notices = Notices().apply { items = listOf(NotificationItem("alert", "Receita", "Texto", "Hoje", NotificationType.PRESCRIPTION_EXPIRING, targetId = "p", phase = ReminderPhase.EXPIRED, targetType = "PRESCRIPTION")) }
        val recipes = Recipes().apply { state = RecordState.REPLACED }
        val vm = track(NotificationsViewModel(TestTexts, notices, recipes, Medications())); runCurrent()
        var opened = false
        vm.openRenewal("alert") { opened = true }; runCurrent()
        assertFalse(opened)
        assertNotNull(vm.uiState.value.snackbarMessage)
    }
    private class Medications : MedicationRepository {
        var item = MedicationItem("m", "Medicamento")
        var failure = false
        var activeCollectors = 0
        var recordings = 0
        override fun getMedications(): Flow<List<MedicationItem>> = flow {
            if (failure) error("Simulated disk failure")
            activeCollectors++
            try { emit(listOf(item)); awaitCancellation() } finally { activeCollectors-- }
        }
        override suspend fun getMedicationById(id: String) = if (id == "m") MedicationItem("m", "Medicamento") else null
        override suspend fun registerPickup(id: String, date: LocalDate, confirmedEarly: Boolean, expectedCycle: LocalDate?): OperationResult { recordings++; return OperationResult.Success }
        override suspend fun addMedication(medication: MedicationItem) {}
        override suspend fun updateMedication(medication: MedicationItem) {}
        override suspend fun correctPickup(eventId: String, date: LocalDate) = OperationResult.NotFound
        override suspend fun deletePickup(eventId: String) = OperationResult.NotFound
        override suspend fun archiveTracking(id: String, archived: Boolean) = OperationResult.NotFound
        override suspend fun deleteMedication(id: String) = OperationResult.NotFound
    }
    private class Recipes : PrescriptionRepository {
        var updates: MutableStateFlow<List<PrescriptionItem>>? = null
        var failure = false
        var activeCollectors = 0
        var state = RecordState.ACTIVE
        private fun recipe() = PrescriptionItem("p", "Receita", "", state = state)
        override fun getPrescriptions(): Flow<List<PrescriptionItem>> = updates ?: flow {
            if (failure) error("Simulated disk failure")
            activeCollectors++
            try { emit(listOf(recipe())); awaitCancellation() } finally { activeCollectors-- }
        }
        override suspend fun getPrescriptionById(id: String) = if (id == "p") recipe() else null
        override suspend fun addPrescription(prescription: PrescriptionItem) {}
        override suspend fun updatePrescription(prescription: PrescriptionItem) {}
        override suspend fun deletePrescription(id: String) {}
        override suspend fun archivePrescription(id: String, archived: Boolean) {}
    }
    private class Notices : NotificationRepository {
        var failure = false
        var items = emptyList<NotificationItem>()
        override fun getNotifications(): Flow<List<NotificationItem>> = flow { if (failure) error("Simulated disk failure"); emit(items); awaitCancellation() }
        override suspend fun refresh() {}
        override suspend fun markAsRead(id: String) {}
        override suspend fun markAllAsRead() {}
        override suspend fun deleteNotification(id: String) {}
        override suspend fun clearAll() {}
    }
}
