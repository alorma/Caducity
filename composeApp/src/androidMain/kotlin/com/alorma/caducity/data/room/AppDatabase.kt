package com.alorma.caducity.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [
    ProductRoomEntity::class,
    ProductInstanceRoomEntity::class,
  ],
  version = 1,
  exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun productDao(): ProductDao
}
