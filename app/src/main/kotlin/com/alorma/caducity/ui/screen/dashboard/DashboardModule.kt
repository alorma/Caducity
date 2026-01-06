package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
  singleOf(::ObtainDashboardProductsUseCase)
  singleOf(::DashboardMapper)
  viewModelOf(::DashboardViewModel)

}