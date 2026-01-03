package com.alorma.caducity.ui.screen.product.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import com.alorma.caducity.config.clock.AppClock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
class FutureDateSelectableDates(
  private val appClock: AppClock,
) : SelectableDates {

  private val todayMillis: Long by lazy {
    val now = appClock.now()
    val today = now.toLocalDateTime(TimeZone.UTC).date
    val todayStart = today.atStartOfDayIn(TimeZone.UTC)
    todayStart.minus(3.days).toEpochMilliseconds()
  }

  override fun isSelectableDate(utcTimeMillis: Long): Boolean {
    return utcTimeMillis >= todayMillis
  }

  override fun isSelectableYear(year: Int): Boolean {
    val currentYear = appClock.now().toLocalDateTime(TimeZone.UTC).year
    return year >= currentYear
  }
}
