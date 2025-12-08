package com.alorma.caducity.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.days

class DashboardViewModel(
  productDataSource: ProductDataSource,
  private val appClock: AppClock
) : ViewModel() {

  val state: StateFlow<DashboardState> = productDataSource
    .getAllProductInstances()
    .map { instances ->
      val now = appClock.now()
      val soonThreshold = now + 7.days

      val expired = instances.count { it.expirationDate <= now }
      val soonExpiring = instances.count { it.expirationDate > now && it.expirationDate <= soonThreshold }
      val stillGood = instances.count { it.expirationDate > soonThreshold }

      val sections = listOf(
        DashboardSection(
          title = "Expired Items",
          itemCount = expired
        ),
        DashboardSection(
          title = "Soon Expiring Items",
          itemCount = soonExpiring
        ),
        DashboardSection(
          title = "Still Good Items",
          itemCount = stillGood
        )
      )

      DashboardState.Success(sections = sections)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = DashboardState.Loading
    )
}
