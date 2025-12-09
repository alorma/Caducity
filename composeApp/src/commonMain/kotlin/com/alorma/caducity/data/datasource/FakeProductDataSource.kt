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

    // Select 5-8 products
    val selectedProducts = allProducts.shuffled().take((5..8).random())

    return selectedProducts.mapIndexed { index, (name, description) ->
      ProductWithInstances(
        product = Product(
          id = (index + 1).toString(),
          name = name,
          description = description,
        ),
        instances = generateFakeProductInstances(),
      )
    }
  }

  private fun generateFakeProductInstances(): List<ProductInstance> {
    val now = appClock.now()

    // Predefined day offsets for more varied expiration dates
    val expirationOffsets = listOf(
      -15, // Expired 15 days ago
      -7,  // Expired 7 days ago
      -3,  // Expired 3 days ago
      -1,  // Expired yesterday
      1,   // Expires tomorrow
      3,   // Expires in 3 days
      5,   // Expires in 5 days
      9,   // Expires in 9 days
      14,  // Expires in 14 days
      21,  // Expires in 21 days
      30,  // Expires in 30 days
      45,  // Expires in 45 days
      60,  // Expires in 60 days
      90   // Expires in 90 days
    )
    val instanceCount = (3..8).random()
    val shuffledOffsets = expirationOffsets.shuffled()

    return List(instanceCount) { index ->
      val daysOffset = shuffledOffsets[index % shuffledOffsets.size]

      // Purchase date is 30-60 days before expiration
      val purchaseDaysBeforeExpiration = (30..60).random()

      ProductInstance(
        id = index.toString(),
        expirationDate = Instant.fromEpochMilliseconds((now + daysOffset.days).toEpochMilliseconds()),
        purchaseDate = Instant.fromEpochMilliseconds((now + daysOffset.days - purchaseDaysBeforeExpiration.days).toEpochMilliseconds()),
      )
    }
  }
}