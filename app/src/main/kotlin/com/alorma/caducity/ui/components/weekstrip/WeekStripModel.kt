package com.alorma.caducity.ui.components.weekstrip

import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.domain.usecase.WeekStripData
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** One day of the strip with the statuses of its dots (at most [MAX_DOTS], never `EXPIRED`). */
data class WeekStripDay(
  val date: LocalDate,
  val dots: List<ExpiryStatus>,
  /** Products that expire that day, to describe it for accessibility; dots are capped, this is not. */
  val productCount: Int,
)

const val MAX_DOTS = 3

/** The 7 days of window [page] (0 = the 7 days starting today). */
fun WeekStripData.window(page: Int): List<WeekStripDay> =
  (0 until ObserveWeekStripUseCase.DAYS_PER_WINDOW).map { offset ->
    val date = today.plus(page * ObserveWeekStripUseCase.DAYS_PER_WINDOW + offset, DateTimeUnit.DAY)
    val statuses = statusesByDate[date].orEmpty().filter { it != ExpiryStatus.EXPIRED }
    WeekStripDay(date = date, dots = statuses.take(MAX_DOTS), productCount = statuses.size)
  }
