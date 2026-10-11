package com.alorma.caducity.feature.notification

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.PantryEntry
import com.alorma.caducity.domain.model.normalizeForSearch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** One of the most urgent products named in the body: [daysLeft] is 0 for «avui», 1 for «demà». */
data class ReminderHighlight(
  val name: String,
  val daysLeft: Int,
)

/** What the daily reminder says: [count] units about to expire and the first [highlights], most urgent first. */
data class ReminderContent(
  val count: Int,
  val highlights: List<ReminderHighlight>,
)

/** Pure: decides whether there is a daily reminder to show and what it says. Texts are built by the notifier. */
object NotificationContentBuilder {
  const val MAX_HIGHLIGHTS = 3

  /** `null` when nothing is `SOON` (which includes everything that expires today): no notification. */
  fun build(
    entries: List<PantryEntry>,
    today: LocalDate,
  ): ReminderContent? {
    val soon =
      entries
        .filter { it.status == ExpiryStatus.SOON }
        .sortedWith(compareBy<PantryEntry> { it.expiration }.thenBy { it.name.normalizeForSearch() })
    if (soon.isEmpty()) return null

    val highlights =
      soon
        .distinctBy { it.name.normalizeForSearch() }
        .take(MAX_HIGHLIGHTS)
        .map { ReminderHighlight(name = it.name, daysLeft = today.daysUntil(it.expiration)) }

    return ReminderContent(count = soon.size, highlights = highlights)
  }
}
