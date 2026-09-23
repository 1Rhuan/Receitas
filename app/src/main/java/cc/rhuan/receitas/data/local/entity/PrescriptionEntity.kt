package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: PrescriptionType? = null,
    val validityDays: Int? = null,
    val issueDate: LocalDate,
    val renewalReminderEnabled: Boolean = true,
    val renewalReminderDaysBefore: Int = 5,
    val doctorId: String? = null,
    val medicationIds: List<String> = emptyList(),
    val notes: String? = null,
    val photoUri: String? = null
)
