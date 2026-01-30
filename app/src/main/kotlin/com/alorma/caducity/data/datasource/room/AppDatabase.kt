package com.alorma.caducity.data.datasource.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [
    CategoryRoomEntity::class,
    ItemRoomEntity::class,
    ProductRoomEntity::class,
  ],
  version = 1,
  exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun categoryDao(): CategoryDao
  abstract fun itemDao(): ItemDao
  abstract fun productDao(): ProductDao
}
