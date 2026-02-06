package com.alorma.caducity.di

import com.alorma.caducity.barcode.base.BarcodeHandler
import com.alorma.caducity.barcode.base.BarcodeHandlerNoOp
import com.alorma.caducity.config.ConfigQualifier
import com.alorma.caducity.config.configModule
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.data.dataModule
import com.alorma.caducity.data.datasource.RoomBackupDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.domainModule
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.GetExpiringCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObtainCategoryDetailUseCase
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase
import com.alorma.caducity.domain.usecase.backup.ImportBackupUseCase
import com.alorma.caducity.feature.backup.AndroidBackupFileHandler
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.ui.screen.dashboard.dashboardModule
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingViewModel
import com.alorma.caducity.ui.screen.category.create.CreateCategoryViewModel
import com.alorma.caducity.ui.screen.category.create.FutureDateSelectableDates
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailAddItemViewModel
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailMapper
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailViewModel
import com.alorma.caducity.ui.screen.category.detail.product.ProductPageViewModel
import com.alorma.caducity.ui.screen.settings.backup.BackupViewModel
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsViewModel
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
  singleOf(::GetExpiringCategoriesUseCase)

  factory {
    RelativeTimeFormatter(
      context = androidContext(),
      appClock = get(),
      dateFormat = get(qualifier = ConfigQualifier.DateFormat.HumanReadable),
    )
  }

  // Category detail
  singleOf(::ObtainCategoryDetailUseCase)
  singleOf(::AddItemToCategoryUseCase)
  singleOf(::DeleteItemUseCase)
  singleOf(::DeleteCategoryUseCase)
  singleOf(::ConsumeItemUseCase)
  singleOf(::FreezeItemUseCase)
  singleOf(::CategoryDetailMapper)
  viewModelOf(::CategoryDetailViewModel)
  viewModelOf(::CategoryDetailAddItemViewModel)
  viewModelOf(::ProductPageViewModel)

  // Create category
  singleOf(::CreateCategoryUseCase)
  viewModelOf(::CreateCategoryViewModel)

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

  // Debug Settings
  viewModelOf(::DebugSettingsViewModel)
}
