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
    soonDays: Int = 3,
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
    soonDays = soonDays,
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
  fun `counts only soon units that enter soon today or expire today or tomorrow, naming the most urgent`() {
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
    expectThat(content!!.count).isEqualTo(3)
    expectThat(content.highlights).containsExactly(
      ReminderHighlight("Llet", 0),
      ReminderHighlight("Iogurt", 1),
      ReminderHighlight("Pa", 3),
    )
  }

  @Test
  fun `units of the same product count but are named once`() {
    val entries = listOf(entry("Llet", 1, id = "a"), entry("llet", 1, id = "b"), entry("Pa", 3))
    val content = NotificationContentBuilder.build(entries, today)!!
    expectThat(content.count).isEqualTo(3)
    expectThat(content.highlights).containsExactly(ReminderHighlight("Llet", 1), ReminderHighlight("Pa", 3))
  }

  @Test
  fun `a unit with a long margin is only reported the day it becomes soon`() {
    val pasta = entry("Pasta", days = 30, soonDays = 30)
    val inside = entry("Cigrons", days = 20, soonDays = 30)
    expectThat(NotificationContentBuilder.build(listOf(inside), today)).isNull()
    expectThat(NotificationContentBuilder.build(listOf(pasta), today)).isNotNull().get { count }.isEqualTo(1)
  }

  @Test
  fun `units that expire today or tomorrow are always reported`() {
    val entries =
      listOf(
        entry("Arròs", days = 0, soonDays = 30),
        entry("Llet", days = 1, soonDays = 14),
        entry("Pasta", days = 2, soonDays = 30),
      )
    val content = NotificationContentBuilder.build(entries, today)
    expectThat(content).isNotNull().get { count }.isEqualTo(2)
    expectThat(content).isNotNull().get { highlights.map { it.name } }.containsExactly("Arròs", "Llet")
  }

  @Test
  fun `the default margin still reports on the first day inside it`() {
    val entries = listOf(entry("Iogurt", days = 3), entry("Formatge", days = 2))
    expectThat(NotificationContentBuilder.build(entries, today)).isNotNull().get { count }.isEqualTo(1)
  }
}
