package com.alorma.caducity.feature.notification

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.model.normalizeForSearch
import com.alorma.caducity.domain.model.sumAmount
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** One product in the reminder: [units] are the ones that expire on [expiration], its nearest date. */
data class ReminderProduct(
  val name: String,
  val categoryName: String?,
  val expiration: LocalDate,
  val daysLeft: Int,
  val units: Int,
  val unitType: UnitType = UnitType.DEFAULT,
  /** Total amount of the batches that expire that day (g or mL); null for units. */
  val amount: Int? = null,
)

/** Products of one notification, nearest first. Only the first [MAX_SHOWN] are named, the rest are [hiddenCount]. */
data class ReminderContent(
  val products: List<ReminderProduct>,
) {
  val shown: List<ReminderProduct> get() = products.take(MAX_SHOWN)
  val hiddenCount: Int get() = products.size - shown.size
  val sameDay: Boolean get() = products.map { it.expiration }.distinct().size == 1

  companion object {
    const val MAX_SHOWN = 5
  }
}

/** [today]: products that expire today, in their own notification. [soon]: the rest of «Aviat». */
data class DailyReminders(
  val today: ReminderContent?,
  val soon: ReminderContent?,
)

/** Pure: decides which daily reminders there are and what they say. Texts are built by the notifier. */
object NotificationContentBuilder {
  /**
   * Counts products (never units) in `SOON`, the same set as the «Aviat» card on Home (frozen and expired
   * are not `SOON`). A product whose nearest unit expires today goes to [DailyReminders.today] instead.
   */
  fun build(
    entries: List<PantryEntry>,
    today: LocalDate,
    categoryNames: Map<String, String> = emptyMap(),
  ): DailyReminders {
    val products =
      entries
        .filter { it.status == ExpiryStatus.SOON }
        .groupBy { it.groupKey }
        .values
        .map { units ->
          val nearest = units.minOf { it.expiration }
          val first = units.first { it.expiration == nearest }
          ReminderProduct(
            name = first.name,
            categoryName = categoryNames[first.categoryId],
            expiration = nearest,
            daysLeft = today.daysUntil(nearest),
            units = units.count { it.expiration == nearest },
            unitType = first.unitType,
            amount = units.filter { it.expiration == nearest }.sumAmount(),
          )
        }.sortedWith(compareBy<ReminderProduct> { it.expiration }.thenBy { it.name.normalizeForSearch() })

    val (expiringToday, soon) = products.partition { it.daysLeft <= 0 }
    return DailyReminders(
      today = expiringToday.takeIf { it.isNotEmpty() }?.let(::ReminderContent),
      soon = soon.takeIf { it.isNotEmpty() }?.let(::ReminderContent),
    )
  }
}
