package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.base.main.InstanceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

interface DashboardUiConfiguration {
  val config: MutableStateFlow<DashboardUI>
  fun updateSearchQuery(query: String)
  fun updateStatusFilters(filters: Set<InstanceStatus>)
}

class DashboardUiConfigurationImpl : DashboardUiConfiguration {
  override val config = MutableStateFlow(
    DashboardUI(),
  )

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
  val searchQuery: String = "",
  val statusFilters: Set<InstanceStatus> = emptySet(),
)