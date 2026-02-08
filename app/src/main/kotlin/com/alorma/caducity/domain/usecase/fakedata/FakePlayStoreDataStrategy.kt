package com.alorma.caducity.domain.usecase.fakedata

import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Strategy for generating consistent fake data for PlayStore screenshots
 *
 * Creates:
 * - 5 fixed categories (always the same)
 * - Predefined products per category
 * - Items with varied expiration dates relative to current time
 * - Different item distribution per category for interesting screenshots
 *
 * The data is consistent (same categories/products) but item states vary based on creation time,
 * allowing for reproducible yet time-appropriate screenshots.
 */
class FakePlayStoreDataStrategy : FakeDataStrategy {

  override fun getCategoryConfigs(now: Instant): List<CategoryConfig> {
    return listOf(
      createDairyCategory(now),
      createProduceCategory(now),
      createMeatCategory(now),
      createPantryCategory(now),
      createFrozenCategory(now)
    )
  }

  private fun createDairyCategory(now: Instant): CategoryConfig {
    return CategoryConfig(
      name = "Dairy",
      description = "Milk, cheese, yogurt and other dairy products",
      products = listOf(
        ProductConfig(
          name = "Whole Milk",
          items = listOf(
            ItemConfig("Whole Milk #1", now.plus(2.days)),
            ItemConfig("Whole Milk #2", now.plus(5.days)),
            ItemConfig("Whole Milk #3", now.plus(8.days))
          )
        ),
        ProductConfig(
          name = "Cheddar Cheese",
          items = listOf(
            ItemConfig("Cheddar Cheese #1", now.plus(10.days)),
            ItemConfig("Cheddar Cheese #2", now.plus(15.days)),
            ItemConfig("Cheddar Cheese #3", now.plus(20.days))
          )
        ),
        ProductConfig(
          name = "Greek Yogurt",
          items = listOf(
            ItemConfig("Greek Yogurt #1", now.plus(3.days)),
            ItemConfig("Greek Yogurt #2", now.plus(7.days)),
            ItemConfig("Greek Yogurt #3", now.minus(1.days))
          )
        )
      ),
      standaloneItems = listOf(
        StandaloneItemConfig("Butter", now.plus(10.days)),
        StandaloneItemConfig("Sour Cream", now.plus(5.days))
      )
    )
  }

  private fun createProduceCategory(now: Instant): CategoryConfig {
    return CategoryConfig(
      name = "Produce",
      description = "Fresh fruits and vegetables",
      products = listOf(
        ProductConfig(
          name = "Apples",
          items = listOf(
            ItemConfig("Apples #1", now.plus(5.days)),
            ItemConfig("Apples #2", now.plus(10.days)),
            ItemConfig("Apples #3", now.plus(15.days))
          )
        ),
        ProductConfig(
          name = "Carrots",
          items = listOf(
            ItemConfig("Carrots #1", now.plus(8.days)),
            ItemConfig("Carrots #2", now.plus(12.days)),
            ItemConfig("Carrots #3", now.minus(2.days))
          )
        ),
        ProductConfig(
          name = "Spinach",
          items = listOf(
            ItemConfig("Spinach #1", now.plus(2.days)),
            ItemConfig("Spinach #2", now.plus(4.days)),
            ItemConfig("Spinach #3", now.plus(6.days))
          )
        ),
        ProductConfig(
          name = "Tomatoes",
          items = listOf(
            ItemConfig("Tomatoes #1", now.plus(3.days)),
            ItemConfig("Tomatoes #2", now.minus(1.days)),
            ItemConfig("Tomatoes #3", now.plus(7.days))
          )
        )
      ),
      standaloneItems = listOf(
        StandaloneItemConfig("Bananas", now.plus(3.days)),
        StandaloneItemConfig("Lettuce", now.plus(4.days))
      )
    )
  }

