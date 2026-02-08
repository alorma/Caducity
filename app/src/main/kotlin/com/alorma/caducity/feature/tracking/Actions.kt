package com.alorma.caducity.feature.tracking

// Dashboard Actions
class NavigateToCreateCategoryAction : NavigationAction(
  actionName = "create_category",
  origin = "dashboard",
  parameters = mapOf("source" to "fab")
)

class NavigateToCategoryAction(source: String) : NavigationAction(
  actionName = "category",
  origin = "dashboard",
  parameters = mapOf("source" to source) // "category_title" or "calendar_date"
)

class NavigateToFilteredItemsAction(status: String) : NavigationAction(
  actionName = "filtered_items",
  origin = "dashboard",
  parameters = mapOf(
    "status" to status, // "expired", "expiring_soon", "fresh", "frozen"
    "source" to "summary"
  )
)

class NavigateToSettingsAction : NavigationAction(
  actionName = "settings",
  origin = "dashboard",
  parameters = mapOf("source" to "topbar")
)
