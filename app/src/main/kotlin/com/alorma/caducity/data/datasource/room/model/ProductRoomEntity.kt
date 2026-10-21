package com.alorma.caducity.data.datasource.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alorma.caducity.domain.model.UnitType

@Entity(
  tableName = "products",
  foreignKeys = [
    ForeignKey(
      entity = CategoryRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["categoryId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [Index("categoryId")],
)
data class ProductRoomEntity(
  @PrimaryKey
  val id: String,
  val categoryId: String,
  val name: String,
  val createdAt: Long,
  /** Overrides the category: null inherits [CategoryRoomEntity.canFreeze]. */
  val canFreeze: Boolean? = null,
  /** How the product is counted; stored by `UnitTypeConverters`. */
  @ColumnInfo(defaultValue = "'UNITS'")
  val unitType: UnitType = UnitType.DEFAULT,
)
