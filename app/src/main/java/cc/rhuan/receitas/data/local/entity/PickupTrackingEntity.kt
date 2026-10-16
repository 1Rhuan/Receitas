package cc.rhuan.receitas.data.local.entity

import androidx.room.*
import cc.rhuan.receitas.domain.model.MedicationSource
@Entity(tableName = "pickup_tracking", foreignKeys = [
    ForeignKey(entity = MedicationEntity::class, parentColumns = ["id"], childColumns = ["medicationId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = PrescriptionEntity::class, parentColumns = ["id"], childColumns = ["prescriptionId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index(value = ["medicationId"], unique = true), Index("prescriptionId")])
data class PickupTrackingEntity(@PrimaryKey val id: String, val medicationId: String,
    val prescriptionId: String? = null, val enabled: Boolean = false, val archived: Boolean = false,
    val intervalAmount: Int = 30, val intervalUnit: String = "DAYS", val reminderDaysBefore: Int = 3,
    val source: MedicationSource = MedicationSource.SELF_PURCHASE, val sourceName: String? = null,
    val quantity: String = "", val note: String? = null)
