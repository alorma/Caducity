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
 * The fixed list of unit types of a product; the subtypes of a [parent] only change the name and the icon. Stored by
 * name in the database and the backups: never rename or remove a value.
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

    /** The type stored as [name]; [DEFAULT] for anything unknown. */
    fun of(name: String?): UnitType = entries.firstOrNull { it.name == name } ?: DEFAULT
  }
}
