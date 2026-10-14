package cc.rhuan.receitas.data.local.entity

import androidx.room.*
import cc.rhuan.receitas.domain.model.PrescriptionType
import java.time.LocalDate

@Entity(tableName = "prescriptions", foreignKeys = [
    ForeignKey(entity = DoctorEntity::class, parentColumns = ["id"], childColumns = ["doctorId"], onDelete = ForeignKey.SET_NULL),
    ForeignKey(entity = PrescriptionEntity::class, parentColumns = ["id"], childColumns = ["previousPrescriptionId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("doctorId"), Index("previousPrescriptionId")])
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
    // Legacy attachment retained only to preserve existing data.
    val photoUri: String? = null,
    val state: String = "ACTIVE",
    val previousPrescriptionId: String? = null,
    val doctorNameSnapshot: String = "",
    val doctorRegistrationSnapshot: String? = null,
    val doctorSpecialtySnapshot: String = "",
    val validityOrigin: String = "USER_ENTERED",
    val expiresOn: LocalDate? = null
)
