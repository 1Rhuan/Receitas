package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val registration: String? = null,
    val specialty: String? = null,
    val healthFacility: String? = null,
    val phone: String? = null
)
