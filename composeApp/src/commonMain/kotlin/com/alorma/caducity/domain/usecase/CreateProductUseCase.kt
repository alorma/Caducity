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
    instances: List<Pair<String, Instant>>,
  ): Result<String> {
    return try {
      val productId = Uuid.random().toString()
      val product = Product(
        id = productId,
        name = name,
        description = description,
      )

      val productInstances = instances.map { (identifier, expirationDate) ->
        ProductInstance(
          id = Uuid.random().toString(),
          identifier = identifier,
          expirationDate = expirationDate,
        )
      }

      productDataSource.createProduct(product, productInstances)
      Result.success(productId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
