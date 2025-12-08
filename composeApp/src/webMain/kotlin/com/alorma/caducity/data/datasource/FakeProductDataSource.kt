package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.days

class FakeProductDataSource(
  private val appClock: AppClock
) : ProductDataSource {
  private val _products = MutableStateFlow(generateFakeProducts())

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

  private fun generateFakeProducts(): List<Product> {
    val now = appClock.now()

    return listOf(
      Product(
        id = "1",
        name = "Milk",
        description = "Fresh whole milk",
        expirationDate = (now + 2.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "2",
        name = "Bread",
        description = "Whole wheat bread",
        expirationDate = (now + 5.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "3",
        name = "Cheese",
        description = "Cheddar cheese block",
        expirationDate = (now + 14.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "4",
        name = "Yogurt",
        description = "Greek yogurt",
        expirationDate = (now + 7.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "5",
        name = "Eggs",
        description = "Free range eggs (12 pack)",
        expirationDate = (now + 10.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "6",
        name = "Tomatoes",
        description = "Fresh organic tomatoes",
        expirationDate = (now + 3.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "7",
        name = "Chicken Breast",
        description = "Boneless skinless chicken breast",
        expirationDate = (now + 4.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "8",
        name = "Orange Juice",
        description = "Freshly squeezed orange juice",
        expirationDate = (now + 6.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "9",
        name = "Lettuce",
        description = "Romaine lettuce",
        expirationDate = (now + 4.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "10",
        name = "Butter",
        description = "Unsalted butter",
        expirationDate = (now + 21.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "11",
        name = "Ham",
        description = "Sliced deli ham",
        expirationDate = (now - 2.days).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "12",
        name = "Sour Cream",
        description = "Low fat sour cream",
        expirationDate = (now - 5.days).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "13",
        name = "Strawberries",
        description = "Fresh strawberries",
        expirationDate = (now - 1.days).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "14",
        name = "Mayonnaise",
        description = "Classic mayonnaise",
        expirationDate = (now - 3.days).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "15",
        name = "Carrots",
        description = "Baby carrots",
        expirationDate = (now - 7.days).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "16",
        name = "Ground Beef",
        description = "Lean ground beef",
        expirationDate = (now - 4.days).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "17",
        name = "Pasta",
        description = "Whole grain pasta",
        expirationDate = (now + 180.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "18",
        name = "Rice",
        description = "Basmati rice",
        expirationDate = (now + 365.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "19",
        name = "Apples",
        description = "Granny smith apples",
        expirationDate = (now + 12.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "20",
        name = "Bananas",
        description = "Ripe bananas",
        expirationDate = (now + 3.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "21",
        name = "Salmon Fillet",
        description = "Fresh Atlantic salmon",
        expirationDate = (now + 2.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "22",
        name = "Cream Cheese",
        description = "Philadelphia cream cheese",
        expirationDate = (now + 20.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "23",
        name = "Spinach",
        description = "Baby spinach leaves",
        expirationDate = (now + 5.days).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "24",
        name = "Mushrooms",
        description = "White button mushrooms",
        expirationDate = (now + 4.days).toEpochMilliseconds(),
        isExpired = false
      )
    )
  }
}
