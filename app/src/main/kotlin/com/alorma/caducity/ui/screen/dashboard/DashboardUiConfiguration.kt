package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.InstanceStatus
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

enum class CalendarMode {
  MONTH,
  WEEK
}

interface DashboardUiConfiguration {
  val config: MutableStateFlow<DashboardUI>
  fun updateSearchQuery(query: String)
  fun updateStatusFilters(filters: Set<InstanceStatus>)
  fun toggleCalendarMode()
}

class DashboardUiConfigurationImpl(
  private val settings: Settings
) : DashboardUiConfiguration {
  companion object {
    private const val KEY_CALENDAR_MODE = "dashboard_calendar_mode"
  }

  override val config = MutableStateFlow(
    DashboardUI(
      calendarMode = loadCalendarMode()
    ),
  )

  private fun loadCalendarMode(): CalendarMode {
    val savedValue = settings.getStringOrNull(KEY_CALENDAR_MODE)
    return savedValue?.let {
      try {
        CalendarMode.valueOf(it)
      } catch (_: IllegalArgumentException) {
        CalendarMode.MONTH
      }
    } ?: CalendarMode.MONTH
  }

  private fun saveCalendarMode(mode: CalendarMode) {
    settings.putString(KEY_CALENDAR_MODE, mode.name)
  }

  override fun updateSearchQuery(query: String) {
    config.getAndUpdate { current ->
      current.copy(searchQuery = query)
    }
  }

  override fun updateStatusFilters(filters: Set<InstanceStatus>) {
    config.getAndUpdate { current ->
      current.copy(statusFilters = filters)
    }
  }

  override fun toggleCalendarMode() {
    config.getAndUpdate { current ->
      val newMode = when (current.calendarMode) {
        CalendarMode.MONTH -> CalendarMode.WEEK
        CalendarMode.WEEK -> CalendarMode.MONTH
      }
      saveCalendarMode(newMode)
      current.copy(calendarMode = newMode)
    }
  }
}

data class DashboardUI(
  val searchQuery: String = "",
  val statusFilters: Set<InstanceStatus> = emptySet(),
  val calendarMode: CalendarMode = CalendarMode.MONTH,
)