package com.alorma.caducity.di

import com.alorma.caducity.base.ui.theme.di.themeModule
import com.alorma.caducity.data.dataModule
import com.alorma.caducity.domain.domainModule
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.time.clock.KotlinAppClock
import com.alorma.caducity.ui.screen.dashboard.DashboardMapper
import com.alorma.caducity.ui.screen.dashboard.DashboardUiConfiguration
import com.alorma.caducity.ui.screen.dashboard.DashboardUiConfigurationImpl
import com.alorma.caducity.ui.screen.dashboard.DashboardViewModel
import com.alorma.caducity.ui.screen.product.create.CreateProductViewModel
import com.alorma.caducity.ui.screen.productdetail.ProductDetailMapper
import com.alorma.caducity.ui.screen.productdetail.ProductDetailViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
  includes(platformModule)
  includes(themeModule)
  includes(dataModule)
  includes(domainModule)

  single { Settings() }

  singleOf(::DashboardUiConfigurationImpl) bind DashboardUiConfiguration::class

  single<AppClock> { KotlinAppClock() }
  singleOf(::ObtainDashboardProductsUseCase)
  singleOf(::DashboardMapper)
  viewModelOf(::DashboardViewModel)

  // Product detail
  singleOf(::ObtainProductDetailUseCase)
  singleOf(::ProductDetailMapper)
  viewModelOf(::ProductDetailViewModel)

  // Create product
  singleOf(::CreateProductUseCase)
  viewModelOf(::CreateProductViewModel)
}
