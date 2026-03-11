package com.alorma.caducity.feature.tracking

// Dashboard Actions
class NavigateToCreateCategoryAction :
  NavigationAction(
    actionName = "create_category",
    origin = "dashboard",
    parameters = mapOf("source" to "fab"),
  )

class NavigateToCategoryAction(
  source: String,
) : NavigationAction(
    actionName = "category",
    origin = "dashboard",
    parameters = mapOf("source" to source), // "category_title" or "calendar_date"
  )

class NavigateToFilteredItemsAction(
  status: String,
) : NavigationAction(
    actionName = "filtered_items",
    origin = "dashboard",
    parameters =
      mapOf(
        "status" to status, // "expired", "expiring_soon", "fresh", "frozen"
        "source" to "summary",
      ),
  )

class NavigateToSettingsAction :
  NavigationAction(
    actionName = "settings",
    origin = "dashboard",
    parameters = mapOf("source" to "topbar"),
  )

class NavigateToAiAssistantAction :
  NavigationAction(
    actionName = "ai_assistant",
    origin = "dashboard",
    parameters = mapOf("source" to "topbar"),
  )

// Onboarding Actions
class CompleteOnboardingAction :
  NavigationAction(
    actionName = "complete_onboarding",
    origin = "onboarding",
    parameters = emptyMap(),
  )

// Create Category Actions
class CancelCreateCategoryAction :
  NavigationAction(
    actionName = "cancel_create_category",
    origin = "create_category",
    parameters = mapOf("source" to "back_button"),
  )

class CategoryCreatedAction(
  source: String,
) : NavigationAction(
    actionName = "category_created",
    origin = "create_category",
    parameters = mapOf("source" to source), // "form_submit"
  )

// Add Item Actions
class CancelAddItemAction :
  NavigationAction(
    actionName = "cancel_add_item",
    origin = "add_item",
    parameters = mapOf("source" to "back_button"),
  )

class ItemSavedAction(
  hasProduct: Boolean,
  quantity: Int,
) : NavigationAction(
    actionName = "item_saved",
    origin = "add_item",
    parameters =
      mapOf(
        "has_product" to hasProduct.toString(),
        "quantity_range" to
          when {
            quantity == 1 -> "single"
            quantity in 2..5 -> "few"
            quantity in 6..10 -> "several"
            else -> "many"
          },
      ),
  )

// Product Page Actions
class NavigateToAddItemFromProductAction :
  NavigationAction(
    actionName = "add_item",
    origin = "product_page",
    parameters = mapOf("source" to "fab"),
  )

class ProductDeletedAction :
  NavigationAction(
    actionName = "product_deleted",
    origin = "product_page",
    parameters = mapOf("source" to "delete_dialog"),
  )

// Category Detail Actions
class NavigateToAddItemFromCategoryAction(
  source: String,
  hasProduct: Boolean,
) : NavigationAction(
    actionName = "add_item",
    origin = "category_detail",
    parameters =
      mapOf(
        "source" to source, // "fab", "product_tab"
        "has_product" to hasProduct.toString(),
      ),
  )

class CategoryDeletedAction :
  NavigationAction(
    actionName = "category_deleted",
    origin = "category_detail",
    parameters = mapOf("source" to "delete_dialog"),
  )

// Filtered Items Actions
class NavigateToCategoryFromFilteredAction(
  source: String,
) : NavigationAction(
    actionName = "category",
    origin = "filtered_items",
    parameters = mapOf("source" to source), // "category_header"
  )

// Item Actions (non-navigation)
class ItemConsumedAction(
  itemStatus: String,
  parameters: Map<String, String> = emptyMap(),
) : Action(
    name = "item_consumed",
    parameters = mapOf("item_status" to itemStatus) + parameters,
  )

class ItemFrozenAction(
  itemStatus: String,
  parameters: Map<String, String> = emptyMap(),
) : Action(
    name = "item_frozen",
    parameters = mapOf("item_status" to itemStatus) + parameters,
  )

class ItemUnfrozenAction(
  parameters: Map<String, String> = emptyMap(),
) : Action(
    name = "item_unfrozen",
    parameters = parameters,
  )

class ItemRescheduledAction(
  itemStatus: String,
  daysChanged: String,
) : Action(
    name = "item_rescheduled",
    parameters =
      mapOf(
        "item_status" to itemStatus, // "fresh", "expiring_soon", "expired", "frozen"
        "days_changed" to daysChanged, // "earlier", "later", "no_change"
      ),
  )

class ItemDeletedAction(
  itemStatus: String,
  parameters: Map<String, String> = emptyMap(),
) : Action(
    name = "item_deleted",
    parameters = mapOf("item_status" to itemStatus) + parameters,
  )
