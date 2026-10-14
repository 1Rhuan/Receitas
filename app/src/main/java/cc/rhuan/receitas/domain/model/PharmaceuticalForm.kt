package cc.rhuan.receitas.domain.model

enum class PharmaceuticalForm(val label: String) {
    TABLET("Comprimido"),
    CAPSULE("Cápsula"),
    LIQUID("Xarope ou solução"),
    DROPS("Gotas"),
    OINTMENT("Pomada ou creme"),
    INJECTION("Injetável"),
    OTHER("Outro");

    companion object {
        fun fromLabel(label: String): PharmaceuticalForm {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: OTHER
        }
    }
}
