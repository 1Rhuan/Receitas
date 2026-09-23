package cc.rhuan.receitas.domain.model

data class Doctor(
    val id: String,
    val name: String,
    val registration: String? = null,
    val specialty: String? = null,
    val healthFacility: String? = null,
    val phone: String? = null
)
