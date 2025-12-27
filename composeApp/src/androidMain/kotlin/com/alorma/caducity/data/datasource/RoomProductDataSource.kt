package com.alorma.caducity.data.datasource

import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class RoomProductDataSource : ProductDataSource {

  private val _products = MutableStateFlow<ImmutableList<ProductWithInstances>>(persistentListOf())

  override val products: StateFlow<ImmutableList<ProductWithInstances>> = _products

  override fun getProduct(productId: String): Flow<Result<ProductWithInstances>> {
    return products.map { productList ->
      productList.firstOrNull { it.product.id == productId }
        ?.let { Result.success(it) }
        ?: Result.failure(NoSuchElementException("Product with id $productId not found"))
    }
  }
}
