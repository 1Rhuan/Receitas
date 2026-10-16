package cc.rhuan.receitas.domain.model

import cc.rhuan.receitas.R

enum class MedicationSource(val labelRes: Int) {
    SELF_PURCHASE(R.string.source_self_purchase),
    HEALTH_INSURANCE(R.string.source_health_insurance),
    POPULAR_PHARMACY(R.string.source_popular_pharmacy),
    PUBLIC_PHARMACY(R.string.source_public_pharmacy),
    OTHER(R.string.source_other);


}
