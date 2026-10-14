package com.alorma.caducity.ui.screen.transform.picker

import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.toProductGroups
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class IngredientPickerStateTest {
  private fun entry(
    id: String,
    name: String,
    status: ExpiryStatus,
    categoryId: String = "meat",
    productId: String? = name,
    day: Int = 20,
  ) = PantryEntry(
    itemId = id,
    productId = productId,
    name = name,
    categoryId = categoryId,
    expiration = LocalDate(2026, 10, day),
    status = status,
    frozenSince = null,
    remainingDays = null,
    createdAt = null,
  )

  private val entries =
    listOf(
      entry("chicken", "Pollastre sencer", ExpiryStatus.FROZEN),
      entry("ham", "Pernil dolç", ExpiryStatus.SOON, day = 14),
      entry("carrot1", "Pastanagues", ExpiryStatus.FRESH, categoryId = "veg", day = 25),
      entry("carrot2", "Pastanagues", ExpiryStatus.SOON, categoryId = "veg", day = 13),
      entry("old", "Iogurt", ExpiryStatus.EXPIRED, categoryId = "dairy", day = 1),
      entry("milk-old", "Llet", ExpiryStatus.EXPIRED, categoryId = "dairy", day = 2),
      entry("milk", "Llet", ExpiryStatus.FRESH, categoryId = "dairy", day = 30),
    )

  private val categories =
    listOf("meat", "veg", "dairy", "empty").mapIndexed { index, id ->
      Category(id, id, "", CategoryShape.COOKIE, CategoryIcon("set_meal"), index)
    }

  private val groups = entries.toProductGroups()

  private fun names(
    query: String = "",
    status: Set<ExpiryStatus> = emptySet(),
    category: Set<String> = emptySet(),
  ) = groups.filterPickerGroups(query, status, category).map { it.name }

  @Test
  fun `expired units are listed, also in a product that has only expired ones`() {
    val listed = groups.map { it.name }
    expectThat(listed).containsExactlyInAnyOrder("Pollastre sencer", "Pernil dolç", "Pastanagues", "Iogurt", "Llet")
    expectThat(groups.first { it.name == "Llet" }.units.map { it.itemId }).containsExactly("milk-old", "milk")
    expectThat(names(status = setOf(ExpiryStatus.EXPIRED))).containsExactlyInAnyOrder("Iogurt", "Llet")
  }

  @Test
  fun `search ignores case and accents`() {
    expectThat(names(query = "dolc")).containsExactly("Pernil dolç")
    expectThat(names(query = "  PASTA ")).containsExactly("Pastanagues")
    expectThat(names(query = "xyz")).isEmpty()
  }

  @Test
  fun `status filter keeps products with a unit in any chosen status`() {
    expectThat(names(status = setOf(ExpiryStatus.SOON))).containsExactlyInAnyOrder("Pernil dolç", "Pastanagues")
    expectThat(names(status = setOf(ExpiryStatus.FROZEN))).containsExactly("Pollastre sencer")
    expectThat(names(status = setOf(ExpiryStatus.FROZEN, ExpiryStatus.FRESH)))
      .containsExactlyInAnyOrder("Pollastre sencer", "Pastanagues", "Llet")
    expectThat(names()).hasSize(5)
  }

  @Test
  fun `status filter leaves only the units in that status, so the row shows their status`() {
    val fresh = groups.filterPickerGroups("", setOf(ExpiryStatus.FRESH), emptySet()).first { it.name == "Pastanagues" }
    expectThat(fresh.units.map { it.itemId }).containsExactly("carrot1")
    expectThat(fresh.nearest.status).isEqualTo(ExpiryStatus.FRESH)

    val soon = groups.filterPickerGroups("", setOf(ExpiryStatus.SOON), emptySet()).first { it.name == "Pastanagues" }
    expectThat(soon.units.map { it.itemId }).containsExactly("carrot2")

    val all = groups.first { it.name == "Pastanagues" }
    expectThat(all.units).hasSize(2)
  }

  @Test
  fun `category filter and the other filters combine`() {
    expectThat(names(category = setOf("veg"))).containsExactly("Pastanagues")
    expectThat(names(category = setOf("veg", "dairy"))).containsExactlyInAnyOrder("Pastanagues", "Iogurt", "Llet")
    expectThat(names(category = setOf("meat"), status = setOf(ExpiryStatus.SOON))).containsExactly("Pernil dolç")
    expectThat(names(category = setOf("veg"), query = "pern")).isEmpty()
  }

  @Test
  fun `filter chips only show with more than one option`() {
    val state = IngredientPickerState(groups = groups, categories = categories)
    expectThat(state.showStatusFilter).isTrue()
    expectThat(state.showCategoryFilter).isTrue()
    expectThat(state.availableCategories.map { it.id }).containsExactly("meat", "veg", "dairy")

    val frozenOnly =
      IngredientPickerState(groups = groups.filter { it.name == "Pollastre sencer" }, categories = categories)
    expectThat(frozenOnly.showStatusFilter).isFalse()
    expectThat(frozenOnly.showCategoryFilter).isFalse()
  }

  @Test
  fun `products that own the given units start selected`() {
    expectThat(groups.keysOf(listOf("carrot2", "chicken")))
      .containsExactlyInAnyOrder("Pastanagues", "Pollastre sencer")
    expectThat(groups.keysOf(listOf("gone"))).isEmpty()
  }

  @Test
  fun `a new product enters with its closest unit`() {
    expectThat(groups.chosenItemIds(setOf("Pastanagues", "Llet"), current = emptyList()))
      .containsExactlyInAnyOrder("carrot2", "milk-old")
  }

  @Test
  fun `a new product enters with its closest unit in the filtered status`() {
    expectThat(
      groups.chosenItemIds(setOf("Pastanagues"), current = emptyList(), statusFilters = setOf(ExpiryStatus.FRESH)),
    ).containsExactly("carrot1")
    // A product with none of the chosen statuses falls back to its closest unit.
    expectThat(
      groups.chosenItemIds(setOf("Pastanagues"), current = emptyList(), statusFilters = setOf(ExpiryStatus.FROZEN)),
    ).containsExactly("carrot2")
  }

  @Test
  fun `a product that was already an ingredient keeps its unit`() {
    expectThat(groups.chosenItemIds(setOf("Pastanagues"), current = listOf("carrot1"))).containsExactly("carrot1")
  }

  @Test
  fun `selection survives a filter that hides it`() {
    val state = IngredientPickerState(groups = groups, selectedKeys = setOf("Llet"), query = "pern")
    expectThat(state.visibleGroups.map { it.name }).containsExactly("Pernil dolç")
    expectThat(state.selectedCount).isEqualTo(1)
  }
}
