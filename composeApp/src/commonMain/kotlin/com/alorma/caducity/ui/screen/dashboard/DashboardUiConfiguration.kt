package com.alorma.caducity.ui.screen.dashboard

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

interface DashboardUiConfiguration {
  val config: MutableStateFlow<DashboardUI>
  fun updateCollapse(collapsed: Boolean)
  fun updateSearchQuery(query: String)
  fun updateStatusFilters(filters: Set<InstanceStatus>)
}

class DashboardUiConfigurationImpl(
  private val settings: Settings
) : DashboardUiConfiguration {
  override val config = MutableStateFlow<DashboardUI>(
    DashboardUI(
      collapsed = settings.getBoolean(CollapseKey, false),
    ),
  )

  override fun updateCollapse(collapsed: Boolean) {
    config.getAndUpdate { current ->
      settings.putBoolean(CollapseKey, collapsed)
      current.copy(collapsed = collapsed)
    }
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

  companion object {
    private const val CollapseKey = "CollapseKey"
  }
}

data class DashboardUI(
  val collapsed: Boolean,
  val searchQuery: String = "",
  val statusFilters: Set<InstanceStatus> = emptySet(),
)