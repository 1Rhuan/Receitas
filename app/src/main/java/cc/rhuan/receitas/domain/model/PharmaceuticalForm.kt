package cc.rhuan.receitas.domain.model

enum class PharmaceuticalForm(val label: String) {
    TABLET("Comprimido"),
    CAPSULE("Cápsula"),
    LIQUID("Xarope / Solução"),
    DROPS("Gotas"),
    OINTMENT("Pomada / Creme"),
    INJECTION("Injetável"),
    OTHER("Outro");

    companion object {
        fun fromLabel(label: String): PharmaceuticalForm {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: OTHER
        }
    }
}
