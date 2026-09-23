package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
    val dosage: String = "",
    // Posologia
    val doseAmount: String = "1 comprimido",
    val frequencyHours: Int = 8,
    val durationDays: Int = 30,
    val firstDoseTime: LocalTime? = null,
    // Origem e Retirada
    val source: MedicationSource = MedicationSource.COMPRA_PROPRIA,
    val sourceName: String? = null,
    val hasRecurringPickup: Boolean = false,
    val lastPickupDate: LocalDate? = null,
    val pickupIntervalDays: Int? = 30,
    val pickupReminderDaysBefore: Int = 3,
    // Estoque
    val currentStock: Int? = null,
    val stockAlertEnabled: Boolean = true,
    val minStockThreshold: Int = 5,
    val availabilityNote: String? = null
)
