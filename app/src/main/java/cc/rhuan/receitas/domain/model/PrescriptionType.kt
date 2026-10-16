package cc.rhuan.receitas.domain.model

import cc.rhuan.receitas.R

// Suggested defaults; the user confirms the actual expiry from the document or pharmacy.
enum class PrescriptionType(val labelRes: Int, val defaultValidityDays: Int?) {
    CONTINUOUS_USE(R.string.prescription_type_continuous, 180), ANTIBIOTIC(R.string.prescription_type_antibiotic, 10), CONTROLLED(R.string.prescription_type_controlled, 30),
    POPULAR_PHARMACY(R.string.source_popular_pharmacy, 180), SUS_BASIC(R.string.source_public_pharmacy, 180),
    ANNUAL_USE(R.string.prescription_type_annual, 365), CUSTOM(R.string.prescription_type_custom, null);

}
