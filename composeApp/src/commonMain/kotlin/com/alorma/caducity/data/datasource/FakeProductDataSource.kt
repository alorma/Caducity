package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FakeProductDataSource : ProductDataSource {

  private val _products = MutableStateFlow(emptyList<ProductWithInstances>().toImmutableList())
  override val products: StateFlow<ImmutableList<ProductWithInstances>> = _products

  override fun getProduct(productId: String): Flow<Result<ProductWithInstances>> {
    return emptyFlow()
  }

  override suspend fun createProduct(
    product: Product,
    instances: ImmutableList<ProductInstance>,
  ) {

  }
}