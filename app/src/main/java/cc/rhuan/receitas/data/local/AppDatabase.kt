package cc.rhuan.receitas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cc.rhuan.receitas.data.local.dao.DoctorDao
import cc.rhuan.receitas.data.local.dao.DoseLogDao
import cc.rhuan.receitas.data.local.dao.DoseScheduleDao
import cc.rhuan.receitas.data.local.dao.MedicationDao
import cc.rhuan.receitas.data.local.dao.NotificationDao
import cc.rhuan.receitas.data.local.dao.PrescriptionDao
import cc.rhuan.receitas.data.local.entity.DoctorEntity
import cc.rhuan.receitas.data.local.entity.DoseLogEntity
import cc.rhuan.receitas.data.local.entity.DoseScheduleEntity
import cc.rhuan.receitas.data.local.entity.MedicationEntity
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity

@Database(
    entities = [
        DoctorEntity::class,
        MedicationEntity::class,
        PrescriptionEntity::class,
        DoseScheduleEntity::class,
        DoseLogEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun medicationDao(): MedicationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun doseScheduleDao(): DoseScheduleDao
    abstract fun doseLogDao(): DoseLogDao
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
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
