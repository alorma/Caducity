package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper
) : ViewModel() {

  val state: StateFlow<DashboardState> = obtainDashboardProductsUseCase
    .obtainProducts()
    .onEach { delay(5.milliseconds) }
    .map { dashboardProducts ->
      val sections = dashboardMapper.mapToDashboardSections(dashboardProducts)
      DashboardState.Success(items = sections)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  private val _isExpanded = MutableStateFlow(true)
  val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

  fun toggleExpanded() {
    _isExpanded.value = !_isExpanded.value
  }
}
