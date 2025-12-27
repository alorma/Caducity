package com.alorma.caducity.domain.usecase

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateProductUseCase(
  private val productDataSource: ProductDataSource,
) {

  @OptIn(ExperimentalUuidApi::class)
  suspend fun createProduct(
    name: String,
    description: String,
    expirationDate: Instant,
  ): Result<String> {
    return try {
      val productId = Uuid.random().toString()
      val product = Product(
        id = productId,
        name = name,
        description = description,
      )

      val instanceId = Uuid.random().toString()
      val instance = ProductInstance(
        id = instanceId,
        identifier = "Instance 1",
        expirationDate = expirationDate,
      )

      productDataSource.createProduct(product, instance)
      Result.success(productId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
