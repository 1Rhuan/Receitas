package cc.rhuan.receitas.text

import java.io.Serializable

/** A resource reference that can be kept in restorable form state. */
data class TextMessage(val resourceId: Int, val arguments: List<Serializable> = emptyList()) : Serializable

interface TextResolver {
    fun get(resourceId: Int, vararg arguments: Any): String
    fun resolve(message: TextMessage): String = get(message.resourceId,
        *message.arguments.map { if (it is TextMessage) resolve(it) else it }.toTypedArray())
}
