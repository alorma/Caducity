package com.alorma.caducity.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alorma.caducity.data.datasource.room.model.ActiveItemRow
import com.alorma.caducity.data.datasource.room.model.CategoryActiveCountRow
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
  @Query("SELECT * FROM items")
  fun getAllItems(): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE categoryId = :categoryId")
  fun getCategoryItems(categoryId: String): Flow<List<ItemRoomEntity>>

  @Query("SELECT * FROM items WHERE consumedDate IS NULL AND discardedDate IS NULL AND transformedDate IS NULL")
  fun getActiveItems(): Flow<List<ItemRoomEntity>>

  @Query(
    "SELECT i.id AS itemId, i.categoryId AS categoryId, i.productId AS productId, " +
      "COALESCE(p.name, i.identifier) AS name, i.expirationDate AS expirationDate, i.pausedDate AS pausedDate, " +
      "i.remainingDays AS remainingDays, i.packSize AS packSize, i.createdAt AS createdAt " +
      "FROM items i LEFT JOIN products p ON p.id = i.productId " +
      "WHERE i.consumedDate IS NULL AND i.discardedDate IS NULL AND i.transformedDate IS NULL",
  )
  fun observeActiveItemRows(): Flow<List<ActiveItemRow>>

  @Query(
    "SELECT categoryId, COUNT(*) AS activeCount FROM items " +
      "WHERE consumedDate IS NULL AND discardedDate IS NULL AND transformedDate IS NULL GROUP BY categoryId",
  )
  fun getActiveCountByCategory(): Flow<List<CategoryActiveCountRow>>

  @Query("SELECT * FROM items WHERE id = :itemId")
  suspend fun getItem(itemId: String): ItemRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ItemRoomEntity)

  @Update
  suspend fun updateItem(item: ItemRoomEntity)

  /** Marks the item as discarded; a frozen item is unfrozen so it no longer counts as paused. */
  @Query("UPDATE items SET discardedDate = :discardedDate, pausedDate = NULL, remainingDays = NULL WHERE id = :itemId")
  suspend fun discardItem(
    itemId: String,
    discardedDate: Long,
  )

  /** Undo of consume/discard: the item becomes active again. */
  @Query("UPDATE items SET consumedDate = NULL, discardedDate = NULL, transformedDate = NULL WHERE id = :itemId")
  suspend fun restoreItem(itemId: String)

  @Query("UPDATE items SET categoryId = :categoryId, productId = :productId WHERE id = :itemId")
  suspend fun moveItem(
    itemId: String,
    categoryId: String,
    productId: String?,
  )

  @Query("DELETE FROM items WHERE id IN (:itemIds)")
  suspend fun deleteItems(itemIds: List<String>)

  @Query("DELETE FROM items WHERE id = :itemId")
  suspend fun deleteItem(itemId: String)

  @Query("UPDATE items SET productId = :toProductId WHERE productId = :fromProductId")
  suspend fun moveItemsToProduct(
    fromProductId: String,
    toProductId: String?,
  )

  // Backup & Restore methods
  @Query("SELECT * FROM items")
  suspend fun getAllItemsSync(): List<ItemRoomEntity>

  @Query("DELETE FROM items")
  suspend fun clearAllItems()
}
