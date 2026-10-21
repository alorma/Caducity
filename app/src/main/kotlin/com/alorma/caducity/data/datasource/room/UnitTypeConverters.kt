package com.alorma.caducity.data.datasource.room

import androidx.room.TypeConverter
import com.alorma.caducity.domain.model.UnitType

/**
 * Persists the unit type as an explicit code, never as the enum name, so a [UnitType] can be renamed or split without
 * touching the rows of existing users. The codes are also what the backups carry. Unknown values fall back to units.
 */
class UnitTypeConverters {
  @TypeConverter
  fun unitTypeToCode(unitType: UnitType): String =
    when (unitType) {
      UnitType.UNITS -> "UNITS"
      UnitType.TUPPERS -> "TUPPERS"
      UnitType.PACKS -> "PACKS"
      UnitType.CANS -> "CANS"
      UnitType.BRICKS -> "BRICKS"
      UnitType.GRAMS -> "GRAMS"
      UnitType.MILLILITRES -> "MILLILITRES"
      UnitType.HANDFULS -> "HANDFULS"
    }

  @TypeConverter
  fun codeToUnitType(code: String?): UnitType =
    when (code) {
      "TUPPERS" -> UnitType.TUPPERS
      "PACKS" -> UnitType.PACKS
      "CANS" -> UnitType.CANS
      "BRICKS" -> UnitType.BRICKS
      "GRAMS" -> UnitType.GRAMS
      "MILLILITRES" -> UnitType.MILLILITRES
      "HANDFULS" -> UnitType.HANDFULS
      else -> UnitType.UNITS
    }
}
