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
    private fun model(handle: SavedStateHandle = SavedStateHandle()) = PrescriptionWizardViewModel(handle, recipes, meds, Doctors(), registration).also { vm -> models.add(vm); vm.updateForm { if (it.prescriptionTitle.isBlank()) it.copy(prescriptionTitle = "Receita de teste") else it } }
    private fun add(vm: PrescriptionWizardViewModel, name: String) { vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = name)) }; assertTrue(vm.commitMedicationDraft()) }

    @Test fun validityActionsKeepSuggestedAndEnteredDeadlinesDistinct() = runTest {
        val vm = model(); runCurrent()
        vm.updateForm { it.copy(renewalReminderDaysBefore = 90, issueDate = LocalDate.of(2026, 10, 1)) }
        vm.selectPrescriptionType(PrescriptionType.ANTIBIOTICO)
        assertEquals(PrescriptionType.ANTIBIOTICO.defaultValidityDays, vm.uiState.value.validityDays)
        assertEquals("SUGGESTED", vm.uiState.value.validityOrigin)
        assertTrue(vm.uiState.value.renewalReminderDaysBefore <= vm.uiState.value.validityDays!!)
        vm.updateExpirationDate(LocalDate.of(2026, 10, 16))
        assertEquals(15, vm.uiState.value.validityDays)
        assertEquals("15", vm.uiState.value.validityDaysInput)
        assertEquals("USER_ENTERED", vm.uiState.value.validityOrigin)
        vm.updateValidityDays("abc")
        assertEquals("abc", vm.uiState.value.validityDaysInput)
        assertNull(vm.uiState.value.validityDays)
        vm.goToStep(requireNotNull(vm.uiState.value.renewalOptionsStep))
        vm.nextStep()
        assertEquals(vm.uiState.value.renewalOptionsStep, vm.uiState.value.currentStep)
    }

    @Test fun customTypeRequiresEnteredValidityAndKeepsItWhenSelectedAgain() = runTest {
        val vm = model(); runCurrent()
        vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        assertNull(vm.uiState.value.validityDays)
        assertEquals("", vm.uiState.value.validityDaysInput)
        assertEquals("USER_ENTERED", vm.uiState.value.validityOrigin)
        vm.goToStep(requireNotNull(vm.uiState.value.renewalOptionsStep)); vm.nextStep()
        assertEquals(vm.uiState.value.renewalOptionsStep, vm.uiState.value.currentStep)
        vm.updateValidityDays("45"); vm.nextStep()
        assertEquals(vm.uiState.value.medicationStep, vm.uiState.value.currentStep)
        vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        assertEquals(45, vm.uiState.value.validityDays)
        vm.updateExpirationDate(vm.uiState.value.issueDate.plusDays(60))
        assertEquals(60, vm.uiState.value.validityDays)
        add(vm, "Losartana")
        vm.savePrescription(); runCurrent()
        assertEquals(PrescriptionType.PERSONALIZADO, registration.recipe!!.type)
        assertEquals(60, registration.recipe!!.validityDurationDays)
        val converters = cc.rhuan.receitas.data.local.Converters()
        assertEquals(PrescriptionType.PERSONALIZADO, converters.toPrescriptionType(converters.fromPrescriptionType(PrescriptionType.PERSONALIZADO)))
        val presetVm = model(); runCurrent()
        presetVm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        presetVm.updateValidityDays("60")
        presetVm.selectPrescriptionType(PrescriptionType.ANTIBIOTICO)
        assertEquals(10, presetVm.uiState.value.validityDays)
        assertEquals("SUGGESTED", presetVm.uiState.value.validityOrigin)
        presetVm.updateValidityDays("75")
        presetVm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        assertEquals(75, presetVm.uiState.value.validityDays)
        assertEquals("USER_ENTERED", presetVm.uiState.value.validityOrigin)
    }

    @Test fun selectingExistingMedicationPreservesTrackingAndClearsRecipePosology() = runTest {
        val vm = model(); runCurrent()
        val medication = MedicationItem("existing", "Losartana", dosage = "50 mg",
            hasRecurringPickup = true, lastRefillDate = LocalDate.of(2026, 10, 1),
            refillCycleDays = 30, pickupReminderDaysBefore = 3, prescribedPosology = "Receita anterior")
        vm.selectExistingMedication(medication)
        val draft = vm.uiState.value.currentMedicationDraft
        assertEquals("existing", draft.id)
        assertEquals("50 mg", draft.dosage)
        assertEquals(medication.lastRefillDate, draft.lastPickupDate)
        assertEquals(3, draft.pickupReminderDaysBefore)
        assertEquals("", draft.prescribedPosology)
        assertTrue(vm.commitMedicationDraft())
        vm.updatePickupInterval(0, "")
        assertNull(vm.uiState.value.medications.single().pickupIntervalDays)
        vm.updatePickupInterval(0, "45")
        assertEquals(45, vm.uiState.value.medications.single().pickupIntervalDays)
        assertEquals("45", vm.uiState.value.medications.single().pickupIntervalInput)
    }

    @Test fun selectingDoctorCopiesOptionalFieldsWithoutNulls() = runTest {
        val vm = model(); runCurrent()
        vm.updateForm { it.copy(doctorRegistration = "antigo", doctorPhone = "antigo") }
        vm.selectDoctor(Doctor("doctor", "Dra. Ana", specialty = "Clínica"))
        assertEquals("doctor", vm.uiState.value.selectedDoctorId)
        assertEquals("Dra. Ana", vm.uiState.value.doctorName)
        assertEquals("Clínica", vm.uiState.value.doctorSpecialty)
        assertEquals("", vm.uiState.value.doctorRegistration)
        assertEquals("", vm.uiState.value.doctorPhone)
        assertTrue(vm.uiState.value.hasUnsavedChanges)
    }

    @Test fun fullFlowReachesReviewWithSeveralMedicationsAndReturnsFromEdits() = runTest {
        val vm = model(); runCurrent()
        repeat(3) { vm.nextStep() }; assertEquals(vm.uiState.value.medicationStep, vm.uiState.value.currentStep)
        add(vm, "Losartana"); vm.startNewMedication(); add(vm, "Metformina")
        vm.nextStep(); assertEquals(vm.uiState.value.pickupStep, vm.uiState.value.currentStep)
        vm.nextStep(); assertEquals(vm.uiState.value.optionalDetailsStep, vm.uiState.value.currentStep)
        vm.nextStep(); assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        vm.editMedication(0); vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(dosageOverride = "50 mg")) }
        vm.nextStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals(listOf("Losartana", "Metformina"), vm.uiState.value.allMedications.map { it.title })
        assertEquals("50 mg", vm.uiState.value.allMedications.first().dosage)
        vm.goToStep(1); vm.previousStep(); assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
    }
    @Test fun backKeepsAnUnfinishedMedicationEditVisibleInReview() = runTest {
        val vm = model(); runCurrent(); repeat(3) { vm.nextStep() }; add(vm, "Losartana"); vm.nextStep(); vm.nextStep(); vm.nextStep()
        vm.editMedication(0); vm.updateForm { it.copy(currentMedicationDraft = it.currentMedicationDraft.copy(title = "")) }
        vm.previousStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertFalse(vm.uiState.value.canSave)
        vm.savePrescription(); runCurrent(); assertEquals(0, registration.attempts)
        vm.editMedication(0)
        assertEquals(vm.uiState.value.medicationStep, vm.uiState.value.currentStep)
    }
    @Test fun pickupRequiresRealDateAndPositiveIntervalButNotDoseOrStock() = runTest {
        val vm = model(); runCurrent(); repeat(3) { vm.nextStep() }; add(vm, "Losartana"); vm.nextStep()
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true) }; vm.nextStep()
        assertEquals(vm.uiState.value.pickupStep, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(lastPickupDate = LocalDate.now(), pickupIntervalDays = 0) }; vm.nextStep()
        assertEquals(vm.uiState.value.pickupStep, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(pickupIntervalDays = 30) }; vm.nextStep()
        assertEquals(vm.uiState.value.pickupOptionsStep, vm.uiState.value.currentStep)
        assertTrue(vm.uiState.value.canSave)
    }
    @Test fun retryAndDoubleTapPreserveIdsAndReminderSettings() = runTest {
        val vm = model(); runCurrent(); repeat(3) { vm.nextStep() }; add(vm, "Losartana"); vm.nextStep(); vm.nextStep()
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
        val vm = model(handle); runCurrent(); repeat(3) { vm.nextStep() }; add(vm, "Losartana"); vm.nextStep(); vm.nextStep(); vm.nextStep(); runCurrent()
        val snapshot = handle.get<PrescriptionWizardUiState>("registration_draft")!!
        val bytes = ByteArrayOutputStream().also { ObjectOutputStream(it).use { out -> out.writeObject(snapshot) } }.toByteArray()
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as PrescriptionWizardUiState }
        val recreated = model(SavedStateHandle(mapOf("registration_draft" to restored))); runCurrent()
        assertEquals(recreated.uiState.value.maxSteps, recreated.uiState.value.currentStep)
        assertEquals("Losartana", recreated.uiState.value.allMedications.single().title)
        assertEquals(vm.uiState.value.registrationId, recreated.uiState.value.registrationId)
    }
    @Test fun standaloneMedicationEndsWithOptionalDetailsAndDoesNotCreatePrescription() = runTest {
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION"))); runCurrent()
        add(vm, "Losartana"); vm.nextStep(); vm.nextStep(); assertEquals(vm.uiState.value.optionalDetailsStep, vm.uiState.value.currentStep); vm.nextStep(); vm.savePrescription(); runCurrent()
        assertEquals(4, vm.uiState.value.maxSteps)
        assertNull(registration.recipe); assertTrue(vm.uiState.value.isSaved)
    }
    @Test fun recurringPickupSeparatesScheduleFromPreferencesAndCanReturnFromReview() = runTest {
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION"))); runCurrent()
        add(vm, "Losartana"); vm.nextStep()
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true, lastPickupDate = LocalDate.now(), pickupIntervalDays = 1, pickupReminderDaysBefore = 3) }
        assertEquals(6, vm.uiState.value.maxSteps)
        vm.nextStep()
        assertEquals(vm.uiState.value.pickupOptionsStep, vm.uiState.value.currentStep)
        vm.nextStep() // Invalid lead time must be corrected on the preferences screen.
        assertEquals(vm.uiState.value.pickupOptionsStep, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(pickupReminderDaysBefore = 0) }
        vm.nextStep()
        assertEquals(vm.uiState.value.pickupContextStep, vm.uiState.value.currentStep)
        vm.nextStep()
        assertEquals(vm.uiState.value.optionalDetailsStep, vm.uiState.value.currentStep)
        vm.nextStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        vm.goToStep(2)
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = false) }
        vm.nextStep()
        assertEquals(4, vm.uiState.value.maxSteps)
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals("Losartana", vm.uiState.value.medications.single().title)
    }
    @Test fun recipeDecisionsValidateTheirOwnStepAndOptionalDetailsReturnToReview() = runTest {
        val vm = model(); runCurrent()
        vm.updateForm { it.copy(validityDays = null, validityDaysInput = "") }
        vm.nextStep() // The optional name does not require dates or validity.
        assertEquals(vm.uiState.value.recipeIdentityStep, vm.uiState.value.currentStep)
        vm.nextStep() // Type and validity must be configured before leaving identity.
        assertEquals(vm.uiState.value.recipeIdentityStep, vm.uiState.value.currentStep)
        vm.nextStep()
        assertNotNull(vm.uiState.value.errorMessage)
        assertEquals(vm.uiState.value.recipeIdentityStep, vm.uiState.value.currentStep)
        vm.openTypeEditor(); vm.updateValidityDays("180"); vm.confirmTypeEditor(); vm.nextStep()
        assertEquals(vm.uiState.value.medicationStep, vm.uiState.value.currentStep)
        vm.updateForm { it.copy(currentMedicationDraft = MedicationDraft(title = "Losartana")) }
        vm.nextStep(); vm.nextStep(); vm.nextStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        vm.goToStep(requireNotNull(vm.uiState.value.recipeDetailsStep))
        vm.updateForm { it.copy(notes = "Observação preservada", doctorName = "Dra. Ana") }
        vm.nextStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals("Observação preservada", vm.uiState.value.notes)
        assertFalse(vm.uiState.value.medications.single().linkToCurrentRecipe)
        assertNull(vm.uiState.value.medications.single().linkedPrescriptionId)
    }

    @Test fun customTypePageCancelsAtomicallyAndKeepsEnteredValue() = runTest {
        val vm = model(); runCurrent()
        vm.goToStep(2); vm.updateValidityDays("75")
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        assertEquals("75", vm.uiState.value.typeEditor!!.validityDaysInput)
        vm.updateExpirationDate(vm.uiState.value.issueDate.plusDays(45))
        vm.previousStep()
        assertNull(vm.uiState.value.typeEditor)
        assertEquals(2, vm.uiState.value.currentStep)
        assertEquals(75, vm.uiState.value.validityDays)
        assertEquals(PrescriptionType.CONTINUO, vm.uiState.value.type)
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        vm.updateValidityDays("90"); vm.confirmTypeEditor()
        assertEquals(vm.uiState.value.renewalOptionsStep, vm.uiState.value.currentStep)
        assertEquals(90, vm.uiState.value.validityDays)
        assertEquals(PrescriptionType.PERSONALIZADO, vm.uiState.value.type)
    }

    @Test fun presetTypeAppliesSuggestedDaysAndAdvancesOrReturnsToReview() = runTest {
        val vm = model(); runCurrent(); vm.goToStep(2); vm.updateValidityDays("75")
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.ANTIBIOTICO)
        assertNull(vm.uiState.value.typeEditor)
        assertEquals(10, vm.uiState.value.validityDays)
        assertEquals("SUGGESTED", vm.uiState.value.validityOrigin)
        assertEquals(vm.uiState.value.renewalOptionsStep, vm.uiState.value.currentStep)
        add(vm, "Losartana"); vm.goToStep(vm.uiState.value.maxSteps)
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.CONTROLADO)
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals(30, vm.uiState.value.validityDays)
        vm.goToStep(2); vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.CONTINUO)
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertNull(vm.uiState.value.reviewEditStep)
        assertEquals(180, vm.uiState.value.validityDays)
    }

    @Test fun invalidTypePageStaysOpenAndReviewReturnsWithoutExtraStep() = runTest {
        val vm = model(); runCurrent(); add(vm, "Losartana"); vm.goToStep(vm.uiState.value.maxSteps)
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO); vm.confirmTypeEditor()
        assertNotNull(vm.uiState.value.typeEditor)
        assertNotNull(vm.uiState.value.errorMessage)
        assertEquals(180, vm.uiState.value.validityDays)
        vm.savePrescription(); runCurrent(); assertEquals(0, registration.attempts)
        vm.setValidityDateMode(true)
        vm.updateExpirationDate(vm.uiState.value.issueDate.minusDays(1)); vm.confirmTypeEditor()
        assertNotNull(vm.uiState.value.typeEditor)
        vm.updateExpirationDate(vm.uiState.value.issueDate.plusDays(2)); vm.confirmTypeEditor()
        assertNull(vm.uiState.value.typeEditor)
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertTrue(vm.uiState.value.validityDateMode)
        assertEquals(2, vm.uiState.value.renewalReminderDaysBefore)
        vm.savePrescription(); runCurrent()
        assertEquals(2, registration.recipe!!.validityDurationDays)
    }

    @Test fun changingIssueDatePreservesExplicitExpiryButKeepsDurationForDaysMode() = runTest {
        val vm = model(); runCurrent()
        val issue = LocalDate.now().minusDays(20)
        vm.updateIssueDate(issue)
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        val expiry = issue.plusDays(60)
        vm.updateExpirationDate(expiry); vm.confirmTypeEditor()
        vm.updateIssueDate(issue.plusDays(10))
        assertEquals(50, vm.uiState.value.validityDays)
        assertEquals(expiry, vm.uiState.value.issueDate.plusDays(vm.uiState.value.validityDays!!.toLong()))
        vm.openTypeEditor(); vm.setValidityDateMode(false); vm.updateValidityDays("30"); vm.confirmTypeEditor()
        vm.updateIssueDate(issue.plusDays(15))
        assertEquals(30, vm.uiState.value.validityDays)
        vm.openTypeEditor(); vm.updateExpirationDate(expiry); vm.confirmTypeEditor()
        vm.updateIssueDate(expiry)
        assertNotNull(vm.uiState.value.recipeError)
        assertFalse(vm.uiState.value.canSave)
    }

    @Test fun pendingTypePageRestoresWithoutChangingCommittedValues() = runTest {
        val handle = SavedStateHandle()
        val vm = model(handle); runCurrent(); vm.goToStep(2); vm.openTypeEditor()
        vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO); vm.updateValidityDays("45"); vm.setValidityDateMode(true); runCurrent()
        val snapshot = handle.get<PrescriptionWizardUiState>("registration_draft")!!
        val bytes = ByteArrayOutputStream().also { ObjectOutputStream(it).use { out -> out.writeObject(snapshot) } }.toByteArray()
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as PrescriptionWizardUiState }
        val recreated = model(SavedStateHandle(mapOf("registration_draft" to restored))); runCurrent()
        assertEquals("45", recreated.uiState.value.typeEditor!!.validityDaysInput)
        assertTrue(recreated.uiState.value.typeEditor!!.useExpirationDate)
        assertEquals(180, recreated.uiState.value.validityDays)
        recreated.cancelTypeEditor()
        assertEquals(PrescriptionType.CONTINUO, recreated.uiState.value.type)
        assertEquals(180, recreated.uiState.value.validityDays)
    }

    @Test fun recipeNameIsRequiredBeforeAdvancingAndSaving() = runTest {
        val vm = model(); runCurrent()
        vm.updateForm { it.copy(prescriptionTitle = "   ") }
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.nextStep()
        assertEquals(1, vm.uiState.value.currentStep)
        assertEquals("Informe o nome da receita.", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.canSave)
        vm.savePrescription(); runCurrent()
        assertEquals(0, registration.attempts)
        vm.updateForm { it.copy(prescriptionTitle = "Acompanhamento") }
        vm.nextStep()
        assertEquals(vm.uiState.value.recipeIdentityStep, vm.uiState.value.currentStep)
    }

    @Test fun customDateStartsWithAValidDateWithoutReplacingAnExistingDeadline() = runTest {
        val vm = model(); runCurrent()
        vm.openTypeEditor(); vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        vm.setValidityDateMode(true)
        val initial = requireNotNull(vm.uiState.value.typeEditor)
        assertTrue(requireNotNull(initial.validityDays) in 1..3650)
        vm.updateValidityDays("45"); vm.setValidityDateMode(true)
        assertEquals(45, vm.uiState.value.typeEditor!!.validityDays)
    }

    @Test fun optionalStepsSkipWithoutClearingEnteredValues() = runTest {
        val vm = model(); runCurrent()
        vm.updateForm { it.copy(prescriptionTitle = "Minha receita", doctorName = "Dra. Ana", notes = "Preservar observação", renewalReminderEnabled = false, renewalReminderDaysBefore = 7) }
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.nextStep(); assertEquals(2, vm.uiState.value.currentStep)
        vm.skipStep(); assertEquals(2, vm.uiState.value.currentStep)
        vm.nextStep(); assertEquals(3, vm.uiState.value.currentStep)
        vm.skipStep(); assertEquals(4, vm.uiState.value.currentStep)
        vm.skipStep(); assertEquals(5, vm.uiState.value.currentStep)
        vm.skipStep(); assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        vm.previousStep(); assertEquals(5, vm.uiState.value.currentStep)
        add(vm, "Losartana"); vm.nextStep()
        vm.updateMedicationPickup(0) { it.copy(replenishmentQuantity = "2 caixas") }
        vm.skipStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals("Minha receita", vm.uiState.value.prescriptionTitle)
        assertEquals("Dra. Ana", vm.uiState.value.doctorName)
        assertEquals("Preservar observação", vm.uiState.value.notes)
        assertFalse(vm.uiState.value.renewalReminderEnabled)
        assertEquals(7, vm.uiState.value.renewalReminderDaysBefore)
        assertEquals("2 caixas", vm.uiState.value.medications.single().replenishmentQuantity)
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.goToStep(4); vm.skipStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals("Preservar observação", vm.uiState.value.notes)
    }

    @Test fun recurringScheduleAndInvalidPreferencesCannotBeSkipped() = runTest {
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION"))); runCurrent()
        add(vm, "Losartana"); vm.nextStep()
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true) }
        vm.skipStep(); assertEquals(2, vm.uiState.value.currentStep)
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.updateMedicationPickup(0) { it.copy(lastPickupDate = LocalDate.now(), pickupIntervalDays = 1, pickupReminderDaysBefore = 3) }
        vm.nextStep(); assertEquals(3, vm.uiState.value.currentStep)
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.skipStep(); assertEquals(3, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(pickupReminderDaysBefore = 0) }
        vm.skipStep(); assertEquals(4, vm.uiState.value.currentStep)
        vm.updateMedicationPickup(0) { it.copy(sourceName = "UBS Central", linkedPrescriptionId = "escolhida") }
        vm.skipStep(); assertEquals(5, vm.uiState.value.currentStep)
        assertEquals("UBS Central", vm.uiState.value.medications.single().sourceName)
        assertEquals("escolhida", vm.uiState.value.medications.single().linkedPrescriptionId)
    }

    @Test fun skipNeverBypassesCustomValidityOrInvalidEnabledAlerts() = runTest {
        val vm = model(); runCurrent(); vm.goToStep(2); vm.openTypeEditor()
        vm.selectPrescriptionType(PrescriptionType.PERSONALIZADO)
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.skipStep(); assertNotNull(vm.uiState.value.typeEditor)
        vm.confirmTypeEditor(); assertNotNull(vm.uiState.value.typeEditor)
        vm.cancelTypeEditor(); vm.goToStep(3)
        vm.updateForm { it.copy(renewalReminderDaysBefore = 999) }
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.skipStep(); assertEquals(3, vm.uiState.value.currentStep)
        assertFalse(PrescriptionWizardUiState(isLoading = true).canSkipCurrentStep)
        assertFalse(PrescriptionWizardUiState(initialLoadFailed = true).canSkipCurrentStep)
        assertFalse(PrescriptionWizardUiState(isSaved = true).canSkipCurrentStep)
    }

    @Test fun reviewCanEditPickupContextWithoutRepeatingReminderStep() = runTest {
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION"))); runCurrent()
        add(vm, "Losartana"); vm.nextStep()
        vm.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true, lastPickupDate = LocalDate.now()) }
        repeat(4) { vm.nextStep() }
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        vm.goToStep(requireNotNull(vm.uiState.value.pickupContextStep))
        vm.updateMedicationPickup(0) { it.copy(sourceName = "UBS Central") }
        vm.nextStep()
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertEquals("UBS Central", vm.uiState.value.medications.single().sourceName)
        assertEquals(3, vm.uiState.value.medications.single().pickupReminderDaysBefore)
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
    @Test fun renewalLoadsANewIdAndLetsUserContinueTrackingExplicitly() = runTest {
        recipes.items.value = listOf(PrescriptionItem("old", "Receita antiga", "Clínica", doctorName = "Profissional antigo", medicationIds = listOf("m"), items = listOf(PrescriptionMedication("m", "Pela manhã"))))
        meds.items.value = listOf(MedicationItem("m", "Losartana", lastRefillDate = LocalDate.now().minusDays(30), hasRecurringPickup = true, linkedPrescriptionId = "old"))
        val vm = model(SavedStateHandle(mapOf("renewalOf" to "old"))); runCurrent()
        assertNull(vm.uiState.value.editingPrescriptionId)
        assertNotEquals("old", vm.uiState.value.registrationId)
        assertEquals("old", vm.uiState.value.previousPrescriptionId)
        assertEquals("Profissional antigo", vm.uiState.value.doctorName)
        vm.updateMedicationPickup(0) { it.copy(linkToCurrentRecipe = true, linkedPrescriptionId = null) }
        vm.savePrescription(); runCurrent()
        assertEquals("old", registration.recipe!!.previousPrescriptionId)
        assertEquals(registration.recipe!!.id, registration.medications.single().linkedPrescriptionId)
    }
    @Test fun emptyRecipeSkipsPickupAndSavesWithAutomaticName() = runTest {
        val vm = model(); runCurrent()
        repeat(3) { vm.nextStep() }
        assertTrue(vm.uiState.value.canSkipCurrentStep)
        vm.skipStep()
        assertEquals(vm.uiState.value.optionalDetailsStep, vm.uiState.value.currentStep)
        vm.skipStep()
        assertEquals(6, vm.uiState.value.maxSteps)
        assertEquals(vm.uiState.value.maxSteps, vm.uiState.value.currentStep)
        assertTrue(vm.uiState.value.canSave)
        vm.previousStep(); assertEquals(5, vm.uiState.value.currentStep)
        vm.nextStep(); vm.savePrescription(); runCurrent()
        assertTrue(vm.uiState.value.isSaved)
        assertTrue(registration.recipe!!.title == "Receita de teste")
        assertTrue(registration.recipe!!.medicationIds.isEmpty())
        assertTrue(registration.medications.isEmpty())
    }

    @Test fun skipMedicationPreservesCompleteDraftAndRejectsPartialDraft() = runTest {
        val vm = model(); runCurrent(); vm.goToStep(vm.uiState.value.medicationStep)
        vm.updateForm { it.copy(currentMedicationDraft = MedicationDraft(dosageOverride = "50 mg")) }
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.skipStep(); vm.nextStep()
        assertEquals(vm.uiState.value.medicationStep, vm.uiState.value.currentStep)
        assertEquals("50 mg", vm.uiState.value.currentMedicationDraft.dosage)
        vm.cancelMedicationEditor()
        assertEquals(MedicationDraft(), vm.uiState.value.currentMedicationDraft)
        assertTrue(vm.uiState.value.canSkipCurrentStep)
        vm.updateForm { it.copy(currentMedicationDraft = MedicationDraft(title = "Losartana", dosageOverride = "50 mg")) }
        vm.skipStep()
        assertEquals(vm.uiState.value.pickupStep, vm.uiState.value.currentStep)
        assertEquals("Losartana", vm.uiState.value.medications.single().title)
        assertEquals("50 mg", vm.uiState.value.medications.single().dosage)
        vm.skipStep(); vm.savePrescription(); runCurrent()
        assertEquals(1, registration.recipe!!.medicationIds.size)
    }

    @Test fun editingRecipeCanRemoveLastMedicationWithoutErasingItsName() = runTest {
        meds.items.value = listOf(MedicationItem("m", "Losartana"))
        recipes.items.value = listOf(PrescriptionItem("p", "Minha receita", "", medicationIds = listOf("m")))
        val vm = model(SavedStateHandle(mapOf("prescriptionId" to "p"))); runCurrent()
        vm.goToStep(5); vm.removeMedication(0); vm.skipStep()
        assertEquals(6, vm.uiState.value.currentStep)
        assertTrue(vm.uiState.value.canSave)
        vm.savePrescription(); runCurrent()
        assertEquals("p", registration.recipe!!.id)
        assertEquals("Minha receita", registration.recipe!!.title)
        assertTrue(registration.medications.isEmpty())
    }

    @Test fun legacyIntervalsShowEquivalentDaysWithoutChangingSavedCalendarSchedule() = runTest {
        val date = LocalDate.of(2026, 1, 31)
        listOf(IntervalUnit.WEEKS to 7, IntervalUnit.MONTHS to 28, IntervalUnit.YEARS to 365).forEach { (unit, days) ->
            meds.items.value = listOf(MedicationItem("m", "Medicamento", hasRecurringPickup = true, lastRefillDate = date,
                refillCycleDays = 1, intervalUnit = unit))
            val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION", "medicationId" to "m"))); runCurrent()
            assertEquals(days.toString(), vm.uiState.value.medications.single().pickupIntervalDisplayInput)
            vm.savePrescription(); runCurrent()
            assertEquals(unit, registration.medications.single().intervalUnit)
            assertEquals(1, registration.medications.single().refillCycleDays)
            assertEquals(PickupInterval(1, unit).nextAfter(date), registration.medications.single().nextAvailableRefillDate)
        }
    }

    @Test fun editingLegacyIntervalChangesOnlyTheIntervalToDaysAndKeepsHistoryAndLink() = runTest {
        val date = LocalDate.of(2026, 1, 31)
        meds.items.value = listOf(MedicationItem("m", "Medicamento", hasRecurringPickup = true, lastRefillDate = date,
            refillCycleDays = 1, intervalUnit = IntervalUnit.MONTHS, linkedPrescriptionId = "chosen", pickupHistory = listOf(PickupEvent("e", date, 1L))))
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION", "medicationId" to "m"))); runCurrent()
        vm.updatePickupInterval(0, "45")
        assertEquals(IntervalUnit.DAYS, vm.uiState.value.medications.single().intervalUnit)
        assertEquals("45", vm.uiState.value.medications.single().pickupIntervalDisplayInput)
        assertTrue(vm.uiState.value.medications.single().hasPickupHistory)
        assertEquals(date, vm.uiState.value.medications.single().lastPickupDate)
        vm.savePrescription(); runCurrent()
        assertEquals(45, registration.medications.single().refillCycleDays)
        assertEquals(date.plusDays(45), registration.medications.single().nextAvailableRefillDate)
        assertEquals("chosen", registration.medications.single().linkedPrescriptionId)
        val invalid = model(); runCurrent(); add(invalid, "Medicamento")
        invalid.updateMedicationPickup(0) { it.copy(hasRecurringPickup = true, lastPickupDate = date) }
        listOf("", "0", "3651").forEach { input ->
            invalid.updatePickupInterval(0, input)
            assertNotNull(invalid.uiState.value.pickupError)
        }
    }

    @Test fun recipeNotesSaveWithoutProfessionalAndRemainWhenProfessionalChanges() = runTest {
        val vm = model(); runCurrent(); vm.goToStep(4)
        vm.updateForm { it.copy(notes = "Observação pertencente à receita") }
        vm.skipStep(); vm.skipStep(); vm.savePrescription(); runCurrent()
        assertEquals("Observação pertencente à receita", registration.recipe!!.notes)
        assertEquals("", registration.recipe!!.doctorName)
        assertNull(registration.recipe!!.doctorId)
        recipes.items.value = listOf(registration.recipe!!)
        val edit = model(SavedStateHandle(mapOf("prescriptionId" to registration.recipe!!.id))); runCurrent()
        edit.selectDoctor(Doctor("doctor", "Outro profissional"))
        assertEquals("Observação pertencente à receita", edit.uiState.value.notes)
        edit.savePrescription(); runCurrent()
        assertEquals("Observação pertencente à receita", registration.recipe!!.notes)
    }

    @Test fun standaloneStillRequiresMedication() = runTest {
        val vm = model(SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION"))); runCurrent()
        assertFalse(vm.uiState.value.canSkipCurrentStep)
        vm.nextStep(); vm.savePrescription(); runCurrent()
        assertEquals(1, vm.uiState.value.currentStep)
        assertEquals(0, registration.attempts)
    }

    @Test fun addingFromRecipePreservesContextAndAutomaticallyUsesOriginForPickups() = runTest {
        recipes.items.value = listOf(PrescriptionItem("p", "Minha receita", "", medicationIds = listOf("existing")))
        val handle = SavedStateHandle(mapOf("entryMode" to "STANDALONE_MEDICATION", "addToPrescriptionId" to "p"))
        val vm = model(handle); runCurrent()
        assertEquals("Minha receita", vm.uiState.value.addToPrescriptionTitle)
        assertEquals("p", vm.uiState.value.currentMedicationDraft.linkedPrescriptionId)
        assertEquals(listOf("existing"), vm.uiState.value.existingPrescriptionMedicationIds)
        add(vm, "Novo medicamento"); runCurrent()
        vm.updateMedicationPickup(0) { it.copy(linkedPrescriptionId = null) }
        assertEquals("p", vm.uiState.value.medications.single().linkedPrescriptionId)
        val restored = model(handle); runCurrent()
        restored.savePrescription(); runCurrent()
        assertEquals("p", registration.destination)
        assertNull(registration.recipe)
        assertEquals("p", registration.medications.single().linkedPrescriptionId)
        assertTrue(restored.uiState.value.isSaved)
    }

    @Test fun missingOrReplacedDestinationCannotSaveMedication() = runTest {
        val args = mapOf("entryMode" to "STANDALONE_MEDICATION", "addToPrescriptionId" to "p")
        val missing = model(SavedStateHandle(args)); runCurrent()
        assertTrue(missing.uiState.value.initialLoadFailed)
        missing.savePrescription(); runCurrent(); assertEquals(0, registration.attempts)
        recipes.items.value = listOf(PrescriptionItem("p", "Histórico", "", state = RecordState.REPLACED))
        val replaced = model(SavedStateHandle(args)); runCurrent()
        assertTrue(replaced.uiState.value.initialLoadFailed)
        recipes.items.value = listOf(PrescriptionItem("p", "Arquivada", "", state = RecordState.ARCHIVED))
        val archived = model(SavedStateHandle(args)); runCurrent()
        assertTrue(archived.uiState.value.initialLoadFailed)
    }

    private class Registration : PrescriptionRegistrationRepository {
        var destination: String? = null
        override suspend fun addToPrescription(prescriptionId: String, medications: List<MedicationItem>) {
            destination = prescriptionId
            save(null, medications, null)
        }
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
        override suspend fun correctPickup(eventId: String, date: LocalDate) = OperationResult.NotFound
        override suspend fun deletePickup(eventId: String) = OperationResult.NotFound
        override suspend fun archiveTracking(id: String, archived: Boolean) = OperationResult.NotFound
        override suspend fun deleteMedication(id: String) = OperationResult.NotFound
        override suspend fun registerPickup(id: String, date: LocalDate, confirmedEarly: Boolean, expectedCycle: LocalDate?) = OperationResult.Success
        override suspend fun addMedication(medication: MedicationItem) {}
        override suspend fun updateMedication(medication: MedicationItem) {}
    }
    private class Recipes : PrescriptionRepository {
        val items = MutableStateFlow<List<PrescriptionItem>>(emptyList())
        override fun getPrescriptions(): Flow<List<PrescriptionItem>> = items
        override suspend fun getPrescriptionById(id: String) = items.value.find { it.id == id }
        override suspend fun addPrescription(prescription: PrescriptionItem) {}
        override suspend fun updatePrescription(prescription: PrescriptionItem) {}
        override suspend fun deletePrescription(id: String) {}
        override suspend fun archivePrescription(id: String, archived: Boolean) {}
    }
    private class Doctors : DoctorRepository {
        override fun getDoctors(): Flow<List<Doctor>> = flowOf(listOf(Doctor("suggestion", "Profissional sugerido")))
        override suspend fun getDoctorById(id: String): Doctor? = null
        override suspend fun addDoctor(doctor: Doctor) {}
        override suspend fun updateDoctor(doctor: Doctor) {}
    }
}
