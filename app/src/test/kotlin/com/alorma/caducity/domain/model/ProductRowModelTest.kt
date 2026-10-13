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
  ) = PantryEntry(
    itemId = id,
    productId = "p1",
    name = "Iogurt",
    categoryId = "c1",
    expiration = LocalDate(2026, 10, day),
    status = status,
    frozenSince = null,
    remainingDays = day - 14,
    createdAt = createdAt?.let { Instant.fromEpochMilliseconds(it) },
  )

  @Test
  fun `nearest is the one that expires first, not the first created`() {
    val units = listOf(unit("a", day = 20, createdAt = 1), unit("b", day = 16, createdAt = 2))

    expectThat(units.nearestFirst().itemId).isEqualTo("b")
  }

  @Test
  fun `same date breaks the tie by creation and then by id`() {
    val units = listOf(unit("c", 16, createdAt = 5), unit("b", 16, createdAt = 3), unit("a", 16, createdAt = 3))

    expectThat(units.sortedNearestFirst().map { it.itemId }).isEqualTo(listOf("a", "b", "c"))
  }

  @Test
  fun `row orders the unit ids and takes state and days from the nearest unit`() {
    val units =
      listOf(
        unit("late", day = 25, status = ExpiryStatus.FRESH),
        unit("early", day = 15, status = ExpiryStatus.SOON),
      )

    val row = units.toProductRow(category = null, detail = ProductRowDetail.WithDays)

    expectThat(row.itemIds).isEqualTo(listOf("early", "late"))
    expectThat(row.nearestItemId).isEqualTo("early")
    expectThat(row.unitCount).isEqualTo(2)
    expectThat(row.status).isEqualTo(ExpiryStatus.SOON)
    expectThat(row.remainingDays).isEqualTo(1)
  }
}
