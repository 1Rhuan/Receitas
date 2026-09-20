package cc.rhuan.receitas.domain.model

data class PrescriptionItem(
    val id: String,
    val title: String,
    val specialty: String,
    val doctorName: String = "",
    val daysUntilExpiration: Int,
    val issueDate: String = "",
    val notes: String? = null,
) {
    val isExpired: Boolean
        get() = daysUntilExpiration <= 0
}
