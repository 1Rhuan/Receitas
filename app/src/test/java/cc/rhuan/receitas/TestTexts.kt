package cc.rhuan.receitas

import cc.rhuan.receitas.text.TextResolver
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/** Reads the same catalog as Android without requiring a device in unit tests. */
object TestTexts : TextResolver {
    private val names = R.string::class.java.fields.associate { it.getInt(null) to it.name }
    private val values by lazy {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File("src/main/res/values/strings.xml"))
        val strings = document.getElementsByTagName("string")
        (0 until strings.length).associate { index ->
            val node = strings.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent.replace("\\'", "'")
        }
    }
    override fun get(resourceId: Int, vararg arguments: Any): String {
        val value = values.getValue(names.getValue(resourceId))
        return if (arguments.isEmpty()) value else String.format(Locale.forLanguageTag("pt-BR"), value, *arguments)
    }
}
