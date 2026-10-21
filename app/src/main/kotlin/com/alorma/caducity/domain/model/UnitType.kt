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
  BRICKS(UnitParent.UNITS),
  GRAMS(UnitParent.MEASURED),
  MILLILITRES(UnitParent.MEASURED),
  HANDFULS(UnitParent.HANDFULS),
  ;

  /** Amount (g or mL) a measured product starts with; null for the types that are not measured. */
  val defaultAmount: Int? get() = if (parent == UnitParent.MEASURED) DEFAULT_AMOUNT else null

  /** Step of the − / + selectors, in g or mL; null for the types that are not measured. */
  val amountStep: Int?
    get() =
      when (this) {
        GRAMS -> 50
        MILLILITRES -> 100
        else -> null
      }

  companion object {
    val DEFAULT = UNITS

    const val DEFAULT_AMOUNT = 500

    /** Largest amount of a single batch, in g or mL (100 kg / 100 L). */
    const val MAX_AMOUNT = 100_000
  }
}
