package com.alorma.caducity.data.datasource.room

import androidx.room.TypeConverter
import com.alorma.caducity.domain.model.HandfulLevel

/**
 * Persists the level of a handful product as an explicit code, never as the enum name, so it can be renamed or split
 * without touching the rows of existing users. The codes are also what the backups carry.
 */
class HandfulLevelConverters {
  @TypeConverter
  fun levelToCode(level: HandfulLevel?): String? =
    when (level) {
      null -> null
      HandfulLevel.LOTS -> "LOTS"
      HandfulLevel.HALF -> "HALF"
      HandfulLevel.LITTLE -> "LITTLE"
    }

  @TypeConverter
  fun codeToLevel(code: String?): HandfulLevel? =
    when (code) {
      null -> null
      "HALF" -> HandfulLevel.HALF
      "LITTLE" -> HandfulLevel.LITTLE
      else -> HandfulLevel.LOTS
    }
}
