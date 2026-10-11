package com.alorma.caducity.ui.screen.settings

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.data.datasource.SettingsNotificationConfigDataSource
import com.alorma.caducity.feature.notification.ExpirationWorkScheduler
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferencesNoOp
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
  private lateinit var vm: SettingsViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    vm = SettingsViewModel(config, scheduler, thresholds, ThemePreferencesNoOp, firstDay, EventTracker(emptyList()))
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
