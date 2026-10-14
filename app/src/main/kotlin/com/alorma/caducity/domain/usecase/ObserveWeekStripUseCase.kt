package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.ExpiryStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** What the week strip draws: [today] and, for each date, the status of every unit that expires that day. */
data class WeekStripData(
  val today: LocalDate,
  val statusesByDate: Map<LocalDate, List<ExpiryStatus>>,
) {
  companion object {
    val Empty = WeekStripData(LocalDate(1970, 1, 1), emptyMap())
  }
}

/**
 * Data for the strip of 7-day windows that starts today (it is a moving window, not a calendar week).
 * It covers [WINDOWS] windows so it can be paged, and restarts when the day changes. Units that are
 * already expired are left out: the strip begins today.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveWeekStripUseCase(
  private val observeCalendar: ObserveCalendarUseCase,
  private val todayProvider: TodayProvider,
) {
  fun observe(categoryId: String? = null): Flow<WeekStripData> =
    todayProvider
      .observe()
      .flatMapLatest { today ->
        val range = today..today.plus(WINDOWS * DAYS_PER_WINDOW - 1, DateTimeUnit.DAY)
        observeCalendar.observe(range, categoryId).map { byDate ->
          WeekStripData(
            today = today,
            statusesByDate =
              byDate.mapValues { (_, units) ->
                units.map { it.status }.filter { it != ExpiryStatus.EXPIRED }.sortedBy { it.ordinal }
              },
          )
        }
      }.flowOn(Dispatchers.Default)

  companion object {
    const val DAYS_PER_WINDOW = 7
    const val WINDOWS = 8
  }
}
