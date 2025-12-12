package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.listOf
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FakeProductDataSource(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) : ProductDataSource {

  override val products: StateFlow<List<ProductWithInstances>> = MutableStateFlow(generateFakeProducts())

  @OptIn(ExperimentalUuidApi::class)
  private fun generateFakeProducts(): List<ProductWithInstances> {
    return expiredProducts() + expiringSoonProducts() + freshProducts()
  }

  @OptIn(ExperimentalUuidApi::class)
  private fun expiredProducts(): List<ProductWithInstances> = listOf(
    ProductWithInstances(
      product = Product(
        id = "1",
        name = "Milk",
        description = "Fresh whole milk",
      ),
      instances = List(Random.nextInt(1, 4)) { seed ->
        val expirationDays = Random(seed).nextInt(2, 7).days

        ProductInstance(
          id = (seed).toString(),
          identifier = Uuid.random().toHexDashString().split("-")[0],
          expirationDate = appClock.now().minus(expirationDays),
        )
      },
    ),
    ProductWithInstances(
      product = Product(
        id = "2",
        name = "Strawberries",
        description = "Fresh strawberries",
      ),
      instances = List(Random.nextInt(3, 9)) { seed ->
        val expirationDays = Random(seed).nextInt(2, 12).days

        ProductInstance(
          id = (seed).toString(),
          identifier = Uuid.random().toHexDashString().split("-")[0],
          expirationDate = appClock.now().minus(expirationDays),
        )
      },
    ),
  )

  @OptIn(ExperimentalUuidApi::class)
  private fun expiringSoonProducts(): List<ProductWithInstances> = listOf(
    ProductWithInstances(
      product = Product(
        id = "1",
        name = "Cheese",
        description = "Cheddar cheese block",
      ),
      instances = List(Random.nextInt(1, 4)) { seed ->
        ProductInstance(
          id = (seed).toString(),
          identifier = Uuid.random().toHexDashString().split("-")[0],
          expirationDate = appClock.now().plus(expirationThresholds.soonExpiringThreshold - 1.days),
        )
      },
    ),
    ProductWithInstances(
      product = Product(
        id = "1",
        name = "Carrots",
        description = "Baby carrots",
      ),
      instances = List(Random.nextInt(1, 6)) { seed ->
        ProductInstance(
          id = (seed).toString(),
          identifier = Uuid.random().toHexDashString().split("-")[0],
          expirationDate = appClock.now().plus(expirationThresholds.soonExpiringThreshold - 1.days),
        )
      },
    ),
  )

  @OptIn(ExperimentalUuidApi::class)
  private fun freshProducts(): List<ProductWithInstances> = listOf(
    ProductWithInstances(
      product = Product(
        id = "1",
        name = "Bread",
        description = "Whole wheat bread",
      ),
      instances = List(Random.nextInt(1, 4)) { seed ->
        ProductInstance(
          id = (seed).toString(),
          identifier = Uuid.random().toHexDashString().split("-")[0],
          expirationDate = appClock.now().plus(expirationThresholds.soonExpiringThreshold + 5.days),
        )
      },
    ),
  )

}