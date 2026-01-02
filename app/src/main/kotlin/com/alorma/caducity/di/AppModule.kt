package com.alorma.caducity.di

import com.alorma.caducity.base.main.clock.clockModule
import com.alorma.caducity.base.ui.theme.di.themeModule
import com.alorma.caducity.data.dataModule
import com.alorma.caducity.domain.domainModule
import com.alorma.caducity.domain.usecase.AddInstanceToProductUseCase
import com.alorma.caducity.domain.usecase.ConsumeInstanceUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DeleteInstanceUseCase
import com.alorma.caducity.domain.usecase.FreezeInstanceUseCase
import com.alorma.caducity.domain.usecase.GetExpiringProductsUseCase
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import com.alorma.caducity.ui.screen.dashboard.DashboardMapper
import com.alorma.caducity.ui.screen.dashboard.DashboardUiConfiguration
import com.alorma.caducity.ui.screen.dashboard.DashboardUiConfigurationImpl
import com.alorma.caducity.ui.screen.dashboard.DashboardViewModel
import com.alorma.caducity.ui.screen.dashboard.components.LocalizedDateFormatter
import com.alorma.caducity.ui.screen.product.create.CreateProductViewModel
import com.alorma.caducity.ui.screen.product.create.FutureDateSelectableDates
import com.alorma.caducity.ui.screen.product.detail.ProductDetailMapper
import com.alorma.caducity.ui.screen.product.detail.ProductDetailViewModel
import com.alorma.caducity.ui.screen.products.ProductsListMapper
import com.alorma.caducity.ui.screen.products.ProductsListViewModel
import com.alorma.caducity.ui.screen.products.RelativeTimeFormatter
import com.russhwolf.settings.Settings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
  includes(clockModule)
  includes(platformModule)
  includes(themeModule)
  includes(dataModule)
  includes(domainModule)

  single { Settings() }

  singleOf(::DashboardUiConfigurationImpl) bind DashboardUiConfiguration::class

  factoryOf(::RelativeTimeFormatter)

  singleOf(::ObtainDashboardProductsUseCase)
  singleOf(::GetExpiringProductsUseCase)
  singleOf(::DashboardMapper)
  viewModelOf(::DashboardViewModel)

  // Products list
  singleOf(::ProductsListMapper)
  viewModelOf(::ProductsListViewModel)

  // Product detail
  singleOf(::ObtainProductDetailUseCase)
  singleOf(::AddInstanceToProductUseCase)
  singleOf(::DeleteInstanceUseCase)
  singleOf(::ConsumeInstanceUseCase)
  singleOf(::FreezeInstanceUseCase)
  singleOf(::ProductDetailMapper)
  viewModelOf(::ProductDetailViewModel)

  // Create product
  singleOf(::CreateProductUseCase)
  viewModelOf(::CreateProductViewModel)

  single<DateTimeFormat<LocalDate>> {
    LocalDate.Format {
      day()
      chars("/")
      monthNumber()
      chars("/")
      year()
    }
  }
  singleOf(::FutureDateSelectableDates)
  singleOf(::LocalizedDateFormatter)
}
