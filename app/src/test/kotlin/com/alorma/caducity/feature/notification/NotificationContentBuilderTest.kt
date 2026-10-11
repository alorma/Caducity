package com.alorma.caducity.feature.notification

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class NotificationContentBuilderTest {
  private val today = LocalDate(2026, 10, 11)

  private fun entry(
    name: String,
    days: Int,
    status: ExpiryStatus = ExpiryStatus.SOON,
    id: String = "$name-$days",
  ) = PantryEntry(
    itemId = id,
    productId = name,
    name = name,
    categoryId = "c",
    expiration = LocalDate.fromEpochDays(today.toEpochDays() + days),
    status = status,
    frozenSince = null,
    remainingDays = null,
    packSize = null,
    createdAt = null,
  )

  @Test
  fun `no notification when nothing is soon`() {
    val entries =
      listOf(
        entry("Pa", -2, ExpiryStatus.EXPIRED),
        entry("Arròs", 40, ExpiryStatus.FRESH),
        entry("Peix", 1, ExpiryStatus.FROZEN),
      )
    expectThat(NotificationContentBuilder.build(entries, today)).isNull()
    expectThat(NotificationContentBuilder.build(emptyList(), today)).isNull()
  }

  @Test
  fun `counts only soon units and names the three most urgent products with today and tomorrow`() {
    val entries =
      listOf(
        entry("Iogurt", 1),
        entry("Pa", 3),
        entry("Llet", 0),
        entry("Formatge", 2),
        entry("Arròs", 40, ExpiryStatus.FRESH),
        entry("Vell", -1, ExpiryStatus.EXPIRED),
      )
    val content = NotificationContentBuilder.build(entries, today)
    expectThat(content).isNotNull()
    expectThat(content!!.count).isEqualTo(4)
    expectThat(content.highlights).containsExactly(
      ReminderHighlight("Llet", 0),
      ReminderHighlight("Iogurt", 1),
      ReminderHighlight("Formatge", 2),
    )
  }

  @Test
  fun `units of the same product count but are named once`() {
    val entries = listOf(entry("Llet", 1, id = "a"), entry("llet", 1, id = "b"), entry("Pa", 2))
    val content = NotificationContentBuilder.build(entries, today)!!
    expectThat(content.count).isEqualTo(3)
    expectThat(content.highlights).containsExactly(ReminderHighlight("Llet", 1), ReminderHighlight("Pa", 2))
  }
}
