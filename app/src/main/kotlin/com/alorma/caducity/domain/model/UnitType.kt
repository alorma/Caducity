package com.alorma.caducity.domain.model

/** How a product is counted: the three parents of the unit type. */
enum class UnitParent {
  /** One by one: every unit is its own item. */
  UNITS,

  /** A single batch with an amount (g or mL). */
  MEASURED,

  /** A single batch with a level (a lot, half, little). */
  HANDFULS,
}

/**
 * The fixed list of unit types of a product; the subtypes of a [parent] only change the name and the icon. Persisted
 * through `UnitTypeConverters`, which owns the stored codes.
 */
enum class UnitType(
  val parent: UnitParent,
) {
  UNITS(UnitParent.UNITS),
  TUPPERS(UnitParent.UNITS),
  PACKS(UnitParent.UNITS),
  CANS(UnitParent.UNITS),
  GRAMS(UnitParent.MEASURED),
  MILLILITRES(UnitParent.MEASURED),
  HANDFULS(UnitParent.HANDFULS),
  ;

  companion object {
    val DEFAULT = UNITS
  }
}
