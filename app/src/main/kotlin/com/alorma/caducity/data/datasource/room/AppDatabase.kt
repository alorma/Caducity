package com.alorma.caducity.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [
    ProductRoomEntity::class,
    ProductInstanceRoomEntity::class,
    VariantRoomEntity::class,
  ],
  version = 1,
  exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun productDao(): ProductDao
  abstract fun variantDao(): VariantDao
}
