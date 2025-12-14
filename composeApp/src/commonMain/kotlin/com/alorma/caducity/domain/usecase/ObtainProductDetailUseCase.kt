package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainProductDetailUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtainProductDetail(productId: String): Flow<ProductWithInstances?> {
    return productDataSource
      .products
      .map { products ->
        products.firstOrNull { it.product.id == productId }
      }
  }
}
