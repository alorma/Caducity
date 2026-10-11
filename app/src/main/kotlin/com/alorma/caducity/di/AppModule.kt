package com.alorma.caducity.di

import com.alorma.caducity.config.ConfigQualifier
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
import com.alorma.caducity.config.configModule
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.data.dataModule
import com.alorma.caducity.data.datasource.RoomBackupDataSource
import com.alorma.caducity.domain.backup.BackupDataSource
import com.alorma.caducity.domain.domainModule
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.SplitAndConsumeItemUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase
import com.alorma.caducity.domain.usecase.backup.ImportBackupUseCase
import com.alorma.caducity.feature.backup.AndroidBackupFileHandler
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.feature.consent.consentModule
import com.alorma.caducity.feature.review.reviewModule
import com.alorma.caducity.feature.tracking.trackingModule
import com.alorma.caducity.ui.components.actions.ItemActionsSheetViewModel
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import com.alorma.caducity.ui.screen.add.AddViewModel
import com.alorma.caducity.ui.screen.calendar.CalendarViewModel
import com.alorma.caducity.ui.screen.category.CategoryViewModel
import com.alorma.caducity.ui.screen.category.editor.CategoryEditorViewModel
import com.alorma.caducity.ui.screen.category.quick.CategoryCreatedEvents
import com.alorma.caducity.ui.screen.category.quick.QuickCategoryViewModel
import com.alorma.caducity.ui.screen.home.HomeViewModel
import com.alorma.caducity.ui.screen.home.WidgetTipFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingFlag
import com.alorma.caducity.ui.screen.onboarding.OnboardingViewModel
import com.alorma.caducity.ui.screen.pantry.PantrySearchRequests
import com.alorma.caducity.ui.screen.pantry.PantryViewModel
import com.alorma.caducity.ui.screen.settings.SettingsViewModel
import com.alorma.caducity.ui.screen.settings.backup.BackupViewModel
import com.alorma.caducity.ui.screen.settings.debug.DebugRemoteConfigsViewModel
import com.alorma.caducity.ui.screen.settings.debug.DebugSettingsViewModel
import com.alorma.caducity.ui.screen.settings.privacy.PrivacySettingsViewModel
import com.alorma.caducity.ui.screen.status.StatusListViewModel
import com.alorma.caducity.ui.theme.di.themeModule
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
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

    singleOf(::FirstDayOfWeekPreferences)

    // Calendar
    viewModel { params ->
      CalendarViewModel(
        args = params.get(),
        observeCalendar = get(),
        observeItems = get(),
        observeCategories = get(),
        firstDayOfWeekPreferences = get(),
        todayProvider = get(),
        eventTracker = get(),
      )
    }

    // Home
    singleOf(::WidgetTipFlag)
    viewModelOf(::HomeViewModel)

    // Pantry
    singleOf(::PantrySearchRequests)
    viewModelOf(::PantryViewModel)

    single { Settings() }

    // Onboarding
    singleOf(::OnboardingFlag)
    viewModelOf(::OnboardingViewModel)

    factory {
      RelativeTimeFormatter(
        context = androidContext(),
        appClock = get(),
        dateFormat = get(qualifier = ConfigQualifier.DateFormat.HumanReadable),
      )
    }

    // Status lists (Caducats, Aviat, Frescos, Congelats)
    viewModel { params ->
      StatusListViewModel(
        status = params.get(),
        observeByStatus = get(),
        thresholds = get(),
        consumeItem = get(),
        freezeItem = get(),
        unfreezeItem = get(),
        rescheduleItem = get(),
        discardItems = get(),
        restoreItems = get(),
        snackbars = get(),
        eventTracker = get(),
      )
    }

    // Category
    viewModel { params ->
      CategoryViewModel(
        args = params.get(),
        observeCategory = get(),
        observeWeekStrip = get(),
        deleteCategory = get(),
        eventTracker = get(),
      )
    }
    singleOf(::DeleteItemUseCase)
    singleOf(::DeleteCategoryUseCase)
    singleOf(::ConsumeItemUseCase)
    singleOf(::FreezeItemUseCase)
    singleOf(::RescheduleItemUseCase)
    singleOf(::UnfreezeItemUseCase)
    singleOf(::SplitAndConsumeItemUseCase)

    // Quick category sheet
    singleOf(::CategoryCreatedEvents)
    viewModelOf(::QuickCategoryViewModel)

    // Add screen
    viewModel { params ->
      AddViewModel(
        args = params.get(),
        todayProvider = get(),
        addItemBatch = get(),
        undoAddItems = get(),
        getNameSuggestions = get(),
        getActiveItem = get(),
        updateItem = get(),
        removeBatchEntry = get(),
        restoreBatch = get(),
        observeCategories = get(),
        categoryCreated = get(),
        eventTracker = get(),
      )
    }

    // Product page

    // Category editor (new and edit)
    singleOf(::CreateCategoryUseCase)
    viewModel { params ->
      CategoryEditorViewModel(
        categoryId = params.getOrNull(),
        pantryDataSource = get(),
        createCategory = get(),
        updateCategory = get(),
        deleteCategory = get(),
        eventTracker = get(),
      )
    }

    // Settings
    viewModelOf(::SettingsViewModel)

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

    singleOf(::GlobalSnackbarEvents)
    viewModel { params ->
      ItemActionsSheetViewModel(
        itemId = params.get(),
        observeItems = get(),
        observeCategories = get(),
        consumeItem = get(),
        splitAndConsume = get(),
        freezeItem = get(),
        unfreezeItem = get(),
        rescheduleItem = get(),
        deleteItem = get(),
        moveItem = get(),
        restoreItems = get(),
        appClock = get(),
        snackbars = get(),
        eventTracker = get(),
      )
    }
  }
