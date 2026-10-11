package com.alorma.caducity.ui.screen.settings

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.data.datasource.SettingsNotificationConfigDataSource
import com.alorma.caducity.domain.PantryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferencesNoOp
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
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

  private val settings = MapSettings()
  private val scheduler = FakeScheduler()
  private val thresholds = SoonThresholdPreferences(settings)
  private val firstDay = FirstDayOfWeekPreferences(settings)
  private val config = SettingsNotificationConfigDataSource(settings)
  private val categories =
    MutableStateFlow(
      listOf(
        Category("c1", "Rebost", "", soonThresholdDays = 30),
        Category("c2", "Làctics", "", soonThresholdDays = null),
        Category("c3", "Fleca", "", soonThresholdDays = 2),
      ),
    )
  private lateinit var vm: SettingsViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    val pantry = mock<PantryDataSource> { on { observeCategories() } doReturn categories }
    vm =
      SettingsViewModel(
        config,
        scheduler,
        thresholds,
        ObserveCategoriesUseCase(pantry),
        ThemePreferencesNoOp,
        firstDay,
        EventTracker(emptyList()),
      )
  }

  @After
  fun tearDown() {
    vm.viewModelScope.cancel()
    Dispatchers.resetMain()
  }

  @Test
  fun `turning the reminder on schedules the worker at the stored time and off cancels it`() =
    runTest {
      vm.onReminderToggle(true)
      expectThat(config.isNotificationsEnabled()).isEqualTo(true)
      vm.onReminderToggle(false)
      expectThat(config.isNotificationsEnabled()).isEqualTo(false)
      expectThat(scheduler.calls).containsExactly("schedule 09:00", "cancel")
    }

  @Test
  fun `changing the time or the threshold reschedules only while the reminder is on`() =
    runTest {
      vm.onReminderTimeChange(LocalTime(7, 30))
      vm.onSoonThresholdChange(7)
      expectThat(scheduler.calls).isEmpty()
      expectThat(config.getNotificationTime()).isEqualTo(LocalTime(7, 30))

      vm.onReminderToggle(true)
      vm.onReminderTimeChange(LocalTime(8, 0))
      vm.onSoonThresholdChange(1)
      expectThat(scheduler.calls).containsExactly("schedule 07:30", "reschedule 08:00", "reschedule 08:00")
    }

  @Test
  fun `the threshold changes the state and is stored`() =
    runTest {
      vm.onSoonThresholdChange(7)
      expectThat(thresholds.current()).isEqualTo(7)
      expectThat(vm.state.first { it.soonThresholdDays == 7 }.soonThresholdDays).isEqualTo(7)
    }

  @Test
  fun `the category margins are listed shortest first and follow the default when they have none`() =
    runTest {
      val margins = vm.state.first { it.categoryMargins.isNotEmpty() }.categoryMargins
      expectThat(margins).containsExactly(
        CategoryMarginSummary("Fleca", 2),
        CategoryMarginSummary("Làctics", 3),
        CategoryMarginSummary("Rebost", 30),
      )

      vm.onSoonThresholdChange(7)
      val updated = vm.state.first { it.soonThresholdDays == 7 }.categoryMargins
      expectThat(updated.first { it.name == "Làctics" }.days).isEqualTo(7)
      expectThat(updated.first { it.name == "Rebost" }.days).isEqualTo(30)
    }

  @Test
  fun `week start and theme reach the state`() =
    runTest {
      vm.onFirstDayOfWeekChange(DayOfWeek.SUNDAY)
      expectThat(firstDay.state.value.firstDayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
      expectThat(vm.state.first { it.firstDayOfWeek == DayOfWeek.SUNDAY }.themeMode).isEqualTo(ThemeMode.SYSTEM)
    }

  @Test
  fun `the time picker opens on the current time`() =
    runTest {
      vm.onReminderTimeClick()
      expectThat(vm.sideEffects.first()).isEqualTo(SettingsSideEffect.ShowTimePicker(LocalTime(9, 0)))
    }
}
