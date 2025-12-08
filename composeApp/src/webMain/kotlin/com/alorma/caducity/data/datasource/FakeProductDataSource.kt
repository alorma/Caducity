package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant
import kotlin.time.Duration.Companion.days

class FakeProductDataSource(
  private val appClock: AppClock
) : ProductDataSource {
  private val _products = MutableStateFlow(generateFakeProducts())
  private val _productInstances = MutableStateFlow(generateFakeProductInstances())

  override fun getAllProducts(): Flow<List<Product>> = _products.asStateFlow()

  override suspend fun getProductById(id: String): Product? {
    return _products.value.find { it.id == id }
  }

  override suspend fun insertProduct(product: Product) {
    _products.update { currentList ->
      currentList + product
    }
  }

  override suspend fun deleteProduct(id: String) {
    _products.update { currentList ->
      currentList.filterNot { it.id == id }
    }
  }

  override suspend fun updateProduct(product: Product) {
    _products.update { currentList ->
      currentList.map { if (it.id == product.id) product else it }
    }
  }

  override fun getAllProductInstances(): Flow<List<ProductInstance>> =
    _productInstances.asStateFlow()

  override fun getProductInstancesByProductId(productId: String): Flow<List<ProductInstance>> =
    _productInstances.map { instances ->
      instances.filter { it.productId == productId }
    }

  override suspend fun getProductInstanceById(id: String): ProductInstance? {
    return _productInstances.value.find { it.id == id }
  }

  override suspend fun insertProductInstance(instance: ProductInstance) {
    _productInstances.update { currentList ->
      currentList + instance
    }
  }

  override suspend fun deleteProductInstance(id: String) {
    _productInstances.update { currentList ->
      currentList.filterNot { it.id == id }
    }
  }

  override suspend fun updateProductInstance(instance: ProductInstance) {
    _productInstances.update { currentList ->
      currentList.map { if (it.id == instance.id) instance else it }
    }
  }

  private fun generateFakeProducts(): List<Product> {
    val allProducts = listOf(
      "Milk" to "Fresh whole milk",
      "Bread" to "Whole wheat bread",
      "Cheese" to "Cheddar cheese block",
      "Yogurt" to "Greek yogurt",
      "Eggs" to "Free range eggs (12 pack)",
      "Tomatoes" to "Fresh organic tomatoes",
      "Chicken Breast" to "Boneless skinless chicken breast",
      "Orange Juice" to "Freshly squeezed orange juice",
      "Lettuce" to "Romaine lettuce",
      "Butter" to "Unsalted butter",
      "Ham" to "Sliced deli ham",
      "Sour Cream" to "Low fat sour cream",
      "Strawberries" to "Fresh strawberries",
      "Mayonnaise" to "Classic mayonnaise",
      "Carrots" to "Baby carrots",
      "Ground Beef" to "Lean ground beef",
      "Pasta" to "Whole grain pasta",
      "Rice" to "Basmati rice",
      "Apples" to "Granny smith apples",
      "Bananas" to "Ripe bananas",
      "Salmon Fillet" to "Fresh Atlantic salmon",
      "Cream Cheese" to "Philadelphia cream cheese",
      "Spinach" to "Baby spinach leaves",
      "Mushrooms" to "White button mushrooms",
      "Avocados" to "Ripe avocados",
      "Turkey Slices" to "Deli turkey slices",
      "Cottage Cheese" to "Low fat cottage cheese",
      "Bell Peppers" to "Mixed bell peppers",
      "Broccoli" to "Fresh broccoli florets",
      "Bacon" to "Hickory smoked bacon"
    )

    // Randomly select 15-25 products
    val selectedProducts = allProducts.shuffled().take((15..25).random())

    return selectedProducts.mapIndexed { index, (name, description) ->
      Product(
        id = (index + 1).toString(),
        name = name,
        description = description,
      )
    }
  }

  private fun generateFakeProductInstances(): List<ProductInstance> {
    val now = appClock.now()
    val products = _products.value
    val instances = mutableListOf<ProductInstance>()
    var instanceId = 1

    // Generate 1-4 instances per product
    products.forEach { product ->
      val instanceCount = (1..4).random()
      repeat(instanceCount) {
        // Random expiration: 60% future (active), 40% past (expired)
        val isExpired = kotlin.random.Random.nextFloat() < 0.4f
        val daysOffset = if (isExpired) {
          // Expired: 1-10 days ago
          -(1..10).random()
        } else {
          // Active: 1-30 days in the future
          (1..30).random()
        }

        // Purchase date is 30-60 days before expiration
        val purchaseDaysBeforeExpiration = (30..60).random()

        instances.add(
          ProductInstance(
            id = instanceId.toString(),
            productId = product.id,
            expirationDate = Instant.fromEpochMilliseconds((now + daysOffset.days).toEpochMilliseconds()),
            purchaseDate = Instant.fromEpochMilliseconds((now + daysOffset.days - purchaseDaysBeforeExpiration.days).toEpochMilliseconds()),
          )
        )
        instanceId++
      }
    }

    return instances
  }
}
