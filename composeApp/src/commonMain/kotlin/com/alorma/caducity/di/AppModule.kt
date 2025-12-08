package com.alorma.caducity.di

import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.screen.dashboard.DashboardMapper
import com.alorma.caducity.ui.screen.dashboard.DashboardViewModel
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.time.clock.KotlinAppClock
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
  single { ThemePreferences() }
  single<AppClock> { KotlinAppClock() }
  singleOf(::ObtainDashboardProductsUseCase)
  singleOf(::DashboardMapper)
  viewModelOf(::DashboardViewModel)
}
