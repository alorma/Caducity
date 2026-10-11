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
  fun `status list action carries the status and where it came from`() {
    val action = NavigateToStatusListFromHomeAction("expired", "banner")

    expectThat(action.name).isEqualTo("nav_status_list")
    expectThat(action.parameters)
      .isEqualTo(mapOf("status" to "expired", "source" to "banner", "origin" to "home"))
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
