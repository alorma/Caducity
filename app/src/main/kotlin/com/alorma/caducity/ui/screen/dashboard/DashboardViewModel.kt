package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.ObtainDashboardUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToCreateCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToFilteredItemsAction
import com.alorma.caducity.feature.tracking.NavigateToSettingsAction
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

sealed interface DashboardNavigationSideEffect {
  data object NavigateToCreateCategory : DashboardNavigationSideEffect
  data class NavigateToCategory(val categoryId: String) : DashboardNavigationSideEffect
  data class NavigateToFilteredItems(val status: ItemStatus) : DashboardNavigationSideEffect
  data object NavigateToSettings : DashboardNavigationSideEffect
}

class DashboardViewModel(
  calendarPreferences: CalendarPreferences,
  private val obtainDashboardUseCase: ObtainDashboardUseCase,
  private val dashboardMapper: DashboardMapper,
  private val eventTracker: EventTracker,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val state: StateFlow<DashboardState> = calendarPreferences.state
    .flatMapLatest { calendarConfig ->
      obtainPerCategoryDashboard(calendarConfig.firstDayOfWeek)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  private val navigationSideEffectChannel = Channel<DashboardNavigationSideEffect>()
  val navigationSideEffects = navigationSideEffectChannel.receiveAsFlow()

  private fun obtainPerCategoryDashboard(firstDayOfWeek: kotlinx.datetime.DayOfWeek): Flow<DashboardState.Success> {
    return obtainDashboardUseCase
      .obtain()
      .map { dashboardData ->
        dashboardMapper.mapToPerCategoryState(dashboardData = dashboardData, firstDayOfWeek = firstDayOfWeek)
      }
  }

  fun onNavigateToCreateCategory() {
    eventTracker.trackAction(NavigateToCreateCategoryAction())
    emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToCreateCategory)
  }

  fun onNavigateToCategory(categoryId: String, source: String) {
    eventTracker.trackAction(NavigateToCategoryAction(source))
    emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToCategory(categoryId))
  }

  fun onNavigateToFilteredItems(status: ItemStatus) {
    val statusParam = when (status) {
      ItemStatus.Expired -> "expired"
      ItemStatus.ExpiringSoon -> "expiring_soon"
      ItemStatus.Fresh -> "fresh"
      ItemStatus.Frozen -> "frozen"
      ItemStatus.Consumed -> "consumed"
    }
    eventTracker.trackAction(NavigateToFilteredItemsAction(statusParam))
    emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToFilteredItems(status))
  }

  fun onNavigateToSettings() {
    eventTracker.trackAction(NavigateToSettingsAction())
    emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToSettings)
  }

  private fun emitNavigationSideEffect(effect: DashboardNavigationSideEffect) {
    viewModelScope.launch {
      navigationSideEffectChannel.send(effect)
    }
  }
}
