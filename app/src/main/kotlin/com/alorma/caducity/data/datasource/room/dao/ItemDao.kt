package com.alorma.caducity.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
  @Query("SELECT * FROM items")
  fun getAllItems(): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId")
  fun getCategoryItems(categoryId: String): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId AND productId = :productId")
  fun getProductItems(
    categoryId: String,
    productId: String,
  ): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId AND productId IS NULL")
  fun getStandaloneItems(categoryId: String): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE id = :itemId")
  suspend fun getItem(itemId: String): ItemRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ItemRoomEntity)

  @Update
  suspend fun updateItem(item: ItemRoomEntity)

  @Query("DELETE FROM items WHERE id = :itemId")
  suspend fun deleteItem(itemId: String)

  @Query("UPDATE items SET productId = :toProductId WHERE productId = :fromProductId")
  suspend fun moveItemsToProduct(
    fromProductId: String,
    toProductId: String?,
  )

  @Query("DELETE FROM items WHERE productId = :productId AND consumedDate IS NULL")
  suspend fun deleteActiveItemsByProduct(productId: String)

  @Query("DELETE FROM items WHERE categoryId = :categoryId AND productId = :productId AND consumedDate IS NOT NULL")
  suspend fun deleteConsumedItemsByProduct(
    categoryId: String,
    productId: String,
  )

  @Query("DELETE FROM items WHERE categoryId = :categoryId AND productId = :productId")
  suspend fun deleteAllItemsByProduct(
    categoryId: String,
    productId: String,
  )

  @Query("DELETE FROM items WHERE categoryId = :categoryId AND productId IS NULL AND consumedDate IS NOT NULL")
  suspend fun deleteConsumedStandaloneItems(categoryId: String)

  @Query("DELETE FROM items WHERE categoryId = :categoryId AND productId IS NULL")
  suspend fun deleteAllStandaloneItems(categoryId: String)

  // Backup & Restore methods
  @Query("SELECT * FROM items")
  suspend fun getAllItemsSync(): List<ItemRoomEntity>

  @Query("DELETE FROM items")
  suspend fun clearAllItems()
}
