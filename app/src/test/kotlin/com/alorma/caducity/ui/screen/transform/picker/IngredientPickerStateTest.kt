package com.alorma.caducity.ui.screen.transform.picker

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

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
    packSize = null,
    createdAt = null,
    soonDays = 3,
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

  private val groups = entries.toPickerGroups()

  private fun names(
    query: String = "",
    status: PickerStatusFilter = PickerStatusFilter.All,
    category: String? = null,
  ) = groups.filterPickerGroups(query, status, category).map { it.name }

  @Test
  fun `expired units are not listed and a product with only expired units disappears`() {
    val listed = groups.map { it.name }
    expectThat(listed).containsExactlyInAnyOrder("Pollastre sencer", "Pernil dolç", "Pastanagues", "Llet")
    expectThat(groups.first { it.name == "Llet" }.units.map { it.itemId }).containsExactly("milk")
  }

  @Test
  fun `search ignores case and accents`() {
    expectThat(names(query = "dolc")).containsExactly("Pernil dolç")
    expectThat(names(query = "  PASTA ")).containsExactly("Pastanagues")
    expectThat(names(query = "xyz")).isEmpty()
  }

  @Test
  fun `status filter keeps products with a unit in that status`() {
    expectThat(names(status = PickerStatusFilter.Soon)).containsExactlyInAnyOrder("Pernil dolç", "Pastanagues")
    expectThat(names(status = PickerStatusFilter.Frozen)).containsExactly("Pollastre sencer")
    expectThat(names(status = PickerStatusFilter.Fresh)).containsExactlyInAnyOrder("Pastanagues", "Llet")
  }

  @Test
  fun `category filter and the other filters combine`() {
    expectThat(names(category = "veg")).containsExactly("Pastanagues")
    expectThat(names(category = "meat", status = PickerStatusFilter.Soon)).containsExactly("Pernil dolç")
    expectThat(names(category = "veg", query = "pern")).isEmpty()
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
      .containsExactlyInAnyOrder("carrot2", "milk")
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
