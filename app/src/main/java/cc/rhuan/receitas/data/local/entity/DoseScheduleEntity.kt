package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "dose_schedules")
data class DoseScheduleEntity(
    @PrimaryKey
    val id: String,
    val medicationId: String,
    val scheduledTime: LocalTime,
    val isActive: Boolean = true
)
