package com.alorma.caducity.data.datasource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VariantDao {

  @Query("SELECT * FROM variants WHERE productId = :productId ORDER BY name ASC")
  fun getVariantsByProduct(productId: String): Flow<List<VariantRoomEntity>>

  @Query("SELECT * FROM variants WHERE id = :variantId")
  suspend fun getVariant(variantId: String): VariantRoomEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVariant(variant: VariantRoomEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVariants(variants: List<VariantRoomEntity>)

  @Query("DELETE FROM variants WHERE id = :variantId")
  suspend fun deleteVariant(variantId: String)

  @Query("SELECT COUNT(*) FROM product_instances WHERE variantId = :variantId AND consumedDate IS NULL")
  suspend fun getActiveInstanceCount(variantId: String): Int

  @Query("SELECT * FROM variants")
  suspend fun getAllVariantsSync(): List<VariantRoomEntity>

  @Query("DELETE FROM variants")
  suspend fun clearAllVariants()
}
