package cc.rhuan.receitas

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.data.local.*
import cc.rhuan.receitas.data.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.time.LocalDate

class DatabaseMigrationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "migration-test.db"
    @After fun cleanup() { context.deleteDatabase(name) }
    private fun oldDatabase(version: Int) {
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name); file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE doctors (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, registration TEXT, specialty TEXT, healthFacility TEXT, phone TEXT)")
            db.execSQL("CREATE TABLE prescriptions (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, type TEXT, validityDays INTEGER, issueDate TEXT NOT NULL, renewalReminderEnabled INTEGER NOT NULL, renewalReminderDaysBefore INTEGER NOT NULL, doctorId TEXT, medicationIds TEXT NOT NULL, notes TEXT, photoUri TEXT)")
            db.execSQL("CREATE TABLE notifications (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, message TEXT NOT NULL, timestamp TEXT NOT NULL, type TEXT NOT NULL, isRead INTEGER NOT NULL, actionTaken TEXT, targetId TEXT, createdAtMillis INTEGER NOT NULL)")
            if (version == 3) db.execSQL("CREATE TABLE medications (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, activeIngredient TEXT, form TEXT NOT NULL, dosage TEXT NOT NULL, prescribedPosology TEXT NOT NULL, prescribedQuantity TEXT NOT NULL, source TEXT NOT NULL, sourceName TEXT, hasRecurringPickup INTEGER NOT NULL, lastPickupDate TEXT, pickupIntervalDays INTEGER, pickupReminderDaysBefore INTEGER NOT NULL, currentStock INTEGER, stockAlertEnabled INTEGER NOT NULL, minStockThreshold INTEGER NOT NULL, availabilityNote TEXT)")
            else db.execSQL("CREATE TABLE medications (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, form TEXT NOT NULL, dosage TEXT NOT NULL, doseAmount TEXT NOT NULL, frequencyHours INTEGER NOT NULL, durationDays INTEGER NOT NULL, firstDoseTime TEXT, source TEXT NOT NULL, sourceName TEXT, hasRecurringPickup INTEGER NOT NULL, lastPickupDate TEXT, pickupIntervalDays INTEGER, pickupReminderDaysBefore INTEGER NOT NULL, currentStock INTEGER, stockAlertEnabled INTEGER NOT NULL, minStockThreshold INTEGER NOT NULL, availabilityNote TEXT)")
            db.execSQL("INSERT INTO doctors VALUES ('d', 'Profissional', '123-SP', 'Clínica', NULL, NULL)")
            db.execSQL("INSERT INTO prescriptions VALUES ('p', 'Receita', 'PFPB', 45, '2026-01-01', 1, 5, '123-SP', 'm,missing', 'Observação original', 'content://legacy-photo')")
            if (version == 3) db.execSQL("INSERT INTO medications VALUES ('m','Losartana',NULL,'TABLET','50 mg','Pela manhã','2 caixas','FARMACIA_BASICA','UBS',1,'2026-01-10',30,3,9,1,5,'Nota')")
            else db.execSQL("INSERT INTO medications VALUES ('m','Losartana','TABLET','50 mg','1 comprimido',8,30,NULL,'FARMACIA_BASICA','UBS',1,'2026-01-10',30,3,9,1,5,'Nota')")
            db.execSQL("INSERT INTO notifications VALUES ('prescription:p:2026-02-15:expired','Alerta','Mensagem','15/02/2026','PRESCRIPTION_EXPIRING',1,'Lembrete encerrado','p',1)")
            db.version = version
        }
    }
    private fun verify(version: Int) = runBlocking {
        oldDatabase(version)
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(DatabaseMigrations.FROM_2_TO_3, DatabaseMigrations.FROM_3_TO_4).build()
        try {
            val p = RoomPrescriptionRepository(db).getPrescriptionById("p")!!
            assertEquals("Profissional", p.doctorName)
            assertEquals("d", p.doctorId)
            assertEquals("Observação original", p.notes)
            assertEquals(LocalDate.of(2026, 2, 15), p.expirationDate)
            assertEquals(listOf("m"), p.medicationIds)
            assertEquals("content://legacy-photo", db.prescriptionDao().getPrescriptionById("p")!!.photoUri)
            val m = RoomMedicationRepository(db).getMedicationById("m")!!
            assertEquals(LocalDate.of(2026, 1, 10), m.lastRefillDate)
            assertEquals(1, m.pickupHistory.size)
            assertNull(m.linkedPrescriptionId)
            assertEquals(9, db.medicationDao().getMedicationById("m")!!.currentStock)
            assertEquals("OBSOLETE", db.notificationDao().getNotificationById("prescription:p:2026-02-15:expired")!!.resolutionReason)
            if (version == 2) assertTrue(p.items.single().posology.contains("8 horas")) else assertEquals("Pela manhã", p.items.single().posology)
        } finally { db.close() }
    }
    @Test fun version3PreservesDataAndNormalizesRelations() { verify(3) }
    @Test fun version2PreservesLegacyPosologyAndPickup() { verify(2) }
}
