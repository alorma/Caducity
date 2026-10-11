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

// Shell (floating navigation bar) Actions
class NavigateToTabAction(
  tab: String,
) : NavigationAction(
    actionName = "tab",
    origin = "shell",
    parameters = mapOf("tab" to tab, "source" to "floating_bar"), // tab: "soon", "pantry" or "settings"
  )

class NavigateToAddAction :
  NavigationAction(
    actionName = "add",
    origin = "shell",
    parameters = mapOf("source" to "fab"),
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

// Add Screen Actions
class AddCloseAction(
  batch: Boolean,
) : NavigationAction(
    actionName = "close_add",
    origin = "add",
    parameters = mapOf("source" to if (batch) "done" else "close"),
  )

class AddQuickCategoryAction :
  NavigationAction(
    actionName = "quick_category",
    origin = "add",
    parameters = mapOf("source" to "category_chips"),
  )

class QuickCategoryCloseAction :
  NavigationAction(
    actionName = "close_quick_category",
    origin = "quick_category",
    parameters = mapOf("source" to "sheet"),
  )

// Item actions sheet Actions
class ItemActionAction(
  action: String, // consume, freeze, unfreeze, quick_date, pick_date, move, delete
) : Action(
    name = "item_action",
    parameters = mapOf("action" to action),
  )

class NavigateToEditItemAction :
  NavigationAction(
    actionName = "edit_item",
    origin = "item_actions",
    parameters = mapOf("source" to "sheet"),
  )
