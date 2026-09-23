package cc.rhuan.receitas.domain.model

enum class DoseStatus(val label: String) {
    PENDING("Pendente"),
    TAKEN("Tomada"),
    SKIPPED("Pulada"),
    SNOOZED("Adiada");

    val isCompleted: Boolean
        get() = this == TAKEN || this == SKIPPED
}
