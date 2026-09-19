package cc.rhuan.receitas.domain.model

data class MedicationItem(
    val id: String,
    val title: String,
    val dosage: String,
    val quantity: String,
    val frequency: String = "",
    val category: String = "Uso Geral",
    val isAvailable: Boolean = true,
    val daysUntilAvailable: Int = 0,
    val instructions: String? = null,
)
