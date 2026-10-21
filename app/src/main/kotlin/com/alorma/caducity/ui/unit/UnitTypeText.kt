package com.alorma.caducity.ui.unit

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Inventory2
import com.alorma.caducity.base.ui.icons.symbols.Kitchen
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Tag
import com.alorma.caducity.base.ui.icons.symbols.TakeoutDining
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

/** Name of the type in the picker: «Unitats», «Tuppers», «Paquets», «Llaunes». */
@Composable
fun UnitType.label(): String =
  stringResource(
    when (this) {
      UnitType.TUPPERS -> R.string.unit_type_tuppers
      UnitType.PACKS -> R.string.unit_type_packs
      UnitType.CANS -> R.string.unit_type_cans
      UnitType.UNITS,
      UnitType.GRAMS,
      UnitType.MILLILITRES,
      UnitType.HANDFULS,
      -> R.string.unit_type_units
    },
  )

/** Singular and plural example of the type, «1 tupper, 2 tuppers». */
@Composable
fun UnitType.example(): String =
  stringResource(
    when (this) {
      UnitType.TUPPERS -> R.string.unit_type_tuppers_example
      UnitType.PACKS -> R.string.unit_type_packs_example
      UnitType.CANS -> R.string.unit_type_cans_example
      UnitType.UNITS,
      UnitType.GRAMS,
      UnitType.MILLILITRES,
      UnitType.HANDFULS,
      -> R.string.unit_type_units_example
    },
  )

val UnitType.icon: ImageVector
  get() =
    when (this) {
      UnitType.TUPPERS -> Symbols.Outlined.TakeoutDining
      UnitType.PACKS -> Symbols.Outlined.Inventory2
      UnitType.CANS -> Symbols.Outlined.Kitchen
      UnitType.UNITS,
      UnitType.GRAMS,
      UnitType.MILLILITRES,
      UnitType.HANDFULS,
      -> Symbols.Outlined.Tag
    }
