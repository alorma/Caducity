package com.alorma.caducity.ui.unit

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.UnitType

/** «%d unitats», «%d paquets»…: how many of a product are counted. Measured and handful types arrive in later phases. */
@get:PluralsRes
val UnitType.countPlural: Int
  get() =
    when (this) {
      UnitType.TUPPERS -> R.plurals.unit_count_tuppers
      UnitType.PACKS -> R.plurals.unit_count_packs
      UnitType.CANS -> R.plurals.unit_count_cans
      UnitType.UNITS,
      UnitType.GRAMS,
      UnitType.MILLILITRES,
      UnitType.HANDFULS,
      -> R.plurals.home_unit_count
    }

@Composable
fun unitCountText(
  unitType: UnitType,
  count: Int,
): String = pluralStringResource(unitType.countPlural, count, count)
