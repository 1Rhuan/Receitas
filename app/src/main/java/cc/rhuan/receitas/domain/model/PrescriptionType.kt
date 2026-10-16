package cc.rhuan.receitas.domain.model

// Suggested defaults; the user confirms the actual expiry from the document or pharmacy.
enum class PrescriptionType(val label: String, val defaultValidityDays: Int?) {
    CONTINUO("Uso contínuo", 180), ANTIBIOTICO("Antibiótico", 10), CONTROLADO("Controlado", 30),
    FARMACIA_POPULAR("Farmácia Popular", 180), SUS_BASIC("Farmácia Básica (SUS)", 180),
    ANUAL("Uso contínuo anual", 365), PERSONALIZADO("Personalizado", null);
    companion object {
        fun fromLabel(label: String): PrescriptionType = entries.find { it.label.equals(label, true) || it.name.equals(label, true) } ?: SUS_BASIC
    }
}
