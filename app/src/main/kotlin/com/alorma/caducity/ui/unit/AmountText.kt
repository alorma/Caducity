package com.alorma.caducity.ui.unit

import com.alorma.caducity.domain.model.UnitType
import java.text.NumberFormat
import java.util.Locale

/**
 * «500 g», «750 mL»; from 1000 up «1,2 kg», «1,5 L» (up to two decimals, as the [locale] writes them). Only the
 * measured types have an amount: any other type gets the bare number.
 */
fun formatAmount(
  unitType: UnitType,
  amount: Int,
  locale: Locale = Locale.getDefault(),
): String {
  val big = amount >= 1000
  val (value, symbol) =
    when (unitType) {
      UnitType.GRAMS -> if (big) amount / 1000.0 to "kg" else amount.toDouble() to "g"
      UnitType.MILLILITRES -> if (big) amount / 1000.0 to "L" else amount.toDouble() to "mL"
      else -> amount.toDouble() to ""
    }
  val number = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }.format(value)
  return if (symbol.isEmpty()) number else "$number $symbol"
}
