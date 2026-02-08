package com.alorma.caducity.feature.tracking

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DashboardActionsTest {

  @Test
  fun `NavigateToCreateCategoryAction has correct name and parameters`() {
    // When
    val action = NavigateToCreateCategoryAction()

    // Then
    expectThat(action.name).isEqualTo("nav_create_category")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "source" to "fab"
      )
    )
  }

  @Test
  fun `NavigateToCategoryAction with category_title source`() {
    // When
    val action = NavigateToCategoryAction("category_title")

    // Then
    expectThat(action.name).isEqualTo("nav_category")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "source" to "category_title"
      )
    )
  }

  @Test
  fun `NavigateToCategoryAction with calendar_date source`() {
    // When
    val action = NavigateToCategoryAction("calendar_date")

    // Then
    expectThat(action.name).isEqualTo("nav_category")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "source" to "calendar_date"
      )
    )
  }

  @Test
  fun `NavigateToFilteredItemsAction with expired status`() {
    // When
    val action = NavigateToFilteredItemsAction("expired")

    // Then
    expectThat(action.name).isEqualTo("nav_filtered_items")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "status" to "expired",
        "source" to "summary"
      )
    )
  }

  @Test
  fun `NavigateToFilteredItemsAction with expiring_soon status`() {
    // When
    val action = NavigateToFilteredItemsAction("expiring_soon")

    // Then
    expectThat(action.name).isEqualTo("nav_filtered_items")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "status" to "expiring_soon",
        "source" to "summary"
      )
    )
  }

  @Test
  fun `NavigateToFilteredItemsAction with fresh status`() {
    // When
    val action = NavigateToFilteredItemsAction("fresh")

    // Then
    expectThat(action.name).isEqualTo("nav_filtered_items")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "status" to "fresh",
        "source" to "summary"
      )
    )
  }

  @Test
  fun `NavigateToFilteredItemsAction with frozen status`() {
    // When
    val action = NavigateToFilteredItemsAction("frozen")

    // Then
    expectThat(action.name).isEqualTo("nav_filtered_items")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "status" to "frozen",
        "source" to "summary"
      )
    )
  }

  @Test
  fun `NavigateToSettingsAction has correct name and parameters`() {
    // When
    val action = NavigateToSettingsAction()

    // Then
    expectThat(action.name).isEqualTo("nav_settings")
    expectThat(action.parameters).isEqualTo(
      mapOf(
        "origin" to "dashboard",
        "source" to "topbar"
      )
    )
  }
}
