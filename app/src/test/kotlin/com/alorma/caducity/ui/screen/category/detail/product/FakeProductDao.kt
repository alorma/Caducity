package com.alorma.caducity.ui.screen.category.detail.product

import com.alorma.caducity.data.datasource.room.dao.ProductDao
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Lightweight fake implementation of ProductDao for testing.
 * Uses in-memory MutableStateFlow for storage.
 */
class FakeProductDao(
  private val productsInMemory: MutableStateFlow<List<ProductRoomEntity>>,
  private val itemsInMemory: MutableStateFlow<List<com.alorma.caducity.data.datasource.room.model.ItemRoomEntity>>
) : ProductDao {

  override fun getProductsByCategory(categoryId: String): Flow<List<ProductRoomEntity>> {
    return productsInMemory.map { products ->
      products.filter { it.categoryId == categoryId }
    }
  }

  override suspend fun getProduct(productId: String): ProductRoomEntity? {
    return productsInMemory.value.firstOrNull { it.id == productId }
  }

  override suspend fun insertProduct(product: ProductRoomEntity) {
    productsInMemory.value += product
  }

  override suspend fun getAllProductsSync(): List<ProductRoomEntity> {
    return productsInMemory.value
  }

  override suspend fun deleteProduct(product: ProductRoomEntity) {
    productsInMemory.value = productsInMemory.value.filterNot { it.id == product.id }
  }

  override suspend fun getActiveItemCount(productId: String): Int {
    return itemsInMemory.value.count {
      it.productId == productId && it.consumedDate == null
    }
  }
}
