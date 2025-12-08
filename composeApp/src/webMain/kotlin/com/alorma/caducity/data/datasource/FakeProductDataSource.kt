package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

class FakeProductDataSource : ProductDataSource {
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
    val now = Clock.System.now()
    val timezone = TimeZone.currentSystemDefault()

    return listOf(
      Product(
        id = "1",
        name = "Milk",
        description = "Fresh whole milk",
        expirationDate = now.plus(2, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "2",
        name = "Bread",
        description = "Whole wheat bread",
        expirationDate = now.plus(5, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "3",
        name = "Cheese",
        description = "Cheddar cheese block",
        expirationDate = now.plus(14, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "4",
        name = "Yogurt",
        description = "Greek yogurt",
        expirationDate = now.plus(7, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "5",
        name = "Eggs",
        description = "Free range eggs (12 pack)",
        expirationDate = now.plus(10, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "6",
        name = "Tomatoes",
        description = "Fresh organic tomatoes",
        expirationDate = now.plus(3, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "7",
        name = "Chicken Breast",
        description = "Boneless skinless chicken breast",
        expirationDate = now.plus(4, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "8",
        name = "Orange Juice",
        description = "Freshly squeezed orange juice",
        expirationDate = now.plus(6, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "9",
        name = "Lettuce",
        description = "Romaine lettuce",
        expirationDate = now.plus(4, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "10",
        name = "Butter",
        description = "Unsalted butter",
        expirationDate = now.plus(21, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "11",
        name = "Ham",
        description = "Sliced deli ham",
        expirationDate = now.plus(-2, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "12",
        name = "Sour Cream",
        description = "Low fat sour cream",
        expirationDate = now.plus(-5, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "13",
        name = "Strawberries",
        description = "Fresh strawberries",
        expirationDate = now.plus(-1, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "14",
        name = "Mayonnaise",
        description = "Classic mayonnaise",
        expirationDate = now.plus(-3, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "15",
        name = "Carrots",
        description = "Baby carrots",
        expirationDate = now.plus(-7, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "16",
        name = "Ground Beef",
        description = "Lean ground beef",
        expirationDate = now.plus(-4, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = true
      ),
      Product(
        id = "17",
        name = "Pasta",
        description = "Whole grain pasta",
        expirationDate = now.plus(180, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "18",
        name = "Rice",
        description = "Basmati rice",
        expirationDate = now.plus(365, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "19",
        name = "Apples",
        description = "Granny smith apples",
        expirationDate = now.plus(12, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "20",
        name = "Bananas",
        description = "Ripe bananas",
        expirationDate = now.plus(3, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "21",
        name = "Salmon Fillet",
        description = "Fresh Atlantic salmon",
        expirationDate = now.plus(2, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "22",
        name = "Cream Cheese",
        description = "Philadelphia cream cheese",
        expirationDate = now.plus(20, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "23",
        name = "Spinach",
        description = "Baby spinach leaves",
        expirationDate = now.plus(5, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      ),
      Product(
        id = "24",
        name = "Mushrooms",
        description = "White button mushrooms",
        expirationDate = now.plus(4, DateTimeUnit.DAY, timezone).toEpochMilliseconds(),
        isExpired = false
      )
    )
  }
}
