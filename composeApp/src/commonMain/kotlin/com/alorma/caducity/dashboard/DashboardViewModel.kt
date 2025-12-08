package com.alorma.caducity.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.time.clock.AppClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
  productDataSource: ProductDataSource,
  private val appClock: AppClock
) : ViewModel() {

  val state: StateFlow<DashboardState> = productDataSource
    .getAllProductInstances()
    .map { instances ->
      val sections = instances.toDashboardSections(now = appClock.now())
      DashboardState.Success(sections = sections)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = DashboardState.Loading
    )
}
