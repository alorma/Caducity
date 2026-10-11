package com.alorma.caducity.feature.tracking

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HomeActionsTest {
  @Test
  fun `calendar action carries its source`() {
    val action = NavigateToCalendarFromHomeAction("week_strip")

    expectThat(action.name).isEqualTo("nav_calendar")
    expectThat(action.parameters).isEqualTo(mapOf("origin" to "home", "source" to "week_strip"))
  }

  @Test
  fun `search action`() {
    val action = NavigateToSearchAction()

    expectThat(action.name).isEqualTo("nav_search")
    expectThat(action.parameters).isEqualTo(mapOf("origin" to "home", "source" to "button"))
  }

  @Test
  fun `expired action comes from the banner`() {
    val action = NavigateToExpiredFromHomeAction()

    expectThat(action.name).isEqualTo("nav_expired")
    expectThat(action.parameters).isEqualTo(mapOf("origin" to "home", "source" to "banner"))
  }

  @Test
  fun `pantry action carries its source`() {
    val action = NavigateToPantryFromHomeAction("summary_fresh")

    expectThat(action.name).isEqualTo("nav_pantry")
    expectThat(action.parameters).isEqualTo(mapOf("origin" to "home", "source" to "summary_fresh"))
  }

  @Test
  fun `item actions and add actions never carry user data`() {
    expectThat(NavigateToItemActionsFromHomeAction().parameters)
      .isEqualTo(mapOf("origin" to "home", "source" to "row"))
    expectThat(NavigateToAddFromHomeAction().parameters)
      .isEqualTo(mapOf("origin" to "home", "source" to "empty_cta"))
  }
}
