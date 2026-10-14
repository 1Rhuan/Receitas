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
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        medications = RoomMedicationRepository(db.medicationDao())
        recipes = RoomPrescriptionRepository(db.prescriptionDao(), db.doctorDao())
        doctors = RoomDoctorRepository(db.doctorDao())
    }
    @After fun close() { db.close() }
    @Test fun customRemindersRoundTripAndPickupCanBeDisabled() = runBlocking {
        val registration = RoomPrescriptionRegistrationRepository(db, recipes, medications, doctors)
        val med = MedicationItem("m", "Losartana", lastRefillDate = LocalDate.now(), hasRecurringPickup = true, pickupReminderDaysBefore = 7, activeIngredient = "Princípio ativo", prescribedPosology = "Orientação", sourceName = "UBS", instructions = "Observação")
        val recipe = PrescriptionItem("p", "Receita", "", medicationIds = listOf("m"), renewalReminderEnabled = false, renewalReminderDaysBefore = 9)
        registration.save(recipe, listOf(med), null)
        assertFalse(recipes.getPrescriptionById("p")!!.renewalReminderEnabled)
        assertEquals(9, recipes.getPrescriptionById("p")!!.renewalReminderDaysBefore)
        assertEquals(7, medications.getMedicationById("m")!!.pickupReminderDaysBefore)
        registration.save(recipe, listOf(med.copy(hasRecurringPickup = false, lastRefillDate = null, activeIngredient = null, prescribedPosology = "", sourceName = null, instructions = null)), null)
        assertFalse(db.medicationDao().getMedicationById("m")!!.hasRecurringPickup)
        assertNull(db.medicationDao().getMedicationById("m")!!.lastPickupDate)
        assertNull(db.medicationDao().getMedicationById("m")!!.activeIngredient)
        assertEquals("", db.medicationDao().getMedicationById("m")!!.prescribedPosology)
        assertNull(db.medicationDao().getMedicationById("m")!!.sourceName)
        assertNull(db.medicationDao().getMedicationById("m")!!.availabilityNote)
    }
    @Test fun replenishmentQuantityIsIndependentOfLegacyStockAndSurvivesPickup() = runBlocking {
        val med = MedicationItem("quantity", "Losartana", quantity = "2 caixas", stockQuantity = 9,
            hasRecurringPickup = true, lastRefillDate = LocalDate.now().minusDays(30))
        medications.addMedication(med)
        assertEquals("2 caixas", medications.getMedicationById(med.id)!!.quantity)
        medications.replenishStock(med.id, null, null, LocalDate.now())
        assertEquals("2 caixas", medications.getMedicationById(med.id)!!.quantity)
        assertEquals(9, db.medicationDao().getMedicationById(med.id)!!.currentStock)
        assertEquals(LocalDate.now(), db.medicationDao().getMedicationById(med.id)!!.lastPickupDate)
        medications.updateMedication(med.copy(quantity = ""))
        assertEquals("", medications.getMedicationById(med.id)!!.quantity)
        assertFalse(db.medicationDao().getMedicationById(med.id)!!.stockAlertEnabled)
    }
    @Test fun failureRollsBackDoctorMedicationsAndRecipeThenRetryUsesSameIds() = runBlocking {
        val failingRecipes = object : PrescriptionRepository by recipes {
            override suspend fun addPrescription(prescription: PrescriptionItem) {
                recipes.addPrescription(prescription)
                error("Falha após escrever a receita")
            }
        }
        val recipe = PrescriptionItem("p", "Receita", "", doctorId = "d", medicationIds = listOf("m"))
        val medication = MedicationItem("m", "Losartana")
        val doctor = Doctor("d", "Profissional teste")
        try {
            RoomPrescriptionRegistrationRepository(db, failingRecipes, medications, doctors).save(recipe, listOf(medication), doctor)
            fail("Esperava falha no salvamento")
        } catch (_: IllegalStateException) {}
        assertTrue(db.prescriptionDao().getAllPrescriptions().first().isEmpty())
        assertTrue(db.medicationDao().getAllMedications().first().isEmpty())
        assertTrue(doctors.getDoctors().first().isEmpty())
        RoomPrescriptionRegistrationRepository(db, recipes, medications, doctors).save(recipe, listOf(medication), doctor)
        assertEquals(listOf("p"), db.prescriptionDao().getAllPrescriptions().first().map { it.id })
        assertEquals(listOf("m"), db.medicationDao().getAllMedications().first().map { it.id })
    }
}
