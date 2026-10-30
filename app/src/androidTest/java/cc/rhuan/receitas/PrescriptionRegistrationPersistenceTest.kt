package cc.rhuan.receitas

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.repository.*
import cc.rhuan.receitas.domain.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.time.LocalDate

class PrescriptionRegistrationPersistenceTest {
    private lateinit var db: AppDatabase
    private lateinit var medications: RoomMedicationRepository
    private lateinit var recipes: RoomPrescriptionRepository
    private lateinit var doctors: RoomDoctorRepository
    private val today = LocalDate.now()
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        medications = RoomMedicationRepository(TestTexts, db)
        recipes = RoomPrescriptionRepository(TestTexts, db)
        doctors = RoomDoctorRepository(db.doctorDao())
    }
    @After fun close() { db.close() }
    private fun registration() = RoomPrescriptionRegistrationRepository(TestTexts, db, recipes, medications, doctors)
    private fun med() = MedicationItem("m", "Losartana", hasRecurringPickup = true, lastRefillDate = today.minusDays(30), quantity = "2 caixas", prescribedPosology = "Pela manhã", linkedPrescriptionId = "p")
    private fun recipe() = PrescriptionItem("p", "Receita", "", medicationIds = listOf("m"), renewalReminderEnabled = false, renewalReminderDaysBefore = 9)
    @Test fun emptyRecipeCanReceiveItemsWithoutReplacingExistingSnapshotsOrTracking() = runBlocking {
        val registration = registration()
        registration.save(recipe().copy(medicationIds = emptyList()), emptyList(), null)
        assertTrue(recipes.getPrescriptionById("p")!!.medicationIds.isEmpty())
        val first = med().copy(linkedPrescriptionId = null)
        registration.addToPrescription("p", listOf(first))
        val snapshot = recipes.getPrescriptionById("p")!!.items.single()
        val tracking = medications.getMedicationById("m")!!
        assertEquals("p", tracking.linkedPrescriptionId)
        registration.addToPrescription("p", listOf(MedicationItem("m2", "Segundo", prescribedQuantity = "2 caixas")))
        val saved = recipes.getPrescriptionById("p")!!
        assertEquals(setOf("m", "m2"), saved.medicationIds.toSet())
        assertEquals(snapshot, saved.items.single { it.medicationId == "m" })
        assertEquals(tracking.pickupHistory, medications.getMedicationById("m")!!.pickupHistory)
        assertEquals(tracking.nextAvailableRefillDate, medications.getMedicationById("m")!!.nextAvailableRefillDate)
        assertEquals("2 caixas", saved.items.single { it.medicationId == "m2" }.quantity)
        try { registration.addToPrescription("p", listOf(first)); fail("Esperava duplicado") } catch (_: BusinessRuleViolation) {}
        assertEquals(2, recipes.getPrescriptionById("p")!!.items.size)
    }

    @Test fun appendAutomaticallyUsesOriginForPickupsAndKeepsHistory() = runBlocking {
        registration().save(recipe().copy(medicationIds = emptyList()), emptyList(), null)
        val date = LocalDate.of(2026, 1, 31)
        medications.addMedication(MedicationItem("legacy", "Medicamento mensal", hasRecurringPickup = true,
            lastRefillDate = date, refillCycleDays = 1, intervalUnit = IntervalUnit.MONTHS))
        val legacy = medications.getMedicationById("legacy")!!
        registration().addToPrescription("p", listOf(legacy.copy(refillCycleDays = 45, intervalUnit = IntervalUnit.DAYS)))
        val saved = medications.getMedicationById("legacy")!!
        assertEquals("p", saved.linkedPrescriptionId)
        assertEquals(listOf("legacy"), recipes.getPrescriptionById("p")!!.medicationIds)
        assertEquals(legacy.pickupHistory, saved.pickupHistory)
        assertEquals(date.plusDays(45), saved.nextAvailableRefillDate)
    }

    @Test fun addingToMissingReplacedOrInvalidTrackingRollsBackNewProductsAndMembership() = runBlocking {
        val registration = registration()
        registration.save(recipe().copy(medicationIds = emptyList()), emptyList(), null)
        try { registration.addToPrescription("p", listOf(med().copy(lastRefillDate = today.plusDays(1)))); fail("Esperava validação") } catch (_: BusinessRuleViolation) {}
        assertTrue(recipes.getPrescriptionById("p")!!.items.isEmpty())
        assertNull(medications.getMedicationById("m"))
        try { registration.addToPrescription("missing", listOf(med())); fail("Esperava receita") } catch (_: BusinessRuleViolation) {}
        val old = db.prescriptionDao().getPrescriptionById("p")!!
        db.prescriptionDao().updatePrescription(old.copy(state = "ARCHIVED"))
        try { registration.addToPrescription("p", listOf(med())); fail("Esperava receita ativa") } catch (_: BusinessRuleViolation) {}
        db.prescriptionDao().updatePrescription(old.copy(state = "REPLACED"))
        try { registration.addToPrescription("p", listOf(med())); fail("Esperava histórico") } catch (_: BusinessRuleViolation) {}
        assertNull(medications.getMedicationById("m"))
        try { registration.save(null, emptyList(), null); fail("Esperava medicamento avulso") } catch (_: BusinessRuleViolation) {}
    }

    @Test fun recipeNotesDoNotRequireProfessionalAndAreKeptOnDoctorChanges() = runBlocking {
        val note = "Observações gerais da receita"
        val original = recipe().copy(medicationIds = emptyList(), notes = note)
        registration().save(original, emptyList(), null)
        assertEquals(note, recipes.getPrescriptionById("p")!!.notes)
        assertNull(recipes.getPrescriptionById("p")!!.doctorId)
        assertTrue(doctors.getDoctors().first().isEmpty())
        registration().save(original.copy(doctorId = "d", doctorName = "Profissional"), emptyList(), Doctor("d", "Profissional"))
        doctors.updateDoctor(Doctor("d", "Nome atualizado"))
        assertEquals(note, recipes.getPrescriptionById("p")!!.notes)
    }

    @Test fun reminderPreferencesAndDisabledTrackingPreserveHistory() = runBlocking {
        registration().save(recipe(), listOf(med().copy(pickupReminderDaysBefore = 7)), null)
        assertFalse(recipes.getPrescriptionById("p")!!.renewalReminderEnabled)
        assertEquals(9, recipes.getPrescriptionById("p")!!.renewalReminderDaysBefore)
        assertEquals(7, medications.getMedicationById("m")!!.pickupReminderDaysBefore)
        registration().save(recipe(), listOf(med().copy(hasRecurringPickup = false, lastRefillDate = null)), null)
        val m = medications.getMedicationById("m")!!
        assertFalse(m.hasRecurringPickup)
        assertNull(m.nextAvailableRefillDate)
        assertEquals(1, m.pickupHistory.size)
    }
    @Test fun sameMedicationHasIndependentPrescriptionInstructions() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        registration().save(recipe().copy(id = "p2"), listOf(med().copy(prescribedPosology = "À noite", dosage = "100 mg")), null)
        assertEquals("Pela manhã", recipes.getPrescriptionById("p")!!.items.single().posology)
        assertEquals("À noite", recipes.getPrescriptionById("p2")!!.items.single().posology)
        assertEquals("", recipes.getPrescriptionById("p")!!.items.single().dosage)
        assertEquals("100 mg", recipes.getPrescriptionById("p2")!!.items.single().dosage)
    }
    @Test fun retroactivePickupDoesNotRegressLatestAndCorrectionRecalculates() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        assertEquals(OperationResult.Success, medications.registerPickup("m", today))
        val next = today.plusDays(30)
        assertEquals(OperationResult.Success, medications.registerPickup("m", today.minusDays(60)))
        val afterRetroactivePickup = medications.getMedicationById("m")!!
        assertEquals(next, afterRetroactivePickup.nextAvailableRefillDate)
        assertEquals(1, afterRetroactivePickup.pickupHistory.count { it.occurredOn == today.minusDays(60) })
        assertEquals(OperationResult.AlreadyRecorded, medications.registerPickup("m", today))
        assertEquals(OperationResult.InvalidDate, medications.registerPickup("m", today.plusDays(1), true))
        assertEquals(OperationResult.ObsoleteCycle, medications.registerPickup("m", today.minusDays(1), true, today))
        val latest = medications.getMedicationById("m")!!.pickupHistory.first()
        assertEquals(OperationResult.Success, medications.correctPickup(latest.id, today.minusDays(2)))
        assertEquals(today.plusDays(28), medications.getMedicationById("m")!!.nextAvailableRefillDate)
        medications.deletePickup(latest.id)
        assertEquals(today, medications.getMedicationById("m")!!.nextAvailableRefillDate)
    }
    @Test fun actualEarlyPickupRequiresAdditionalConfirmation() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        assertEquals(OperationResult.ConfirmationRequired, medications.registerPickup("m", today.minusDays(1)))
        assertEquals(1, medications.getMedicationById("m")!!.pickupHistory.size)
        assertEquals(OperationResult.Success, medications.registerPickup("m", today.minusDays(1), true))
        assertEquals(today.plusDays(29), medications.getMedicationById("m")!!.nextAvailableRefillDate)
    }
    @Test fun renewalPreservesPreviousRecipeDoctorAndPickupCycle() = runBlocking {
        registration().save(recipe().copy(doctorId = "d", doctorName = "Profissional anterior", specialty = "Clínica", notes = "Observações antigas"), listOf(med()), Doctor("d", "Profissional anterior"))
        val next = medications.getMedicationById("m")!!.nextAvailableRefillDate
        registration().save(recipe().copy(id = "new", previousPrescriptionId = "p", doctorId = "d2", doctorName = "Novo profissional"), listOf(med().copy(linkedPrescriptionId = "new")), Doctor("d2", "Novo profissional"))
        val old = recipes.getPrescriptionById("p")!!
        assertEquals(RecordState.REPLACED, old.state)
        assertEquals("Profissional anterior", old.doctorName)
        assertEquals("Observações antigas", old.notes)
        assertEquals(next, medications.getMedicationById("m")!!.nextAvailableRefillDate)
        assertEquals("new", medications.getMedicationById("m")!!.linkedPrescriptionId)
        val renewed = recipes.getPrescriptionById("new")!!
        registration().save(renewed.copy(notes = "Anotação nova"), listOf(medications.getMedicationById("m")!!), null)
        assertEquals("p", recipes.getPrescriptionById("new")!!.previousPrescriptionId)
        doctors.updateDoctor(Doctor("d", "Nome corrigido no cadastro"))
        assertEquals("Profissional anterior", recipes.getPrescriptionById("p")!!.doctorName)
    }
    @Test fun archiveStopsTrackingAndDeletionPreservesEvents() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        recipes.archivePrescription("p", true)
        assertTrue(medications.getMedicationById("m")!!.trackingArchived)
        assertEquals(OperationResult.Inactive, medications.registerPickup("m", today))
        recipes.deletePrescription("p")
        val m = medications.getMedicationById("m")!!
        assertNull(m.linkedPrescriptionId)
        assertEquals(1, m.pickupHistory.size)
        assertEquals(OperationResult.Success, medications.archiveTracking("m", false))
    }
    @Test fun failureRollsBackDoctorMedicationsAndRecipeThenRetryUsesSameIds() = runBlocking {
        val failingRecipes = object : PrescriptionRepository by recipes {
            override suspend fun addPrescription(prescription: PrescriptionItem) { recipes.addPrescription(prescription); error("Falha após escrever a receita") }
        }
        val recipe = recipe().copy(doctorId = "d")
        val doctor = Doctor("d", "Profissional teste")
        try { RoomPrescriptionRegistrationRepository(TestTexts, db, failingRecipes, medications, doctors).save(recipe, listOf(med()), doctor); fail("Esperava falha") } catch (_: IllegalStateException) {}
        assertTrue(db.prescriptionDao().getAllPrescriptions().first().isEmpty())
        assertTrue(db.medicationDao().getAllMedications().first().isEmpty())
        assertTrue(doctors.getDoctors().first().isEmpty())
        assertTrue(db.trackingDao().observeEvents().first().isEmpty())
        registration().save(recipe, listOf(med()), doctor)
        assertEquals(listOf("p"), db.prescriptionDao().getAllPrescriptions().first().map { it.id })
    }
    @Test fun savingConfigurationDoesNotRecreateADeletedPickup() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        medications.registerPickup("m", today)
        val stale = medications.getMedicationById("m")!!
        medications.deletePickup(stale.pickupHistory.first().id)
        medications.updateMedication(stale.copy(quantity = "3 caixas"))
        val current = medications.getMedicationById("m")!!
        assertEquals(1, current.pickupHistory.size)
        assertEquals(today.minusDays(30), current.lastRefillDate)
        assertEquals("3 caixas", current.quantity)
    }
    @Test fun archivedRecipeCanBeEditedWithoutReactivatingTracking() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        recipes.archivePrescription("p", true)
        val archived = recipes.getPrescriptionById("p")!!
        registration().save(archived.copy(notes = "Nota do histórico"), listOf(medications.getMedicationById("m")!!), null)
        assertEquals(RecordState.ARCHIVED, recipes.getPrescriptionById("p")!!.state)
        assertTrue(medications.getMedicationById("m")!!.trackingArchived)
    }
    @Test fun trackingCannotBeReactivatedWithAReplacedRecipe() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        registration().save(recipe().copy(id = "new", previousPrescriptionId = "p"), listOf(med()), null)
        assertEquals(OperationResult.InvalidPrescription, medications.archiveTracking("m", false))
        val old = medications.getMedicationById("m")!!
        medications.updateMedication(old.copy(quantity = "3 caixas"))
        assertTrue(medications.getMedicationById("m")!!.trackingArchived)
    }
    @Test fun invalidIntervalIsVisibleButDoesNotGenerateAFalseSchedule() = runBlocking {
        registration().save(recipe(), listOf(med()), null)
        val track = db.trackingDao().tracking("m")!!
        db.trackingDao().updateTracking(track.copy(intervalUnit = "UNSUPPORTED"))
        val current = medications.getMedicationById("m")!!
        assertNotNull(current.trackingError)
        assertFalse(current.hasRecurringPickup)
        assertNull(current.nextAvailableRefillDate)
    }
    @Test fun invalidExplicitRecipeRollsBackEntireRegistration() = runBlocking {
        try { registration().save(recipe(), listOf(med().copy(linkedPrescriptionId = "missing")), null); fail("Esperava validação") } catch (_: BusinessRuleViolation) {}
        assertTrue(recipes.getPrescriptions().first().isEmpty())
        assertTrue(medications.getMedications().first().isEmpty())
    }
}
