package cc.rhuan.receitas.data.local.entity

import androidx.room.*
import java.time.LocalDate
@Entity(tableName = "pickup_events", foreignKeys = [ForeignKey(entity = PickupTrackingEntity::class,
    parentColumns = ["id"], childColumns = ["trackingId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["trackingId", "occurredOn"], unique = true)])
data class PickupEventEntity(@PrimaryKey val id: String, val trackingId: String, val occurredOn: LocalDate,
    val recordedAt: Long = System.currentTimeMillis())
