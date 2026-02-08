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

// Product Page Actions
class NavigateToAddItemFromProductAction : NavigationAction(
  actionName = "add_item",
  origin = "product_page",
  parameters = mapOf("source" to "fab")
)

class ProductDeletedAction : NavigationAction(
  actionName = "product_deleted",
  origin = "product_page",
  parameters = mapOf("source" to "delete_dialog")
)

// Category Detail Actions
class NavigateToAddItemFromCategoryAction(source: String, hasProduct: Boolean) : NavigationAction(
  actionName = "add_item",
  origin = "category_detail",
  parameters = mapOf(
    "source" to source,  // "fab", "product_tab"
    "has_product" to hasProduct.toString()
  )
)

class CategoryDeletedAction : NavigationAction(
  actionName = "category_deleted",
  origin = "category_detail",
  parameters = mapOf("source" to "delete_dialog")
)

// Filtered Items Actions
class NavigateToCategoryFromFilteredAction(source: String) : NavigationAction(
  actionName = "category",
  origin = "filtered_items",
  parameters = mapOf("source" to source)  // "category_header"
)
