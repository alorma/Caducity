package com.alorma.caducity.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductUsageRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
  fun getProductsByCategory(categoryId: String): Flow<List<ProductRoomEntity>>

  @Query("SELECT * FROM products WHERE id = :productId")
  suspend fun getProduct(productId: String): ProductRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductRoomEntity)

  @Query("SELECT * FROM products")
  suspend fun getAllProductsSync(): List<ProductRoomEntity>

  @Query("SELECT * FROM products WHERE categoryId = :categoryId")
  suspend fun getProductsByCategorySync(categoryId: String): List<ProductRoomEntity>

  /** Usage stats per product (any item, including consumed and discarded) for name suggestions. */
  @Query(
    "SELECT p.id AS productId, p.name AS name, p.categoryId AS categoryId, COUNT(i.id) AS uses, " +
      "COALESCE(MAX(COALESCE(i.createdAt, i.expirationDate)), p.createdAt) AS lastUsed " +
      "FROM products p LEFT JOIN items i ON i.productId = p.id GROUP BY p.id",
  )
  suspend fun getProductUsage(): List<ProductUsageRow>

  /** Removes a product only if no item references it (used when undoing an add that created it). */
  @Query("DELETE FROM products WHERE id = :productId AND NOT EXISTS (SELECT 1 FROM items WHERE productId = :productId)")
  suspend fun deleteProductIfUnused(productId: String)

  @Delete
  suspend fun deleteProduct(product: ProductRoomEntity)

  @Query(
    "SELECT COUNT(*) FROM items WHERE productId = :productId " +
      "AND consumedDate IS NULL AND discardedDate IS NULL AND transformedDate IS NULL",
  )
  suspend fun getActiveItemCount(productId: String): Int
}
