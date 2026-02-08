package com.alorma.caducity.feature.tracking

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventTrackerTest {

  @Test
  fun `trackScreen delegates to all trackers`() {
    // Given
    val tracker1 = TestTracker()
    val tracker2 = TestTracker()
    val eventTracker = EventTracker(listOf(tracker1, tracker2))
    val screen = Screen("TestScreen")

    // When
    eventTracker.trackScreen(screen)

    // Then
    expectThat(tracker1.trackedScreens as List<Screen>).isEqualTo(listOf(screen))
    expectThat(tracker2.trackedScreens as List<Screen>).isEqualTo(listOf(screen))
  }

  @Test
  fun `trackAction delegates to all trackers`() {
    // Given
    val tracker1 = TestTracker()
    val tracker2 = TestTracker()
    val eventTracker = EventTracker(listOf(tracker1, tracker2))
    val action = Action("TestAction", mapOf("key" to "value"))

    // When
    eventTracker.trackAction(action)

    // Then
    expectThat(tracker1.trackedActions as List<Action>).isEqualTo(listOf(action))
    expectThat(tracker2.trackedActions as List<Action>).isEqualTo(listOf(action))
  }

  @Test
  fun `trackScreen works with empty tracker list`() {
    // Given
    val eventTracker = EventTracker(emptyList())
    val screen = Screen("TestScreen")

    // When/Then - should not throw
    eventTracker.trackScreen(screen)
  }

  @Test
  fun `trackAction works with empty tracker list`() {
    // Given
    val eventTracker = EventTracker(emptyList())
    val action = Action("TestAction")

    // When/Then - should not throw
    eventTracker.trackAction(action)
  }

  @Test
  fun `multiple trackScreen calls are tracked`() {
    // Given
    val tracker = TestTracker()
    val eventTracker = EventTracker(listOf(tracker))
    val screen1 = Screen("Screen1")
    val screen2 = Screen("Screen2")

    // When
    eventTracker.trackScreen(screen1)
    eventTracker.trackScreen(screen2)

    // Then
    expectThat(tracker.trackedScreens as List<Screen>).isEqualTo(listOf(screen1, screen2))
  }

  @Test
  fun `multiple trackAction calls are tracked`() {
    // Given
    val tracker = TestTracker()
    val eventTracker = EventTracker(listOf(tracker))
    val action1 = Action("Action1")
    val action2 = Action("Action2", mapOf("param" to "value"))

    // When
    eventTracker.trackAction(action1)
    eventTracker.trackAction(action2)

    // Then
    expectThat(tracker.trackedActions as List<Action>).isEqualTo(listOf(action1, action2))
  }

  // Test double for Tracker
  private class TestTracker : Tracker {
    val trackedScreens = mutableListOf<Screen>()
    val trackedActions = mutableListOf<Action>()

    override fun trackScreen(screen: Screen) {
      trackedScreens.add(screen)
    }

    override fun trackAction(action: Action) {
      trackedActions.add(action)
    }
  }
}
