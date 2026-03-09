package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ProductDeletionStrategy

class DeleteProductUseCase(
  private val productDataSource: ProductDataSource,
  private val itemDao: ItemDao,
) {
  suspend fun delete(
    productId: String,
    strategy: ProductDeletionStrategy = ProductDeletionStrategy.CascadeDelete,
  ): Result<Unit> =
    try {
      when (strategy) {
        is ProductDeletionStrategy.CascadeDelete -> {
          // Delete all active items first, then delete the product
          itemDao.deleteActiveItemsByProduct(productId)
          productDataSource.deleteProduct(productId)
        }
        is ProductDeletionStrategy.MoveToStandalone -> {
          // Move items to standalone (productId = null), then delete the product
          productDataSource.moveItemsToProduct(productId, null)
          productDataSource.deleteProduct(productId)
        }
        is ProductDeletionStrategy.MoveToProduct -> {
          // Move items to another product, then delete the product
          productDataSource.moveItemsToProduct(productId, strategy.targetProductId)
          productDataSource.deleteProduct(productId)
        }
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  suspend fun getActiveItemCount(productId: String): Int = productDataSource.getActiveItemCount(productId)
}
