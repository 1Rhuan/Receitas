package cc.rhuan.receitas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cc.rhuan.receitas.data.local.entity.DoseLogEntity
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs ORDER BY scheduledDateTime DESC")
    fun getAllDoseLogs(): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE scheduledDateTime >= :startOfDay AND scheduledDateTime <= :endOfDay ORDER BY scheduledDateTime ASC")
    fun getDoseLogsForDay(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getDoseLogById(id: String): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity)

    @Update
    suspend fun updateDoseLog(log: DoseLogEntity)
}
