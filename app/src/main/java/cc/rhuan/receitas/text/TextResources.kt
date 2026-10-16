package cc.rhuan.receitas.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun TextMessage.asString(): String = stringResource(resourceId,
    *arguments.map { if (it is TextMessage) it.asString() else it }.toTypedArray())
