package com.alorma.caducity.di

import com.alorma.caducity.barcode.base.BarcodeHandler
import com.alorma.caducity.barcode.base.BarcodeHandlerNoOp
import com.alorma.caducity.config.ConfigQualifier
import com.alorma.caducity.config.configModule
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.data.dataModule
import com.alorma.caducity.data.datasource.RoomBackupDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.domainModule
import com.alorma.caducity.domain.usecase.AddInstanceToProductUseCase
import com.alorma.caducity.domain.usecase.ConsumeInstanceUseCase
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DeleteInstanceUseCase
import com.alorma.caducity.domain.usecase.FreezeInstanceUseCase
import com.alorma.caducity.domain.usecase.GetExpiringProductsUseCase
import com.alorma.caducity.domain.usecase.ObtainProductDetailUseCase
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase
import com.alorma.caducity.domain.usecase.backup.ImportBackupUseCase
import com.alorma.caducity.feature.backup.AndroidBackupFileHandler
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.ui.screen.dashboard.dashboardModule
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingViewModel
import com.alorma.caducity.ui.screen.product.create.CreateProductViewModel
import com.alorma.caducity.ui.screen.product.create.FutureDateSelectableDates
import com.alorma.caducity.ui.screen.product.detail.ProductDetailAddInstanceViewModel
import com.alorma.caducity.ui.screen.product.detail.ProductDetailMapper
import com.alorma.caducity.ui.screen.product.detail.ProductDetailViewModel
import com.alorma.caducity.ui.screen.settings.backup.BackupViewModel
import com.alorma.caducity.ui.theme.di.themeModule
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
  includes(configModule)
  includes(platformModule)
  includes(themeModule)
  includes(dataModule)
  includes(domainModule)
  includes(fireAndForgetModule)

  includes(dashboardModule)

  factoryOf(::BarcodeHandlerNoOp) {
    bind<BarcodeHandler>()
  }

  single { Settings() }

  // Onboarding
  singleOf(::OnboardingFlag)
  viewModelOf(::OnboardingViewModel)
  singleOf(::GetExpiringProductsUseCase)

  factory {
    RelativeTimeFormatter(
      context = androidContext(),
      appClock = get(),
      dateFormat = get(qualifier = ConfigQualifier.DateFormat.HumanReadable),
    )
  }

  // Product detail
  singleOf(::ObtainProductDetailUseCase)
  singleOf(::AddInstanceToProductUseCase)
  singleOf(::DeleteInstanceUseCase)
  singleOf(::ConsumeInstanceUseCase)
  singleOf(::FreezeInstanceUseCase)
  singleOf(::ProductDetailMapper)
  viewModelOf(::ProductDetailViewModel)
  viewModelOf(::ProductDetailAddInstanceViewModel)

  // Create product
  singleOf(::CreateProductUseCase)
  viewModelOf(::CreateProductViewModel)

  singleOf(::FutureDateSelectableDates)

  // Backup & Restore
  singleOf(::RoomBackupDataSource) bind BackupDataSource::class
  single {
    AndroidBackupFileHandler(
      context = get(),
      appClock = get(),
      dateFilenameFormat = get(qualifier = ConfigQualifier.DateFormat.BackupName),
    )
  } bind BackupFileHandler::class
  singleOf(::ExportBackupUseCase)
  singleOf(::ImportBackupUseCase)
  viewModelOf(::BackupViewModel)
}
