package com.alorma.caducity.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

  @Transaction
  @Query("SELECT * FROM products")
  fun getAllProductsWithInstances(): Flow<List<ProductWithInstancesRoomEntity>>

  @Transaction
  @Query("""
    SELECT DISTINCT p.* FROM products p
    INNER JOIN product_instances pi ON p.id = pi.productId
    WHERE CASE
            WHEN pi.pausedDate IS NOT NULL THEN pi.pausedDate
            ELSE pi.expirationDate
          END >= :startDate
      AND CASE
            WHEN pi.pausedDate IS NOT NULL THEN pi.pausedDate
            ELSE pi.expirationDate
          END < :endDate
      AND pi.consumedDate IS NULL
  """)
  fun getProductsWithInstancesByDateRange(startDate: Long, endDate: Long): Flow<List<ProductWithInstancesRoomEntity>>

  @Transaction
  @Query("""
    SELECT DISTINCT p.* FROM products p
    INNER JOIN product_instances pi ON p.id = pi.productId
    WHERE CASE
            WHEN pi.pausedDate IS NOT NULL THEN pi.pausedDate
            ELSE pi.expirationDate
          END >= :date
      AND CASE
            WHEN pi.pausedDate IS NOT NULL THEN pi.pausedDate
            ELSE pi.expirationDate
          END < :nextDay
      AND pi.consumedDate IS NULL
  """)
  fun getProductsWithInstancesByDate(date: Long, nextDay: Long): Flow<List<ProductWithInstancesRoomEntity>>

  @Transaction
  @Query("SELECT * FROM products WHERE id = :productId")
  fun getProductWithInstances(productId: String): Flow<ProductWithInstancesRoomEntity?>

  @Query("SELECT * FROM products")
  fun getAllProducts(): Flow<List<ProductRoomEntity>>

  @Query("SELECT * FROM products WHERE id = :productId")
  suspend fun getProduct(productId: String): ProductRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<ProductRoomEntity>)

  @Query("DELETE FROM products WHERE id = :productId")
  suspend fun deleteProduct(productId: String)

  @Query("SELECT * FROM product_instances WHERE productId = :productId")
  fun getProductInstances(productId: String): Flow<List<ProductInstanceRoomEntity>>

  @Query("SELECT * FROM product_instances WHERE id = :instanceId")
  suspend fun getProductInstance(instanceId: String): ProductInstanceRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductInstance(instance: ProductInstanceRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProductInstances(instances: List<ProductInstanceRoomEntity>)

  @Update
  suspend fun updateProductInstance(instance: ProductInstanceRoomEntity)

  @Query("DELETE FROM product_instances WHERE id = :instanceId")
  suspend fun deleteProductInstance(instanceId: String)

  // Backup & Restore methods
  @Query("SELECT * FROM products")
  suspend fun getAllProductsSync(): List<ProductRoomEntity>

  @Query("SELECT * FROM product_instances")
  suspend fun getAllProductInstancesSync(): List<ProductInstanceRoomEntity>

  @Query("DELETE FROM products")
  suspend fun clearAllProducts()

  @Query("DELETE FROM product_instances")
  suspend fun clearAllProductInstances()
}
