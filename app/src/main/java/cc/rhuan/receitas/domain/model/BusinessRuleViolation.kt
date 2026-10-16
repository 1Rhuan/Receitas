package cc.rhuan.receitas.domain.model

import cc.rhuan.receitas.text.TextMessage

import cc.rhuan.receitas.R

class BusinessRuleViolation(val userMessage: TextMessage) : IllegalArgumentException("Business rule violation: ${userMessage.resourceId}")
internal fun requireBusiness(condition: Boolean, message: () -> TextMessage = { TextMessage(R.string.review_fields_error) }) {
    if (!condition) throw BusinessRuleViolation(message())
}
