package com.alorma.caducity.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

  @Query("SELECT * FROM items")
  fun getAllItems(): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId")
  fun getCategoryItems(categoryId: String): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE id = :itemId")
  suspend fun getItem(itemId: String): ItemRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ItemRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItems(items: List<ItemRoomEntity>)

  @Update
  suspend fun updateItem(item: ItemRoomEntity)

  @Query("DELETE FROM items WHERE id = :itemId")
  suspend fun deleteItem(itemId: String)

  // Backup & Restore methods
  @Query("SELECT * FROM items")
  suspend fun getAllItemsSync(): List<ItemRoomEntity>

  @Query("DELETE FROM items")
  suspend fun clearAllItems()
}
