package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.rhuan.receitas.domain.model.DoseStatus
import java.time.LocalDateTime

@Entity(tableName = "dose_logs")
data class DoseLogEntity(
    @PrimaryKey
    val id: String,
    val doseScheduleId: String,
    val medicationId: String,
    val scheduledDateTime: LocalDateTime,
    val status: DoseStatus = DoseStatus.PENDING,
    val confirmedAt: LocalDateTime? = null
)
