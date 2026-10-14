package cc.rhuan.receitas.data.local

import android.content.Context
import androidx.room.*
import cc.rhuan.receitas.data.local.dao.*
import cc.rhuan.receitas.data.local.entity.*

@Database(entities = [DoctorEntity::class, MedicationEntity::class, PrescriptionEntity::class,
    NotificationEntity::class, PrescriptionMedicationEntity::class, PickupTrackingEntity::class, PickupEventEntity::class],
    version = 4, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doctorDao(): DoctorDao
    abstract fun medicationDao(): MedicationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun trackingDao(): TrackingDao
    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "receitas_database")
                .addMigrations(DatabaseMigrations.FROM_2_TO_3, DatabaseMigrations.FROM_3_TO_4)
                .build().also { instance = it }
        }
    }
}
