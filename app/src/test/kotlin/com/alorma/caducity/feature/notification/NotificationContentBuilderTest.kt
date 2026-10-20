package com.alorma.caducity.feature.notification

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

class NotificationContentBuilderTest {
  private val today = LocalDate(2026, 10, 11)

  private fun entry(
    name: String,
    days: Int,
    status: ExpiryStatus = ExpiryStatus.SOON,
    id: String = "$name-$days-${System.nanoTime()}",
  ) = PantryEntry(
    itemId = id,
    productId = name,
    name = name,
    categoryId = "c",
    expiration = LocalDate.fromEpochDays(today.toEpochDays() + days),
    status = status,
    frozenSince = null,
    remainingDays = null,
    createdAt = null,
  )

  private fun build(entries: List<PantryEntry>) =
    NotificationContentBuilder.build(entries, today, mapOf("c" to "Embotits"))

  @Test
  fun `no notification when nothing is soon`() {
    val entries =
      listOf(
        entry("Pa", -2, ExpiryStatus.EXPIRED),
        entry("Arròs", 40, ExpiryStatus.FRESH),
        entry("Peix", 1, ExpiryStatus.FROZEN),
      )
    expectThat(build(entries)).isEqualTo(DailyReminders(today = null, soon = null))
    expectThat(build(emptyList())).isEqualTo(DailyReminders(today = null, soon = null))
  }

  @Test
  fun `one product with one unit`() {
    val soon = build(listOf(entry("Xoriço", 1))).soon
    expectThat(soon).isNotNull()
    expectThat(soon!!.products.single()) {
      get { name }.isEqualTo("Xoriço")
      get { categoryName }.isEqualTo("Embotits")
      get { daysLeft }.isEqualTo(1)
      get { units }.isEqualTo(1)
    }
    expectThat(soon.sameDay).isTrue()
  }

  @Test
  fun `several units of a product count as one product`() {
    val entries = (1..11).map { entry("Xoriço", 1) }
    val soon = build(entries).soon!!
    expectThat(soon.products.size).isEqualTo(1)
    expectThat(soon.products[0].units).isEqualTo(11)
  }

  @Test
  fun `units only count on the nearest date`() {
    val soon = build(listOf(entry("Llet", 3), entry("Llet", 2), entry("Llet", 2))).soon!!
    expectThat(soon.products.single().daysLeft).isEqualTo(2)
    expectThat(soon.products.single().units).isEqualTo(2)
  }

  @Test
  fun `several products the same day`() {
    val soon = build(listOf(entry("Llet", 1), entry("Xoriço", 1), entry("Iogurt", 1))).soon!!
    expectThat(soon.products.map { it.name }).containsExactly("Iogurt", "Llet", "Xoriço")
    expectThat(soon.sameDay).isTrue()
  }

  @Test
  fun `several products on different days, nearest first`() {
    val soon = build(listOf(entry("Iogurt", 5), entry("Xoriço", 1), entry("Llet", 3))).soon!!
    expectThat(soon.products.map { it.name }).containsExactly("Xoriço", "Llet", "Iogurt")
    expectThat(soon.sameDay).isFalse()
  }

  @Test
  fun `more than five products show five and the rest as hidden`() {
    val soon = build((1..8).map { entry("P$it", it) }).soon!!
    expectThat(soon.shown.size).isEqualTo(5)
    expectThat(soon.hiddenCount).isEqualTo(3)
    expectThat(soon.shown.map { it.name }).containsExactly("P1", "P2", "P3", "P4", "P5")
  }

  @Test
  fun `products that expire today have their own reminder and are not soon`() {
    val reminders = build(listOf(entry("Xoriço", 0), entry("Llet", 2)))
    expectThat(reminders.today!!.products.map { it.name }).containsExactly("Xoriço")
    expectThat(reminders.soon!!.products.map { it.name }).containsExactly("Llet")
  }

  @Test
  fun `only products that expire today means no soon reminder`() {
    val reminders = build(listOf(entry("Xoriço", 0)))
    expectThat(reminders.soon).isNull()
    expectThat(reminders.today).isNotNull()
  }

  @Test
  fun `a soon product is reported every day, not only when it enters the margin`() {
    expectThat(build(listOf(entry("Cigrons", 20))).soon).isNotNull()
  }
}
