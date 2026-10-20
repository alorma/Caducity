package com.alorma.caducity.feature.notification

import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/**
 * Day of an expiration beyond tomorrow: the weekday name inside the next 6 days («dijous»), otherwise the
 * short weekday and the day of month («dl 26»). «avui» and «demà» are plain resources, not built here.
 */
object ReminderDayLabel {
  const val MAX_WEEKDAY_DAYS = 6

  fun format(
    expiration: LocalDate,
    daysLeft: Int,
    locale: Locale,
  ): String {
    val weekday = expiration.toJavaLocalDate().dayOfWeek
    return if (daysLeft <= MAX_WEEKDAY_DAYS) {
      weekday.getDisplayName(TextStyle.FULL, locale).lowercase(locale)
    } else {
      val short = weekday.getDisplayName(TextStyle.SHORT, locale).trimEnd('.').lowercase(locale)
      "$short ${expiration.day}"
    }
  }
}
