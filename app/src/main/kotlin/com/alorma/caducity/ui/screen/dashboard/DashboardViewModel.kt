package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.usecase.ObtainDashboardUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToCreateCategoryAction
import com.alorma.caducity.feature.tracking.NavigateToFilteredItemsAction
import com.alorma.caducity.feature.tracking.NavigateToSettingsAction
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
  calendarPreferences: CalendarPreferences,
  private val obtainDashboardUseCase: ObtainDashboardUseCase,
  private val dashboardMapper: DashboardMapper,
  private val eventTracker: EventTracker,
) : BaseViewModel<DashboardNavigation, DashboardNavigationSideEffect, Unit>() {

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

  private fun obtainPerCategoryDashboard(firstDayOfWeek: kotlinx.datetime.DayOfWeek): Flow<DashboardState.Success> {
    return obtainDashboardUseCase
      .obtain()
      .map { dashboardData ->
        dashboardMapper.mapToPerCategoryState(dashboardData = dashboardData, firstDayOfWeek = firstDayOfWeek)
      }
  }

  override fun navigate(navigation: DashboardNavigation) {
    when (navigation) {
      DashboardNavigation.CreateCategory -> {
        eventTracker.trackAction(NavigateToCreateCategoryAction())
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToCreateCategory)
      }

      is DashboardNavigation.Category -> {
        eventTracker.trackAction(NavigateToCategoryAction(navigation.source))
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToCategory(navigation.categoryId))
      }

      is DashboardNavigation.FilteredItems -> {
        val statusParam = when (navigation.status) {
          ItemStatus.Expired -> "expired"
          ItemStatus.ExpiringSoon -> "expiring_soon"
          ItemStatus.Fresh -> "fresh"
          ItemStatus.Frozen -> "frozen"
          ItemStatus.Consumed -> "consumed"
        }
        eventTracker.trackAction(NavigateToFilteredItemsAction(statusParam))
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToFilteredItems(navigation.status))
      }

      DashboardNavigation.Settings -> {
        eventTracker.trackAction(NavigateToSettingsAction())
        emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToSettings)
      }
    }
  }
}
