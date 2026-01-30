package com.alorma.caducity.data.datasource

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.toModel
import com.alorma.caducity.data.datasource.room.toRoomEntity
import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.domain.model.Variant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomProductDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
) : VariantDataSource {

  private val productDao = database.productDao()

  override fun getVariantsByProduct(productId: String): Flow<ImmutableList<Variant>> {
    return productDao.getProductsByCategory(productId)
      .map { entities ->
        entities.map { it.toModel() }.toImmutableList()
      }
  }

  override suspend fun getVariant(variantId: String): Variant? {
    return productDao.getProduct(variantId)?.toModel()
  }

  override suspend fun createVariant(productId: String, name: String): Variant {
    val variant = Variant(
      id = UUID.randomUUID().toString(),
      productId = productId,
      name = name,
      createdAt = appClock.now(),
    )
    productDao.insertProduct(variant.toRoomEntity())
    return variant
  }

  override suspend fun deleteVariant(variantId: String): Result<Unit> {
    val instanceCount = productDao.getActiveItemCount(variantId)
    return if (instanceCount > 0) {
      Result.failure(IllegalStateException("Cannot delete variant with active instances"))
    } else {
      productDao.deleteProduct(variantId)
      Result.success(Unit)
    }
  }

  override suspend fun getActiveInstanceCount(variantId: String): Int {
    return productDao.getActiveItemCount(variantId)
  }

  override suspend fun clearAllVariants() {
    productDao.clearAllProducts()
  }
}
