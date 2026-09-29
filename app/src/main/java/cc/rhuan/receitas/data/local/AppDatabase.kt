package cc.rhuan.receitas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import cc.rhuan.receitas.data.local.dao.DoctorDao
import cc.rhuan.receitas.data.local.dao.MedicationDao
import cc.rhuan.receitas.data.local.dao.NotificationDao
import cc.rhuan.receitas.data.local.dao.PrescriptionDao
import cc.rhuan.receitas.data.local.entity.DoctorEntity
import cc.rhuan.receitas.data.local.entity.MedicationEntity
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking

@Database(
    entities = [
        DoctorEntity::class,
        MedicationEntity::class,
        PrescriptionEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun medicationDao(): MedicationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receitas_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        seedInitialData(INSTANCE)
                    }
                })
                .build()
                INSTANCE = instance
                seedInitialData(instance)
                instance
            }
        }

        private fun seedInitialData(database: AppDatabase?) {
            if (database == null) return
            Executors.newSingleThreadExecutor().execute {
                runBlocking {
                    try {
                        if (database.doctorDao().getDoctorById("doc_1") == null) {
                            database.doctorDao().insertDoctor(
                                DoctorEntity(
                                    id = "doc_1",
                                    name = "Carlos Eduardo",
                                    registration = "12345-SP",
                                    specialty = "Endocrinologia",
                                    healthFacility = "UBS Central",
                                    phone = "(11) 98765-4321"
                                )
                            )
                            database.doctorDao().insertDoctor(
                                DoctorEntity(
                                    id = "doc_2",
                                    name = "Ana Paula",
                                    registration = "67890-SP",
                                    specialty = "Cardiologia",
                                    healthFacility = "Hospital São Lucas",
                                    phone = "(11) 91234-5678"
                                )
                            )
                        }

                        val today = LocalDate.now()

                        if (database.medicationDao().getMedicationById("med_1") == null) {
                            database.medicationDao().insertMedications(
                                listOf(
                                    MedicationEntity(
                                        id = "med_1",
                                        title = "Metformina",
                                        dosage = "850 mg",
                                        form = PharmaceuticalForm.TABLET,
                                        prescribedPosology = "1 comprimido ao dia após o almoço",
                                        prescribedQuantity = "2 caixas",
                                        source = MedicationSource.FARMACIA_BASICA,
                                        sourceName = "UBS Central",
                                        hasRecurringPickup = true,
                                        lastPickupDate = today.minusDays(30),
                                        pickupIntervalDays = 30,
                                        pickupReminderDaysBefore = 3,
                                        currentStock = 12,
                                        minStockThreshold = 5,
                                        availabilityNote = "Disponível para retirada na UBS Central"
                                    ),
                                    MedicationEntity(
                                        id = "med_2",
                                        title = "Losartana Potássica",
                                        dosage = "50 mg",
                                        form = PharmaceuticalForm.TABLET,
                                        prescribedPosology = "1 comprimido pela manhã",
                                        prescribedQuantity = "1 caixa",
                                        source = MedicationSource.FARMACIA_POPULAR,
                                        sourceName = "Drogaria São Paulo",
                                        hasRecurringPickup = true,
                                        lastPickupDate = today.minusDays(35),
                                        pickupIntervalDays = 30,
                                        pickupReminderDaysBefore = 3,
                                        currentStock = 2,
                                        minStockThreshold = 5,
                                        availabilityNote = "Retirada em atraso há 5 dias"
                                    ),
                                    MedicationEntity(
                                        id = "med_3",
                                        title = "Atorvastatina",
                                        dosage = "20 mg",
                                        form = PharmaceuticalForm.CAPSULE,
                                        prescribedPosology = "1 cápsula à noite",
                                        prescribedQuantity = "1 caixa",
                                        source = MedicationSource.FARMACIA_POPULAR,
                                        sourceName = "Farmácia Popular",
                                        hasRecurringPickup = true,
                                        lastPickupDate = today.minusDays(26),
                                        pickupIntervalDays = 30,
                                        pickupReminderDaysBefore = 3,
                                        currentStock = 18,
                                        minStockThreshold = 5,
                                        availabilityNote = "Próxima retirada prevista em 4 dias"
                                    )
                                )
                            )
                        }

                        if (database.prescriptionDao().getPrescriptionById("presc_1") == null) {
                            database.prescriptionDao().insertPrescription(
                                PrescriptionEntity(
                                    id = "presc_1",
                                    title = "Receita de Metformina e Atorvastatina",
                                    type = PrescriptionType.SPECIAL_CONTROL,
                                    validityDays = 30,
                                    issueDate = today.minusDays(25),
                                    renewalReminderEnabled = true,
                                    renewalReminderDaysBefore = 5,
                                    doctorId = "doc_1",
                                    medicationIds = listOf("med_1", "med_3"),
                                    notes = "Acompanhamento trimestral de exames de sangue."
                                )
                            )
                            database.prescriptionDao().insertPrescription(
                                PrescriptionEntity(
                                    id = "presc_2",
                                    title = "Receita de Losartana 50mg",
                                    type = PrescriptionType.CONTINUO,
                                    validityDays = 30,
                                    issueDate = today.minusDays(35),
                                    renewalReminderEnabled = true,
                                    renewalReminderDaysBefore = 5,
                                    doctorId = "doc_2",
                                    medicationIds = listOf("med_2"),
                                    notes = "Manter medição diária da pressão arterial."
                                )
                            )
                        }
                    } catch (_: Exception) {
                        // Ignore seeding errors on open
                    }
                }
            }
        }
    }
}
