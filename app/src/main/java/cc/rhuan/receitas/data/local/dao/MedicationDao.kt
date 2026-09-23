package cc.rhuan.receitas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cc.rhuan.receitas.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY title ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): MedicationEntity?

    @Query("SELECT * FROM medications WHERE id IN (:ids)")
    fun getMedicationsByIds(ids: List<String>): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Int)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: String)
}
