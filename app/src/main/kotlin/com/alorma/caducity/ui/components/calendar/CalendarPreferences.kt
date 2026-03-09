package com.alorma.caducity.ui.components.calendar

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.datetime.DayOfWeek

data class CalendarConfigState(
  val firstDayOfWeek: DayOfWeek,
)

class CalendarPreferences(
  private val settings: Settings,
) {
  val state: MutableStateFlow<CalendarConfigState> =
    MutableStateFlow(
      obtainCurrent(),
    )

  private fun obtainCurrent(): CalendarConfigState {
    val savedValue = settings.getStringOrNull(KEY_FIRST_DAY_OF_WEEK)
    val firstDayOfWeek =
      savedValue?.let {
        try {
          DayOfWeek.valueOf(it)
        } catch (_: IllegalArgumentException) {
          DayOfWeek.MONDAY
        }
      } ?: DayOfWeek.MONDAY

    return CalendarConfigState(
      firstDayOfWeek = firstDayOfWeek,
    )
  }

  fun setFirstDayOfWeek(dayOfWeek: DayOfWeek) {
    settings[KEY_FIRST_DAY_OF_WEEK] = dayOfWeek.name

    state.getAndUpdate { current ->
      current.copy(firstDayOfWeek = dayOfWeek)
    }
  }

  companion object {
    private const val KEY_FIRST_DAY_OF_WEEK = "calendar_first_day_of_week"
  }
}

object CalendarPreferencesNoOp {
  val state: StateFlow<CalendarConfigState> =
    MutableStateFlow(
      CalendarConfigState(firstDayOfWeek = DayOfWeek.MONDAY),
    )

  fun setFirstDayOfWeek(dayOfWeek: DayOfWeek) {
    // No-op
  }
}
