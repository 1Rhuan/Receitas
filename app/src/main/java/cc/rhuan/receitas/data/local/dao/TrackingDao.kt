package cc.rhuan.receitas.data.local.dao

import androidx.room.*
import cc.rhuan.receitas.data.local.entity.*
import kotlinx.coroutines.flow.Flow
@Dao
interface TrackingDao {
    @Query("SELECT * FROM prescription_medications") fun observeItems(): Flow<List<PrescriptionMedicationEntity>>
    @Query("SELECT * FROM prescription_medications WHERE prescriptionId = :id") suspend fun items(id: String): List<PrescriptionMedicationEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putItems(items: List<PrescriptionMedicationEntity>)
    @Query("DELETE FROM prescription_medications WHERE prescriptionId = :id") suspend fun clearItems(id: String)
    @Query("SELECT * FROM pickup_tracking") fun observeTracking(): Flow<List<PickupTrackingEntity>>
    @Query("SELECT * FROM pickup_tracking WHERE medicationId = :id") suspend fun tracking(id: String): PickupTrackingEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTracking(tracking: PickupTrackingEntity)
    @Update suspend fun updateTracking(tracking: PickupTrackingEntity)
    @Query("SELECT * FROM pickup_events ORDER BY occurredOn DESC") fun observeEvents(): Flow<List<PickupEventEntity>>
    @Query("SELECT * FROM pickup_events WHERE trackingId = :id ORDER BY occurredOn DESC") suspend fun events(id: String): List<PickupEventEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertEvent(event: PickupEventEntity): Long
    @Update suspend fun updateEvent(event: PickupEventEntity)
    @Query("SELECT * FROM pickup_events WHERE id = :id") suspend fun event(id: String): PickupEventEntity?
    @Query("DELETE FROM pickup_events WHERE id = :id") suspend fun deleteEvent(id: String)
    @Query("UPDATE pickup_tracking SET prescriptionId = NULL WHERE prescriptionId = :id") suspend fun unlink(id: String)
    @Query("UPDATE pickup_tracking SET archived = :archived WHERE prescriptionId = :id") suspend fun archiveForPrescription(id: String, archived: Boolean)
}
