package cc.rhuan.receitas.data.local.entity

import androidx.room.*
@Entity(tableName = "prescription_medications", primaryKeys = ["prescriptionId", "medicationId"],
    foreignKeys = [ForeignKey(entity = PrescriptionEntity::class, parentColumns = ["id"], childColumns = ["prescriptionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MedicationEntity::class, parentColumns = ["id"], childColumns = ["medicationId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("medicationId")])
data class PrescriptionMedicationEntity(val prescriptionId: String, val medicationId: String, val posology: String = "", val quantity: String = "", val titleSnapshot: String = "", val dosageSnapshot: String = "", val formSnapshot: String = "TABLET")
