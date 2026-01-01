package com.alorma.caducity.ui.screen.products

import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.relative_time_a_month_ago
import caducity.composeapp.generated.resources.relative_time_a_week_ago
import caducity.composeapp.generated.resources.relative_time_days_ago
import caducity.composeapp.generated.resources.relative_time_in_a_month
import caducity.composeapp.generated.resources.relative_time_in_a_week
import caducity.composeapp.generated.resources.relative_time_in_days
import caducity.composeapp.generated.resources.relative_time_in_months
import caducity.composeapp.generated.resources.relative_time_in_weeks
import caducity.composeapp.generated.resources.relative_time_months_ago
import caducity.composeapp.generated.resources.relative_time_today
import caducity.composeapp.generated.resources.relative_time_tomorrow
import caducity.composeapp.generated.resources.relative_time_weeks_ago
import caducity.composeapp.generated.resources.relative_time_yesterday
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.getString
import kotlin.math.abs

class RelativeTimeFormatter {
  suspend fun format(today: LocalDate, expirationDate: LocalDate): String {
    val daysDiff = today.until(expirationDate, DateTimeUnit.DAY).toInt()

    return when {
      daysDiff == 0 -> getString(Res.string.relative_time_today)
      daysDiff == 1 -> getString(Res.string.relative_time_tomorrow)
      daysDiff == -1 -> getString(Res.string.relative_time_yesterday)
      daysDiff in 2..7 -> getString(Res.string.relative_time_in_days, daysDiff)
      daysDiff in 8..14 -> getString(Res.string.relative_time_in_a_week)
      daysDiff in 15..30 -> getString(Res.string.relative_time_in_weeks, daysDiff / 7)
      daysDiff in 31..60 -> getString(Res.string.relative_time_in_a_month)
      daysDiff > 60 -> getString(Res.string.relative_time_in_months, daysDiff / 30)
      daysDiff in -7..-2 -> getString(Res.string.relative_time_days_ago, abs(daysDiff))
      daysDiff in -14..-8 -> getString(Res.string.relative_time_a_week_ago)
      daysDiff in -30..-15 -> getString(Res.string.relative_time_weeks_ago, abs(daysDiff) / 7)
      daysDiff in -60..-31 -> getString(Res.string.relative_time_a_month_ago)
      else -> getString(Res.string.relative_time_months_ago, abs(daysDiff) / 30)
    }
  }
}
