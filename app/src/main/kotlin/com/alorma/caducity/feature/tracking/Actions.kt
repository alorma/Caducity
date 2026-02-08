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

// Onboarding Actions
class CompleteOnboardingAction : NavigationAction(
  actionName = "complete_onboarding",
  origin = "onboarding",
  parameters = emptyMap()
)

// Create Category Actions
class CancelCreateCategoryAction : NavigationAction(
  actionName = "cancel_create_category",
  origin = "create_category",
  parameters = mapOf("source" to "back_button")
)

class CategoryCreatedAction(source: String) : NavigationAction(
  actionName = "category_created",
  origin = "create_category",
  parameters = mapOf("source" to source) // "form_submit"
)

// Add Item Actions
class CancelAddItemAction : NavigationAction(
  actionName = "cancel_add_item",
  origin = "add_item",
  parameters = mapOf("source" to "back_button")
)

class ItemSavedAction(hasProduct: Boolean, quantity: Int) : NavigationAction(
  actionName = "item_saved",
  origin = "add_item",
  parameters = mapOf(
    "has_product" to hasProduct.toString(),
    "quantity_range" to when {
      quantity == 1 -> "single"
      quantity in 2..5 -> "few"
      quantity in 6..10 -> "several"
      else -> "many"
    }
  )
)
