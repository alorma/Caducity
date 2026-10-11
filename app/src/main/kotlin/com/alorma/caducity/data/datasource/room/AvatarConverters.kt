package com.alorma.caducity.data.datasource.room

import androidx.room.TypeConverter
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape

/**
 * Persists category avatars as their stable string id so the domain types stay independent of the
 * database. Unknown values (e.g. a removed shape) fall back to a default instead of failing.
 */
class AvatarConverters {
  @TypeConverter
  fun shapeToString(shape: CategoryShape): String = shape.name

  @TypeConverter
  fun stringToShape(value: String): CategoryShape =
    CategoryShape.entries.firstOrNull { it.name == value } ?: CategoryShape.COOKIE

  @TypeConverter
  fun iconToString(icon: CategoryIcon): String = icon.id

  @TypeConverter
  fun stringToIcon(value: String): CategoryIcon = CategoryIcon(value)
}
