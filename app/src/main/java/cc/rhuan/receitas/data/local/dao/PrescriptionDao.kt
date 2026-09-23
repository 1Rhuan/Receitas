package cc.rhuan.receitas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cc.rhuan.receitas.data.local.entity.PrescriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY issueDate DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Update
    suspend fun updatePrescription(prescription: PrescriptionEntity)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescription(id: String)
}
