package cc.rhuan.receitas.domain.model

import cc.rhuan.receitas.R

enum class PharmaceuticalForm(val labelRes: Int) {
    TABLET(R.string.form_tablet),
    CAPSULE(R.string.form_capsule),
    LIQUID(R.string.form_liquid),
    DROPS(R.string.form_drops),
    OINTMENT(R.string.form_ointment),
    INJECTION(R.string.form_injection),
    OTHER(R.string.source_other);


}
