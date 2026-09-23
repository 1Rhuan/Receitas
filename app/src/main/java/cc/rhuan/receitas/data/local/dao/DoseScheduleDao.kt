package cc.rhuan.receitas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cc.rhuan.receitas.data.local.entity.DoseScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseScheduleDao {
    @Query("SELECT * FROM dose_schedules WHERE isActive = 1")
    fun getActiveSchedules(): Flow<List<DoseScheduleEntity>>

    @Query("SELECT * FROM dose_schedules WHERE medicationId = :medicationId")
    fun getSchedulesByMedication(medicationId: String): Flow<List<DoseScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DoseScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DoseScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: DoseScheduleEntity)

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesByMedication(medicationId: String)
}
