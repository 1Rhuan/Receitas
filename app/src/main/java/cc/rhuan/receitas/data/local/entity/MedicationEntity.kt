package cc.rhuan.receitas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.rhuan.receitas.domain.model.MedicationSource
import cc.rhuan.receitas.domain.model.PharmaceuticalForm
import java.time.LocalDate

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val activeIngredient: String? = null,
    val form: PharmaceuticalForm = PharmaceuticalForm.TABLET,
    val dosage: String = "",
    // Retired columns are preserved for migration/history only; current data uses normalized tables.
    val prescribedPosology: String = "",
    val prescribedQuantity: String = "",
    // Origem e Retirada
    val source: MedicationSource = MedicationSource.SELF_PURCHASE,
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
