package cc.rhuan.receitas.domain.model

class BusinessRuleViolation(message: String) : IllegalArgumentException(message)
internal fun requireBusiness(condition: Boolean, message: () -> String = { "Confira os campos antes de salvar." }) {
    if (!condition) throw BusinessRuleViolation(message())
}
