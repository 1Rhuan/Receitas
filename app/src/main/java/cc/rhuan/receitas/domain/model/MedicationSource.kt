package cc.rhuan.receitas.domain.model

enum class MedicationSource(val label: String) {
    COMPRA_PROPRIA("Compra Própria"),
    CONVENIO("Convênio / Plano de Saúde"),
    FARMACIA_POPULAR("Farmácia Popular"),
    FARMACIA_BASICA("Farmácia Básica (SUS)"),
    OUTRO("Outro");

    companion object {
        fun fromLabel(label: String): MedicationSource {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: COMPRA_PROPRIA
        }
    }
}
