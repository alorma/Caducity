package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper,
) : ViewModel() {

  val state: StateFlow<DashboardState> = obtainDashboardProductsUseCase
    .obtainProducts()
    .map { instances: ImmutableList<ProductInstance> ->
      dashboardMapper.mapToDashboardState(
        instances = instances,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

}
