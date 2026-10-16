package cc.rhuan.receitas

import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.text.AndroidTextResolver
import cc.rhuan.receitas.text.TextResolver

object TestTexts : TextResolver by AndroidTextResolver(InstrumentationRegistry.getInstrumentation().targetContext)
