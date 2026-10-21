package com.alorma.caducity.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ProductRowModelTest {
  private fun unit(
    id: String,
    day: Int,
    createdAt: Long? = null,
    status: ExpiryStatus = ExpiryStatus.FRESH,
    frozenSince: String? = null,
  ) = PantryEntry(
    itemId = id,
    productId = "p1",
    name = "Iogurt",
    categoryId = "c1",
    expiration = LocalDate(2026, 10, day),
    status = status,
    frozenSince = frozenSince?.let { Instant.parse(it) },
    remainingDays = day - 14,
    createdAt = createdAt?.let { Instant.fromEpochMilliseconds(it) },
  )

  @Test
  fun `nearest is the one that expires first, not the first created`() {
    val units = listOf(unit("a", day = 20, createdAt = 1), unit("b", day = 16, createdAt = 2))

    expectThat(units.nearestFirst().itemId).isEqualTo("b")
  }

  @Test
  fun `frozen units go oldest in the freezer first and the row shows that freezing day`() {
    val units =
      listOf(
        unit("new", day = 16, status = ExpiryStatus.FROZEN, frozenSince = "2026-10-08T10:00:00Z"),
        unit("old", day = 30, status = ExpiryStatus.FROZEN, frozenSince = "2026-10-04T10:00:00Z"),
      )

    expectThat(units.nearestFirst().itemId).isEqualTo("old")
    val row = units.toProductRow(category = null, detail = ProductRowDetail.WithDays, today = LocalDate(2026, 10, 14))
    expectThat(row.itemIds).isEqualTo(listOf("old", "new"))
    expectThat(row.frozenSince).isEqualTo(LocalDate(2026, 10, 4))
  }

  @Test
  fun `same date breaks the tie by creation and then by id`() {
    val units = listOf(unit("c", 16, createdAt = 5), unit("b", 16, createdAt = 3), unit("a", 16, createdAt = 3))

    expectThat(units.sortedNearestFirst().map { it.itemId }).isEqualTo(listOf("a", "b", "c"))
  }

  @Test
  fun `days come from the date, not from the frozen remaining days, and frozen has none`() {
    val today = LocalDate(2026, 10, 14)
    // remainingDays of a PantryEntry is only meaningful for frozen units: a non-frozen one has none here.
    val soon = listOf(unit("a", day = 16).copy(remainingDays = null, status = ExpiryStatus.SOON))
    val frozen = listOf(unit("b", day = 16, status = ExpiryStatus.FROZEN))

    expectThat(soon.toProductRow(null, ProductRowDetail.WithDays, today).remainingDays).isEqualTo(2)
    expectThat(frozen.toProductRow(null, ProductRowDetail.WithDays, today).remainingDays).isEqualTo(null)
  }

  @Test
  fun `row can be frozen only when the nearest unit can`() {
    val today = LocalDate(2026, 10, 14)
    val cannot = listOf(unit("a", day = 16).copy(canFreeze = false), unit("b", day = 20).copy(canFreeze = true))
    val can = listOf(unit("a", day = 16))

    expectThat(cannot.toProductRow(null, ProductRowDetail.WithDays, today).canFreeze).isEqualTo(false)
    expectThat(can.toProductRow(null, ProductRowDetail.WithDays, today).canFreeze).isEqualTo(true)
  }

  @Test
  fun `row orders the unit ids and takes state and days from the nearest unit`() {
    val units =
      listOf(
        unit("late", day = 25, status = ExpiryStatus.FRESH),
        unit("early", day = 15, status = ExpiryStatus.SOON),
      )

    val row = units.toProductRow(category = null, detail = ProductRowDetail.WithDays, today = LocalDate(2026, 10, 14))

    expectThat(row.itemIds).isEqualTo(listOf("early", "late"))
    expectThat(row.nearestItemId).isEqualTo("early")
    expectThat(row.unitCount).isEqualTo(2)
    expectThat(row.status).isEqualTo(ExpiryStatus.SOON)
    expectThat(row.remainingDays).isEqualTo(1)
  }
}
