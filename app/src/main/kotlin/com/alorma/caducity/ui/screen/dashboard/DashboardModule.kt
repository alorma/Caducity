package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
  singleOf(::CalendarPreferences)
  singleOf(::AppCalendarConfigMapper)
  singleOf(::ObtainDashboardProductsUseCase)
  singleOf(::ObtainDashboardInstancesUseCase)
  singleOf(::DashboardMapper)
  viewModelOf(::DashboardViewModel)

}