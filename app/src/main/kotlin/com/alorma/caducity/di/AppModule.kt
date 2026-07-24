package com.alorma.caducity.di

import com.alorma.caducity.config.ConfigQualifier
import com.alorma.caducity.config.configModule
import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.data.dataModule
import com.alorma.caducity.data.datasource.RoomBackupDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.domainModule
import com.alorma.caducity.domain.usecase.AddItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.ClearProductItemsUseCase
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.DeleteProductUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.domain.usecase.GetExpiringCategoriesUseCase
import com.alorma.caducity.domain.usecase.GetItemsByStatusUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.domain.usecase.ObtainCategoryDetailUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndConsumeItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndDeleteItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndFreezeItemUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase
import com.alorma.caducity.domain.usecase.backup.ImportBackupUseCase
import com.alorma.caducity.feature.ai.AiFeatureConfig
import com.alorma.caducity.feature.ai.AiGroceryParser
import com.alorma.caducity.feature.ai.AiJaroWinklerMatcher
import com.alorma.caducity.feature.ai.AiModelPreferences
import com.alorma.caducity.feature.ai.AiProductMatcher
import com.alorma.caducity.feature.ai.LlamatikGroceryParser
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.feature.ai.WorkManagerModelManager
import com.alorma.caducity.feature.backup.AndroidBackupFileHandler
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.feature.consent.consentModule
import com.alorma.caducity.feature.review.reviewModule
import com.alorma.caducity.feature.tracking.trackingModule
import com.alorma.caducity.ui.components.bottomsheet.ItemActionsViewModel
import com.alorma.caducity.ui.screen.ai.AiAssistantViewModel
import com.alorma.caducity.ui.screen.category.create.CreateCategoryViewModel
import com.alorma.caducity.ui.screen.category.create.FutureDateSelectableDates
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailAddItemViewModel
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailMapper
import com.alorma.caducity.ui.screen.category.detail.CategoryDetailViewModel
import com.alorma.caducity.ui.screen.category.detail.product.ProductPageMapper
import com.alorma.caducity.ui.screen.category.detail.product.ProductPageViewModel
import com.alorma.caducity.ui.screen.dashboard.dashboardModule
import com.alorma.caducity.ui.screen.filtered.FilteredItemsByStatusViewModel
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingViewModel
import com.alorma.caducity.ui.screen.settings.backup.BackupViewModel
import com.alorma.caducity.ui.screen.settings.debug.DebugRemoteConfigsViewModel
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsViewModel
import com.alorma.caducity.ui.screen.settings.privacy.PrivacySettingsViewModel
import com.alorma.caducity.ui.theme.di.themeModule
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule =
  module {
    includes(configModule)
    includes(platformModule)
    includes(themeModule)
    includes(dataModule)
    includes(domainModule)
    includes(fireAndForgetModule)
    includes(consentModule)
    includes(reviewModule)
    includes(trackingModule)

    includes(dashboardModule)

    single { Settings() }

    // AI Assistant
    singleOf(::AiFeatureConfig) bind RemoteConfig::class
    singleOf(::AiModelPreferences)
    single<ModelManager> { WorkManagerModelManager(androidContext(), get()) }
    single<AiGroceryParser> { LlamatikGroceryParser(get()) }
    singleOf(::AiJaroWinklerMatcher) bind AiProductMatcher::class
    viewModelOf(::AiAssistantViewModel)

    // Onboarding
    singleOf(::OnboardingFlag)
    viewModelOf(::OnboardingViewModel)
    singleOf(::GetExpiringCategoriesUseCase)
    singleOf(::GetItemsByStatusUseCase)
    viewModelOf(::FilteredItemsByStatusViewModel)

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
    singleOf(::RescheduleItemUseCase)
    singleOf(::UnfreezeItemUseCase)
    singleOf(::SplitAndConsumeItemUseCase)
    singleOf(::SplitAndFreezeItemUseCase)
    singleOf(::SplitAndDeleteItemUseCase)
    singleOf(::CategoryDetailMapper)
    viewModelOf(::CategoryDetailViewModel)
    viewModelOf(::CategoryDetailAddItemViewModel)

    // Product page
    factoryOf(::GetProductItemsUseCase)
    factoryOf(::GetCategoryProductsUseCase)
    factoryOf(::ProductPageMapper)
    factoryOf(::DeleteProductUseCase)
    factoryOf(::ClearProductItemsUseCase)
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

    // Privacy Settings
    viewModelOf(::PrivacySettingsViewModel)

    // Debug Settings
    factory {
      DebugSettingsViewModel(
        notificationDebugHelper = get(),
        populateFakeDataUseCase = get(),
        fakeTestDataStrategy = get(),
        fakePlayStoreDataStrategy = get(),
        consentManager = get(),
      )
    }

    // Debug Remote Configs
    factory {
      DebugRemoteConfigsViewModel(
        remoteConfigRunner = get(),
        remoteConfigs = getAll(),
      )
    }

    // Item Actions Bottom Sheet
    viewModelOf(::ItemActionsViewModel)
  }
