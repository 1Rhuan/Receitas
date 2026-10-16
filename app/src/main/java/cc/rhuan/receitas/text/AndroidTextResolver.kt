package cc.rhuan.receitas.text

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidTextResolver @Inject constructor(@ApplicationContext private val context: Context) : TextResolver {
    override fun get(resourceId: Int, vararg arguments: Any): String =
        if (arguments.isEmpty()) context.getString(resourceId) else context.getString(resourceId, *arguments)
}
