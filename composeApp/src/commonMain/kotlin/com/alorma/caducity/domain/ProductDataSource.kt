package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ProductsListFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
  fun getProducts(filter: ProductsListFilter): Flow<ImmutableList<ProductWithInstances>>

  fun getProduct(productId: String): Flow<Result<ProductWithInstances>>

  suspend fun createProduct(product: Product, instances: ImmutableList<ProductInstance>)
}
