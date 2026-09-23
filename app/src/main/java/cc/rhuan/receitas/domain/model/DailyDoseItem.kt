package cc.rhuan.receitas.domain.model

data class DailyDoseItem(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dosage: String,
    val time: String,
    val isTaken: Boolean = false,
    val takenAtTime: String? = null,
    val status: DoseStatus = if (isTaken) DoseStatus.TAKEN else DoseStatus.PENDING,
    val treatmentId: String? = null,
)
