package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.Product
import kotlinx.coroutines.flow.Flow

class GetCategoryProductsUseCase(
  private val productDataSource: ProductDataSource,
) {
  fun obtain(categoryId: String): Flow<List<Product>> {
    return productDataSource.getProductsByCategory(categoryId)
  }
}
