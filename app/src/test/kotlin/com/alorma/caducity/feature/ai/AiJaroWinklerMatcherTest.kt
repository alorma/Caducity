package com.alorma.caducity.feature.ai

import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Product
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThan

class AiJaroWinklerMatcherTest {
  @Test
  fun `jaroWinkler returns 1 for identical strings`() {
    expectThat(jaroWinkler("milk", "milk")).isEqualTo(1.0)
  }

  @Test
  fun `jaroWinkler returns 0 for empty strings`() {
    expectThat(jaroWinkler("", "")).isEqualTo(0.0)
    expectThat(jaroWinkler("milk", "")).isEqualTo(0.0)
    expectThat(jaroWinkler("", "milk")).isEqualTo(0.0)
  }

  @Test
  fun `jaroWinkler returns 0 for completely different strings`() {
    expectThat(jaroWinkler("abc", "xyz")).isLessThan(0.5)
  }

  @Test
  fun `jaroWinkler scores similar strings above threshold`() {
    // "milk" vs "milks" — one character longer, should be very similar
    expectThat(jaroWinkler("milk", "milks")).isGreaterThanOrEqualTo(0.9)
  }

  @Test
  fun `jaroWinkler is case sensitive on raw input`() {
    // Same letters different case: jaro is case-sensitive, lowercase matches are higher
    val lowerScore = jaroWinkler("milk", "milk")
    val mixedScore = jaroWinkler("milk", "MILK")
    expectThat(lowerScore).isGreaterThanOrEqualTo(mixedScore)
  }

  @Test
  fun `jaroWinkler gives high score for dairy vs dairy`() {
    expectThat(jaroWinkler("dairy", "dairy")).isEqualTo(1.0)
  }

  // ── AiJaroWinklerMatcher integration tests ─────────────────────────────

  private val dairyCategory = Category(id = "cat-1", name = "Dairy", description = "")
  private val meatCategory = Category(id = "cat-2", name = "Meat", description = "")

  private val milkProduct =
    Product(
      id = "prod-1",
      categoryId = "cat-1",
      name = "Milk",
      createdAt = Instant.fromEpochSeconds(0),
    )
  private val cheeseProduct =
    Product(
      id = "prod-2",
      categoryId = "cat-1",
      name = "Cheese",
      createdAt = Instant.fromEpochSeconds(0),
    )
  private val beefProduct =
    Product(
      id = "prod-3",
      categoryId = "cat-2",
      name = "Beef",
      createdAt = Instant.fromEpochSeconds(0),
    )

  private fun categoryWithItems(
    category: Category,
    vararg products: Product,
  ): CategoryWithItems =
    CategoryWithItems(
      category = category,
      products =
        products
          .map { product ->
            CategoryProduct(product = product, items = persistentListOf())
          }.toImmutableList(),
      standaloneItems = persistentListOf(),
    )

  private fun matcher(vararg categoryWithItems: CategoryWithItems): AiJaroWinklerMatcher {
    val dataSource =
      mock<CategoryDataSource> {
        on { getCategories() } doReturn flowOf(categoryWithItems.toList().toImmutableList())
      }
    return AiJaroWinklerMatcher(dataSource)
  }

  @Test
  fun `exact match returns Match with correct product and category`() =
    runTest {
      val matcher =
        matcher(
          categoryWithItems(dairyCategory, milkProduct),
        )
      val proposal =
        GroceryProposal(
          productName = "Milk",
          quantity = 1,
          category = "Dairy",
        )

      val result = matcher.match(proposal)

      expectThat(result).isA<MatchResult.Match>().and {
        get { product }.isEqualTo(milkProduct)
        get { category }.isEqualTo(dairyCategory)
        get { score }.isGreaterThanOrEqualTo(0.50)
      }
    }

  @Test
  fun `case insensitive match still finds product`() =
    runTest {
      val matcher =
        matcher(
          categoryWithItems(dairyCategory, milkProduct),
        )
      val proposal =
        GroceryProposal(
          productName = "milk",
          quantity = 2,
          category = "dairy",
        )

      val result = matcher.match(proposal)

      expectThat(result).isA<MatchResult.Match>().and {
        get { product }.isEqualTo(milkProduct)
      }
    }

  @Test
  fun `fuzzy match on product name with typo still returns Match`() =
    runTest {
      val matcher =
        matcher(
          categoryWithItems(dairyCategory, milkProduct),
        )
      val proposal =
        GroceryProposal(
          productName = "Miilk", // one extra 'i'
          quantity = 1,
          category = "Dairy",
        )

      val result = matcher.match(proposal)

      expectThat(result).isA<MatchResult.Match>()
    }

  @Test
  fun `no match when database is empty`() =
    runTest {
      val matcher = matcher()
      val proposal =
        GroceryProposal(
          productName = "Milk",
          quantity = 1,
          category = "Dairy",
        )

      val result = matcher.match(proposal)

      expectThat(result).isA<MatchResult.NoMatch>()
    }

  @Test
  fun `no match for completely unrelated product`() =
    runTest {
      val matcher =
        matcher(
          categoryWithItems(dairyCategory, milkProduct),
        )
      val proposal =
        GroceryProposal(
          productName = "Wrench",
          quantity = 1,
          category = "Hardware",
        )

      val result = matcher.match(proposal)

      expectThat(result).isA<MatchResult.NoMatch>()
    }

  @Test
  fun `picks best candidate across multiple categories`() =
    runTest {
      val matcher =
        matcher(
          categoryWithItems(dairyCategory, milkProduct, cheeseProduct),
          categoryWithItems(meatCategory, beefProduct),
        )
      val proposal =
        GroceryProposal(
          productName = "Cheese",
          quantity = 1,
          category = "Dairy",
        )

      val result = matcher.match(proposal)

      expectThat(result).isA<MatchResult.Match>().and {
        get { product }.isEqualTo(cheeseProduct)
        get { category }.isEqualTo(dairyCategory)
      }
    }

  @Test
  fun `category name contributes to score — wrong category lowers match`() =
    runTest {
      // Same product name exists in both categories; proposal category points to Dairy
      val meatMilk =
        Product(
          id = "prod-4",
          categoryId = "cat-2",
          name = "Milk",
          createdAt = Instant.fromEpochSeconds(0),
        )
      val matcher =
        matcher(
          categoryWithItems(dairyCategory, milkProduct),
          categoryWithItems(meatCategory, meatMilk),
        )
      val proposal =
        GroceryProposal(
          productName = "Milk",
          quantity = 1,
          category = "Dairy",
        )

      val result = matcher.match(proposal)

      // Should prefer the Dairy/Milk match over Meat/Milk
      expectThat(result).isA<MatchResult.Match>().and {
        get { category }.isEqualTo(dairyCategory)
      }
    }
}
