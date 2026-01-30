package com.alorma.caducity.ui.screen.category.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
class FutureDateSelectableDates(
  private val appClock: AppClock,
) : SelectableDates {

  private val todayMillis: Long by lazy {
    val now = appClock.now()
    val today = now.date()
    val todayStart = today.atStartOfDayIn(TimeZone.currentSystemDefault())
    todayStart.toEpochMilliseconds()
  }

  override fun isSelectableDate(utcTimeMillis: Long): Boolean {
    return utcTimeMillis >= todayMillis
  }

  override fun isSelectableYear(year: Int): Boolean {
    val currentYear = appClock.nowDate().year
    return year >= currentYear
  }
}
