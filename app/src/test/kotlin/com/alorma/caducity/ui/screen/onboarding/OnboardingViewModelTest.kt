package com.alorma.caducity.ui.screen.onboarding

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.DisclaimerPreferences
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner
import com.alorma.caducity.config.resources.StringProvider
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.SettingsNotificationConfigDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.SuggestedCategory
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.SeedCategoriesUseCase
import com.alorma.caducity.feature.consent.ConsentFlag
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.consent.ConsentPreferences
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.screen.transform.TransformFeature
import com.alorma.fireandforget.multiplatform.settings.SettingsFireAndForgetRunner
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OnboardingViewModelTest {
  private class FakeScheduler : ExpirationWorkScheduler {
    val calls = mutableListOf<String>()

    override fun scheduleExpirationCheck(time: LocalTime) {
      calls += "schedule $time"
    }

    override fun rescheduleExpirationCheck(time: LocalTime) {
      calls += "reschedule $time"
    }

    override fun cancelExpirationCheck() {
      calls += "cancel"
    }

    override fun triggerImmediateCheck() = Unit
  }

  private val now = Instant.parse("2026-10-11T10:00:00Z")
  private val clock =
    object : AppClock {
      override fun now(): Instant = now

      override fun nowDate(timeZone: TimeZone): LocalDate = now.date(timeZone)
    }

  private val settings = MapSettings()
  private val runner = SettingsFireAndForgetRunner(settings)
  private val onboardingFlag = OnboardingFlag(runner)
  private val legacyOnboardingFlag = LegacyOnboardingFlag(runner)
  private val consentFlag = ConsentFlag(runner)
  private val consentManager = mock<ConsentManager>()
  private val disclaimer = DisclaimerPreferences(settings)
  private val thresholds = SoonThresholdPreferences(settings)
  private val notificationConfig = SettingsNotificationConfigDataSource(settings)
  private val scheduler = FakeScheduler()
  private var transformEnabled = false
  private val transformRunner =
    object : RemoteConfigRunner() {
      override suspend fun fetchAndActivate(): Result<Boolean> = Result.success(true)

      override fun getBoolean(
        key: String,
        defaultValue: Boolean,
      ): Boolean = transformEnabled
    }

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var vm: OnboardingViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    val context = ApplicationProvider.getApplicationContext<Application>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    val itemMapper = ItemRoomMapper(clock, AppExpirationThresholdsImpl(thresholds))
    pantry =
      RoomPantryDataSource(
        database = db,
        categoryDao = db.categoryDao(),
        itemDao = db.itemDao(),
        productDao = db.productDao(),
        categoryMapper = CategoryRoomMapper(ProductRoomMapper(), itemMapper),
        appClock = clock,
      )
    vm = newViewModel()
  }

  @After
  fun tearDown() {
    vm.viewModelScope.cancel()
    db.close()
    Dispatchers.resetMain()
  }

  private fun newViewModel() =
    OnboardingViewModel(
      onboardingFlag = onboardingFlag,
      legacyOnboardingFlag = legacyOnboardingFlag,
      consentFlag = consentFlag,
      consentManager = consentManager,
      disclaimerPreferences = disclaimer,
      soonThresholdPreferences = thresholds,
      notificationConfig = notificationConfig,
      workScheduler = scheduler,
      seedCategories = SeedCategoriesUseCase(pantry),
      stringProvider = StringProvider(ApplicationProvider.getApplicationContext()),
      appClock = clock,
      eventTracker = EventTracker(emptyList()),
      transformFeature = TransformFeature(transformRunner),
    )

  private fun OnboardingViewModel.goTo(step: OnboardingStep) {
    while (state.value.step != step) onContinue()
  }

  private suspend fun OnboardingViewModel.acceptAndAwait(): OnboardingNavigationSideEffect {
    onAccept()
    return withContext(Dispatchers.Default) { withTimeout(5_000) { navigationSideEffects.first() } }
  }

  @Test
  fun `a fresh install starts at the welcome and walks the five steps without skipping`() {
    expectThat(vm.state.value.step).isEqualTo(OnboardingStep.Welcome)
    expectThat(vm.state.value.stepNumber).isNull()
    val seen = mutableListOf(vm.state.value.step to vm.state.value.stepNumber)
    repeat(4) {
      vm.onContinue()
      seen += vm.state.value.step to vm.state.value.stepNumber
    }
    expectThat(seen.map { it.first })
      .containsExactly(
        OnboardingStep.Welcome,
        OnboardingStep.Categories,
        OnboardingStep.Alerts,
        OnboardingStep.Privacy,
        OnboardingStep.Disclaimer,
      )
    expectThat(seen.map { it.second }).containsExactly(null, 1, 2, 3, 4)
    expectThat(vm.state.value.stepCount).isEqualTo(4)
    vm.onContinue()
    expectThat(vm.state.value.step).isEqualTo(OnboardingStep.Disclaimer)
  }

  @Test
  fun `with transform enabled the Transforma step goes between alerts and privacy and the steps are five`() {
    transformEnabled = true
    val withTransform = newViewModel()
    try {
      expectThat(withTransform.state.value.stepCount).isEqualTo(5)
      val seen = mutableListOf(withTransform.state.value.step to withTransform.state.value.stepNumber)
      repeat(5) {
        withTransform.onContinue()
        seen += withTransform.state.value.step to withTransform.state.value.stepNumber
      }
      expectThat(seen.map { it.first }).containsExactly(*OnboardingStep.entries.toTypedArray())
      expectThat(seen.map { it.second }).containsExactly(null, 1, 2, 3, 4, 5)

      withTransform.onBack()
      withTransform.onBack()
      expectThat(withTransform.state.value.step).isEqualTo(OnboardingStep.Transform)
      withTransform.onBack()
      expectThat(withTransform.state.value.step).isEqualTo(OnboardingStep.Alerts)
    } finally {
      withTransform.viewModelScope.cancel()
    }
  }

  @Test
  fun `the Transforma step is not part of the returning flow`() {
    val state = OnboardingState(returning = true, transformStep = true)
    expectThat(state.steps)
      .containsExactly(OnboardingStep.Welcome, OnboardingStep.Privacy, OnboardingStep.Disclaimer)
  }

  @Test
  fun `back goes to the previous step and stops at the welcome`() {
    vm.goTo(OnboardingStep.Privacy)
    vm.onBack()
    expectThat(vm.state.value.step).isEqualTo(OnboardingStep.Alerts)
    vm.onBack()
    vm.onBack()
    vm.onBack()
    expectThat(vm.state.value.step).isEqualTo(OnboardingStep.Welcome)
    expectThat(vm.state.value.canGoBack).isFalse()
  }

  @Test
  fun `the five default categories are preselected and can be toggled`() {
    expectThat(vm.state.value.selectedCategories)
      .containsExactlyInAnyOrder(
        SuggestedCategory.DAIRY,
        SuggestedCategory.PRODUCE,
        SuggestedCategory.MEAT_FISH,
        SuggestedCategory.PANTRY,
        SuggestedCategory.BAKERY,
      )
    vm.onToggleCategory(SuggestedCategory.DRINKS)
    vm.onToggleCategory(SuggestedCategory.DAIRY)
    expectThat(vm.state.value.selectedCategories.size).isEqualTo(5)
    expectThat(SuggestedCategory.DAIRY in vm.state.value.selectedCategories).isFalse()
    expectThat(SuggestedCategory.DRINKS in vm.state.value.selectedCategories).isTrue()
  }

  @Test
  fun `accepting is impossible until the box is ticked`() =
    runTest {
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onAccept()
      expectThat(disclaimer.acceptedVersion()).isNull()
      expectThat(onboardingFlag.isEnabled()).isTrue()
      expectThat(pantry.getCategories()).isEmpty()
    }

  @Test
  fun `accepting saves the notice version and date, the chosen categories with shape and icon, and finishes`() =
    runTest {
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onToggleCategory(SuggestedCategory.COLD_CUTS)
      vm.onDisclaimerCheckedChange(true)
      expectThat(vm.acceptAndAwait()).isEqualTo(OnboardingNavigationSideEffect.NavigateToApp)

      expectThat(disclaimer.acceptedVersion()).isEqualTo(DisclaimerPreferences.CURRENT_VERSION)
      expectThat(disclaimer.acceptedAt()).isEqualTo(now)
      expectThat(onboardingFlag.isEnabled()).isFalse()
      expectThat(consentFlag.isEnabled()).isFalse()

      val categories = pantry.getCategories()
      expectThat(categories.map { it.name })
        .containsExactly("Dairy", "Fruit & veg", "Meat & fish", "Dry goods", "Bakery", "Cold cuts")
      expectThat(categories.map { it.sortOrder }).containsExactly(0, 1, 2, 3, 4, 5)
      expectThat(categories.first().shape).isEqualTo(CategoryShape.COOKIE)
      expectThat(categories.map { it.icon.id })
        .containsExactly("water_full", "nutrition", "set_meal", "grocery", "bakery_dining", "kebab_dining")
    }

  @Test
  fun `every suggested category is created with the margin that fits what it is`() =
    runTest {
      SuggestedCategory.entries.forEach { if (it !in vm.state.value.selectedCategories) vm.onToggleCategory(it) }
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onDisclaimerCheckedChange(true)
      vm.acceptAndAwait()

      val margins = pantry.getCategories().associate { it.name to it.soonThresholdDays }
      expectThat(margins).isEqualTo(
        mapOf(
          "Dairy" to 3,
          "Fruit & veg" to 3,
          "Meat & fish" to 2,
          "Dry goods" to 30,
          "Bakery" to 2,
          "Drinks" to 7,
          "Cold cuts" to 3,
          "Ready meals" to 2,
        ),
      )
    }

  @Test
  fun `no categories is allowed`() =
    runTest {
      vm.goTo(OnboardingStep.Categories)
      SuggestedCategory.entries.forEach { if (it in vm.state.value.selectedCategories) vm.onToggleCategory(it) }
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onDisclaimerCheckedChange(true)
      vm.acceptAndAwait()

      expectThat(pantry.getCategories()).isEmpty()
      expectThat(onboardingFlag.isEnabled()).isFalse()
    }

  @Test
  fun `analytics are off by default and granted only when the user turns them on`() =
    runTest {
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onDisclaimerCheckedChange(true)
      vm.acceptAndAwait()
      verify(consentManager).setConsentPreferences(ConsentPreferences.DEFAULT)
      verify(consentManager, never()).setConsentPreferences(ConsentPreferences.ANALYTICS_ONLY)
    }

  @Test
  fun `turning analytics on grants only analytics`() =
    runTest {
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onAnalyticsChange(true)
      vm.onDisclaimerCheckedChange(true)
      vm.acceptAndAwait()
      verify(consentManager).setConsentPreferences(ConsentPreferences.ANALYTICS_ONLY)
    }

  @Test
  fun `permission granted turns the reminder on, schedules it at the chosen time and moves on`() =
    runTest {
      vm.goTo(OnboardingStep.Alerts)
      vm.onThresholdChange(7)
      vm.onReminderTimeChange(LocalTime(8, 30))
      vm.onRemindersGranted()
      expectThat(vm.state.value.step).isEqualTo(OnboardingStep.Privacy)
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onDisclaimerCheckedChange(true)
      vm.acceptAndAwait()

      expectThat(thresholds.current()).isEqualTo(7)
      expectThat(notificationConfig.isNotificationsEnabled()).isTrue()
      expectThat(notificationConfig.getNotificationTime()).isEqualTo(LocalTime(8, 30))
      expectThat(scheduler.calls).containsExactly("schedule 08:30")
    }

  @Test
  fun `permission denied or not now keeps the reminder off and moves on`() =
    runTest {
      vm.goTo(OnboardingStep.Alerts)
      vm.onRemindersDeclined()
      expectThat(vm.state.value.step).isEqualTo(OnboardingStep.Privacy)
      vm.goTo(OnboardingStep.Disclaimer)
      vm.onDisclaimerCheckedChange(true)
      vm.acceptAndAwait()

      expectThat(notificationConfig.isNotificationsEnabled()).isFalse()
      expectThat(thresholds.current()).isEqualTo(SoonThresholdPreferences.DEFAULT_DAYS)
      expectThat(scheduler.calls).containsExactly("cancel")
    }

  @Test
  fun `an existing user with an old notice only sees the notice and nothing else is touched`() =
    runTest {
      onboardingFlag.disable()
      thresholds.setThresholdDays(7)
      val reaccept = newViewModel()
      expectThat(reaccept.state.value.reacceptOnly).isTrue()
      expectThat(reaccept.state.value.step).isEqualTo(OnboardingStep.Disclaimer)
      expectThat(reaccept.state.value.stepNumber).isNull()
      expectThat(reaccept.state.value.canGoBack).isFalse()

      reaccept.onBack()
      reaccept.onContinue()
      expectThat(reaccept.state.value.step).isEqualTo(OnboardingStep.Disclaimer)

      reaccept.onAnalyticsChange(true)
      reaccept.onDisclaimerCheckedChange(true)
      expectThat(reaccept.acceptAndAwait()).isEqualTo(OnboardingNavigationSideEffect.NavigateToApp)

      expectThat(disclaimer.acceptedVersion()).isEqualTo(DisclaimerPreferences.CURRENT_VERSION)
      expectThat(disclaimer.needsAcceptance()).isFalse()
      expectThat(pantry.getCategories()).isEmpty()
      expectThat(scheduler.calls).isEmpty()
      expectThat(thresholds.current()).isEqualTo(7)
      verify(consentManager, never()).setConsentPreferences(ConsentPreferences.ANALYTICS_ONLY)
      reaccept.viewModelScope.cancel()
    }

  @Test
  fun `an existing user who already accepted the current notice does not need the notice again`() {
    onboardingFlag.disable()
    disclaimer.accept(now)
    expectThat(newViewModel().state.value.reacceptOnly).isFalse()
  }

  @Test
  fun `a user from before 1_0_0 gets welcome, privacy and notice, and keeps categories, margin and reminder`() =
    runTest {
      legacyOnboardingFlag.disable()
      thresholds.setThresholdDays(7)
      whenever(consentManager.getConsentPreferences()).thenReturn(ConsentPreferences.ANALYTICS_ONLY)
      val returning = newViewModel()
      expectThat(returning.state.value.returning).isTrue()
      expectThat(returning.state.value.reacceptOnly).isFalse()
      expectThat(returning.state.value.analyticsEnabled).isTrue()
      expectThat(returning.state.value.stepNumber).isNull()

      returning.onContinue()
      expectThat(returning.state.value.step).isEqualTo(OnboardingStep.Privacy)
      returning.onContinue()
      expectThat(returning.state.value.step).isEqualTo(OnboardingStep.Disclaimer)
      returning.onBack()
      expectThat(returning.state.value.step).isEqualTo(OnboardingStep.Privacy)
      returning.onContinue()

      returning.onAnalyticsChange(false)
      returning.onDisclaimerCheckedChange(true)
      expectThat(returning.acceptAndAwait()).isEqualTo(OnboardingNavigationSideEffect.NavigateToApp)

      expectThat(disclaimer.needsAcceptance()).isFalse()
      expectThat(onboardingFlag.isEnabled()).isFalse()
      expectThat(pantry.getCategories()).isEmpty()
      expectThat(scheduler.calls).isEmpty()
      expectThat(thresholds.current()).isEqualTo(7)
      verify(consentManager).setConsentPreferences(ConsentPreferences.DEFAULT)
      returning.viewModelScope.cancel()
    }

  @Test
  fun `a fresh install is not treated as a returning user`() {
    expectThat(newViewModel().state.value.returning).isFalse()
  }
}
