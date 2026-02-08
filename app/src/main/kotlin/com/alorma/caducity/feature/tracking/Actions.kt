package com.alorma.caducity.feature.tracking

// Dashboard Actions
class NavigateToCreateCategoryAction : Action(
  name = "navigate_to_create_category",
  parameters = mapOf("source" to "dashboard_fab")
)

class NavigateToCategoryAction(source: String) : Action(
  name = "navigate_to_category",
  parameters = mapOf("source" to source) // "category_title" or "calendar_date"
)

class NavigateToFilteredItemsAction(status: String) : Action(
  name = "navigate_to_filtered_items",
  parameters = mapOf(
    "status" to status, // "expired", "expiring_soon", "fresh", "frozen"
    "source" to "dashboard_summary"
  )
)

class NavigateToSettingsAction : Action(
  name = "navigate_to_settings",
  parameters = mapOf("source" to "dashboard_topbar")
)
