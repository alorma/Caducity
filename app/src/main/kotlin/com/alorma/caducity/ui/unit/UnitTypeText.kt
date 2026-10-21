package com.alorma.caducity.ui.unit

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Inventory2
import com.alorma.caducity.base.ui.icons.symbols.Kitchen
import com.alorma.caducity.base.ui.icons.symbols.LocalDrink
import com.alorma.caducity.base.ui.icons.symbols.Scale
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Tag
import com.alorma.caducity.base.ui.icons.symbols.TakeoutDining
import com.alorma.caducity.base.ui.icons.symbols.WaterDrop
import com.alorma.caducity.domain.model.UnitParent
import com.alorma.caducity.domain.model.UnitType

/** «%d unitats», «%d paquets»…: how many of a product are counted. Measured and handful types arrive in later phases. */
@get:PluralsRes
val UnitType.countPlural: Int
  get() =
    when (this) {
      UnitType.TUPPERS -> R.plurals.unit_count_tuppers
      UnitType.PACKS -> R.plurals.unit_count_packs
      UnitType.CANS -> R.plurals.unit_count_cans
      UnitType.BRICKS -> R.plurals.unit_count_bricks
      UnitType.UNITS,
      UnitType.GRAMS,
      UnitType.MILLILITRES,
      UnitType.HANDFULS,
      -> R.plurals.home_unit_count
    }

/** The quantity of a row: the amount of a measured batch («500 g»), else how many («3 paquets»). */
@Composable
fun unitCountText(
  unitType: UnitType,
  count: Int,
  amount: Int? = null,
): String =
  if (amount != null && unitType.parent == UnitParent.MEASURED) {
    formatAmount(unitType, amount, LocalConfiguration.current.locales[0])
  } else {
    pluralStringResource(unitType.countPlural, count, count)
  }

/** Name of the type in the picker: «Unitats», «Tuppers», «Paquets», «Llaunes». */
@Composable
fun UnitType.label(): String =
  stringResource(
    when (this) {
      UnitType.TUPPERS -> R.string.unit_type_tuppers
      UnitType.PACKS -> R.string.unit_type_packs
      UnitType.CANS -> R.string.unit_type_cans
      UnitType.GRAMS -> R.string.unit_type_grams
      UnitType.MILLILITRES -> R.string.unit_type_millilitres
      UnitType.BRICKS -> R.string.unit_type_bricks
      UnitType.UNITS,
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
      UnitType.GRAMS -> R.string.unit_type_grams_example
      UnitType.MILLILITRES -> R.string.unit_type_millilitres_example
      UnitType.BRICKS -> R.string.unit_type_bricks_example
      UnitType.UNITS,
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
      UnitType.BRICKS -> Symbols.Outlined.LocalDrink
      UnitType.GRAMS -> Symbols.Outlined.Scale
      UnitType.MILLILITRES -> Symbols.Outlined.WaterDrop
      UnitType.UNITS,
      UnitType.HANDFULS,
      -> Symbols.Outlined.Tag
    }

/** Name of the parent type in the picker tabs: «Unitats», «Mesurable». */
@Composable
fun UnitParent.label(): String =
  stringResource(
    when (this) {
      UnitParent.UNITS -> R.string.unit_parent_units
      UnitParent.MEASURED -> R.string.unit_parent_measured
      UnitParent.HANDFULS -> R.string.unit_parent_units
    },
  )

/** One line saying how the parent type counts: «D'una en una · el ✓ en gasta una». */
@Composable
fun UnitParent.rule(): String =
  stringResource(
    when (this) {
      UnitParent.UNITS -> R.string.unit_parent_units_rule
      UnitParent.MEASURED -> R.string.unit_parent_measured_rule
      UnitParent.HANDFULS -> R.string.unit_parent_units_rule
    },
  )

val UnitParent.icon: ImageVector
  get() =
    when (this) {
      UnitParent.UNITS -> Symbols.Outlined.Tag
      UnitParent.MEASURED -> Symbols.Outlined.Scale
      UnitParent.HANDFULS -> Symbols.Outlined.Tag
    }