  private fun createMeatCategory(now: Instant): CategoryConfig {
    return CategoryConfig(
      name = "Meat & Seafood",
      description = "Fresh and frozen meats and seafood",
      products = listOf(
        ProductConfig(
          name = "Chicken Breast",
          items = listOf(
            ItemConfig("Chicken Breast #1", now.plus(2.days)),
            ItemConfig("Chicken Breast #2", now.plus(4.days)),
            ItemConfig("Chicken Breast #3", now.minus(1.days))
          )
        ),
        ProductConfig(
          name = "Ground Beef",
          items = listOf(
            ItemConfig("Ground Beef #1", now.plus(3.days)),
            ItemConfig("Ground Beef #2", now.plus(5.days)),
            ItemConfig("Ground Beef #3", now.plus(7.days))
          )
        ),
        ProductConfig(
          name = "Salmon Fillet",
          items = listOf(
            ItemConfig("Salmon Fillet #1", now.plus(2.days)),
            ItemConfig("Salmon Fillet #2", now.minus(2.days)),
            ItemConfig("Salmon Fillet #3", now.plus(6.days))
          )
        )
      ),
      standaloneItems = listOf(
        StandaloneItemConfig("Bacon", now.plus(7.days)),
        StandaloneItemConfig("Shrimp", now.minus(1.days))
      )
    )
  }

  private fun createPantryCategory(now: Instant): CategoryConfig {
    return CategoryConfig(
      name = "Pantry",
      description = "Dry goods, canned foods, and condiments",
      products = listOf(
        ProductConfig(
          name = "Pasta",
          items = listOf(
            ItemConfig("Pasta #1", now.plus(60.days)),
            ItemConfig("Pasta #2", now.plus(90.days)),
            ItemConfig("Pasta #3", now.plus(120.days))
          )
        ),
        ProductConfig(
          name = "Rice",
          items = listOf(
            ItemConfig("Rice #1", now.plus(180.days)),
            ItemConfig("Rice #2", now.plus(240.days)),
            ItemConfig("Rice #3", now.plus(300.days))
          )
        ),
        ProductConfig(
          name = "Tomato Sauce",
          items = listOf(
            ItemConfig("Tomato Sauce #1", now.plus(30.days)),
            ItemConfig("Tomato Sauce #2", now.plus(45.days)),
            ItemConfig("Tomato Sauce #3", now.minus(5.days))
          )
        ),
        ProductConfig(
          name = "Olive Oil",
          items = listOf(
            ItemConfig("Olive Oil #1", now.plus(90.days)),
            ItemConfig("Olive Oil #2", now.plus(150.days)),
            ItemConfig("Olive Oil #3", now.plus(210.days))
          )
        )
      ),
      standaloneItems = listOf(
        StandaloneItemConfig("Bread", now.plus(2.days)),
        StandaloneItemConfig("Crackers", now.plus(14.days))
      )
    )
  }

  private fun createFrozenCategory(now: Instant): CategoryConfig {
    return CategoryConfig(
      name = "Frozen Foods",
      description = "Frozen meals, vegetables, and desserts",
      products = listOf(
        ProductConfig(
          name = "Pizza",
          items = listOf(
            ItemConfig("Pizza #1", now.plus(30.days), shouldFreeze = true, remainingDaysWhenFrozen = 30),
            ItemConfig("Pizza #2", now.plus(60.days)),
            ItemConfig("Pizza #3", now.plus(90.days))
          )
        ),
        ProductConfig(
          name = "Ice Cream",
          items = listOf(
            ItemConfig("Ice Cream #1", now.plus(45.days), shouldFreeze = true, remainingDaysWhenFrozen = 45),
            ItemConfig("Ice Cream #2", now.plus(75.days)),
            ItemConfig("Ice Cream #3", now.plus(105.days))
          )
        ),
        ProductConfig(
          name = "Frozen Peas",
          items = listOf(
            ItemConfig("Frozen Peas #1", now.plus(120.days), shouldFreeze = true, remainingDaysWhenFrozen = 120),
            ItemConfig("Frozen Peas #2", now.plus(180.days)),
            ItemConfig("Frozen Peas #3", now.plus(240.days))
          )
        )
      ),
      standaloneItems = listOf(
        StandaloneItemConfig("Waffles", now.plus(60.days)),
        StandaloneItemConfig("French Fries", now.plus(90.days))
      )
    )
  }
}
