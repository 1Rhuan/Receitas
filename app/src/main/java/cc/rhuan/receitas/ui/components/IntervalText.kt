package cc.rhuan.receitas.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import cc.rhuan.receitas.R
import cc.rhuan.receitas.domain.model.IntervalUnit

@Composable
internal fun intervalUnitText(amount: Int, unit: IntervalUnit): String = pluralStringResource(
    when (unit) {
        IntervalUnit.DAYS -> R.plurals.interval_days
        IntervalUnit.WEEKS -> R.plurals.interval_weeks
        IntervalUnit.MONTHS -> R.plurals.interval_months
        IntervalUnit.YEARS -> R.plurals.interval_years
    }, amount,
)
