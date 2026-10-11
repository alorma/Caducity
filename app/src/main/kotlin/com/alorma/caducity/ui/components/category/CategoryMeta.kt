package com.alorma.caducity.ui.components.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** «14 productes · propera, avui»: the units of a category and when the closest one expires. */
@Composable
fun categoryMeta(
  unitCount: Int,
  nearest: LocalDate?,
  today: LocalDate,
): String {
  val count = pluralStringResource(R.plurals.home_product_count, unitCount, unitCount)
  nearest ?: return count
  val relative =
    when (val days = today.daysUntil(nearest)) {
      0 -> stringResource(R.string.calendar_relative_today)
      1 -> stringResource(R.string.calendar_relative_tomorrow)
      in 2..Int.MAX_VALUE -> pluralStringResource(R.plurals.calendar_relative_in, days, days)
      else -> pluralStringResource(R.plurals.calendar_relative_ago, -days, -days)
    }
  return stringResource(R.string.pantry_card_meta, count, relative.replaceFirstChar { it.lowercase() })
}
