package com.alorma.caducity.ui.screen.products

import android.content.Context
import com.alorma.caducity.R
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import kotlin.math.abs

class RelativeTimeFormatter(
  private val context: Context,
) {
  suspend fun format(today: LocalDate, expirationDate: LocalDate): String {
    val daysDiff = today.until(expirationDate, DateTimeUnit.DAY).toInt()

    return when {
      daysDiff == 0 -> context.getString(R.string.relative_time_today)
      daysDiff == 1 -> context.getString(R.string.relative_time_tomorrow)
      daysDiff == -1 -> context.getString(R.string.relative_time_yesterday)
      daysDiff in 2..7 -> context.getString(R.string.relative_time_in_days, daysDiff)
      daysDiff in 8..14 -> context.getString(R.string.relative_time_in_a_week)
      daysDiff in 15..30 -> context.getString(R.string.relative_time_in_weeks, daysDiff / 7)
      daysDiff in 31..60 -> context.getString(R.string.relative_time_in_a_month)
      daysDiff > 60 -> context.getString(R.string.relative_time_in_months, daysDiff / 30)
      daysDiff in -7..-2 -> context.getString(R.string.relative_time_days_ago, abs(daysDiff))
      daysDiff in -14..-8 -> context.getString(R.string.relative_time_a_week_ago)
      daysDiff in -30..-15 -> context.getString(R.string.relative_time_weeks_ago, abs(daysDiff) / 7)
      daysDiff in -60..-31 -> context.getString(R.string.relative_time_a_month_ago)
      else -> context.getString(R.string.relative_time_months_ago, abs(daysDiff) / 30)
    }
  }
}
