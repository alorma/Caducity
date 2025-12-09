package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class FakeProductDataSource(
  private val appClock: AppClock
) : ProductDataSource {

  private val _products = MutableStateFlow(generateFakeProducts())

  override fun getAllProductsWithInstances(): Flow<List<ProductWithInstances>> {
    return _products
  }

  private fun generateFakeProducts(): List<ProductWithInstances> {
    val now = appClock.now()
    val products = mutableListOf<ProductWithInstances>()
    var productId = 1
    var instanceId = 1

    // EXPIRED SECTION: Products with expired instances
    val expiredProducts = listOf(
      "Milk" to "Fresh whole milk",
      "Yogurt" to "Greek yogurt",
      "Lettuce" to "Romaine lettuce",
    )
    expiredProducts.forEach { (name, description) ->
      products.add(ProductWithInstances(
        product = Product(
          id = (productId++).toString(),
          name = name,
          description = description,
        ),
        instances = listOf(
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now - 5.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 40.days).toEpochMilliseconds()),
          ),
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now - 2.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 35.days).toEpochMilliseconds()),
          ),
        ),
      ))
    }

    // EXPIRING SOON SECTION: Products with instances expiring within 7 days
    val expiringSoonProducts = listOf(
      "Chicken Breast" to "Boneless skinless chicken breast",
      "Strawberries" to "Fresh strawberries",
      "Bread" to "Whole wheat bread",
    )
    expiringSoonProducts.forEach { (name, description) ->
      products.add(ProductWithInstances(
        product = Product(
          id = (productId++).toString(),
          name = name,
          description = description,
        ),
        instances = listOf(
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now + 2.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 30.days).toEpochMilliseconds()),
          ),
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now + 5.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 28.days).toEpochMilliseconds()),
          ),
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now + 6.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 25.days).toEpochMilliseconds()),
          ),
        ),
      ))
    }

    // FRESH SECTION: Products with instances expiring after 7 days
    val freshProducts = listOf(
      "Cheese" to "Cheddar cheese block",
      "Eggs" to "Free range eggs (12 pack)",
      "Butter" to "Unsalted butter",
      "Carrots" to "Baby carrots",
    )
    freshProducts.forEach { (name, description) ->
      products.add(ProductWithInstances(
        product = Product(
          id = (productId++).toString(),
          name = name,
          description = description,
        ),
        instances = listOf(
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now + 15.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 20.days).toEpochMilliseconds()),
          ),
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now + 30.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 10.days).toEpochMilliseconds()),
          ),
          ProductInstance(
            id = (instanceId++).toString(),
            expirationDate = Instant.fromEpochMilliseconds((now + 45.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now - 5.days).toEpochMilliseconds()),
          ),
        ),
      ))
    }

    // EMPTY SECTION: Products with no instances
    val emptyProducts = listOf(
      "Tomatoes" to "Fresh organic tomatoes",
      "Avocados" to "Ripe avocados",
    )
    emptyProducts.forEach { (name, description) ->
      products.add(ProductWithInstances(
        product = Product(
          id = (productId++).toString(),
          name = name,
          description = description,
        ),
        instances = emptyList(),
      ))
    }

    return products
  }

}