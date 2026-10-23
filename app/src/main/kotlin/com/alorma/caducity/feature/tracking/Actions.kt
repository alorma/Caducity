package com.alorma.caducity.feature.tracking

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

// Category editor Actions
class CategoryEditorCloseAction(
  mode: String,
) : NavigationAction(
    actionName = "close_category_editor",
    origin = "category_editor",
    parameters = mapOf("mode" to mode), // "create" or "edit"
  )

class CategorySavedAction(
  mode: String,
) : NavigationAction(
    actionName = "category_saved",
    origin = "category_editor",
    parameters = mapOf("mode" to mode), // "create" or "edit"
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

// Category Actions
class NavigateToAddItemFromCategoryAction(
  source: String, // fab
) : NavigationAction(
    actionName = "add_item",
    origin = "category",
    parameters = mapOf("source" to source),
  )

class NavigateToCalendarFromCategoryAction :
  NavigationAction(
    actionName = "calendar",
    origin = "category",
    parameters = mapOf("source" to "topbar"),
  )

class NavigateToCategoryEditorAction :
  NavigationAction(
    actionName = "edit_category",
    origin = "category",
    parameters = mapOf("source" to "menu"),
  )

class NavigateToItemActionsFromCategoryAction :
  NavigationAction(
    actionName = "item_actions",
    origin = "category",
    parameters = mapOf("source" to "row"),
  )

class CategoryFilterAction(
  status: String, // all, expired, soon, fresh, frozen
) : Action(name = "category_filter", parameters = mapOf("status" to status))

class CategoryDeletedAction :
  NavigationAction(
    actionName = "category_deleted",
    origin = "category",
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
class AddCloseAction :
  NavigationAction(
    actionName = "close_add",
    origin = "add",
  )

// Transform Screen Actions

/**
 * [origin]: "status_list", "item_actions" or "home"; [source]: "selection", "item_actions",
 * "cooked_card" (the «Has cuinat?» card of Home) or "intro_modal" («Prova Transforma» of the «Novetat Transforma» modal).
 */
class NavigateToTransformAction(
  origin: String,
  source: String,
) : NavigationAction(
    actionName = "transform",
    origin = origin,
    parameters = mapOf("source" to source),
  )

/** «Ara no» (or a tap outside) on the «Novetat Transforma» modal. */
class DismissTransformIntroAction :
  NavigationAction(
    actionName = "dismiss_transform_intro",
    origin = "home",
    parameters = mapOf("source" to "intro_modal"),
  )

class TransformCloseAction :
  NavigationAction(
    actionName = "close_transform",
    origin = "transform",
    parameters = emptyMap(),
  )

class NavigateToIngredientPickerAction :
  NavigationAction(
    actionName = "ingredient_picker",
    origin = "transform",
    parameters = mapOf("source" to "add_ingredient"),
  )

class AddQuickCategoryAction :
  NavigationAction(
    actionName = "quick_category",
    origin = "add",
    parameters = mapOf("source" to "category_chips"),
  )

class ChangeSuggestedDurationAction :
  NavigationAction(
    actionName = "category_duration",
    origin = "add",
    parameters = mapOf("source" to "suggested_line"),
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

// Home Actions
class NavigateToCalendarFromHomeAction(
  source: String, // button, week_strip
) : NavigationAction(
    actionName = "calendar",
    origin = "home",
    parameters = mapOf("source" to source),
  )

class NavigateToSearchAction :
  NavigationAction(
    actionName = "search",
    origin = "home",
    parameters = mapOf("source" to "button"),
  )

class NavigateToPantryFromHomeAction(
  source: String, // see_pantry
) : NavigationAction(
    actionName = "pantry",
    origin = "home",
    parameters = mapOf("source" to source),
  )

class NavigateToItemActionsFromHomeAction :
  NavigationAction(
    actionName = "item_actions",
    origin = "home",
    parameters = mapOf("source" to "row"),
  )

class NavigateToAddFromHomeAction :
  NavigationAction(
    actionName = "add",
    origin = "home",
    parameters = mapOf("source" to "empty_cta"),
  )

// Calendar Actions
class CalendarDaySelectAction : Action(name = "calendar_day_select")

class NavigateToItemActionsFromCalendarAction :
  NavigationAction(
    actionName = "item_actions",
    origin = "calendar",
    parameters = mapOf("source" to "row"),
  )

// Pantry Actions
class NavigateToCategoryFromPantryAction(
  source: String, // card, search_result
) : NavigationAction(
    actionName = "category",
    origin = "pantry",
    parameters = mapOf("source" to source),
  )

class NavigateToNewCategoryFromPantryAction :
  NavigationAction(
    actionName = "create_category",
    origin = "pantry",
    parameters = mapOf("source" to "card"),
  )

class NavigateToStatusListFromHomeAction(
  status: String,
  source: String,
) : NavigationAction(
    actionName = "status_list",
    origin = "home",
    parameters = mapOf("status" to status, "source" to source),
  )

class NavigateToItemActionsFromStatusListAction(
  status: String,
) : NavigationAction(
    actionName = "item_actions",
    origin = "status_list",
    parameters = mapOf("status" to status, "source" to "row"),
  )

class NavigateToAddFromStatusListAction(
  status: String,
) : NavigationAction(
    actionName = "add",
    origin = "status_list",
    parameters = mapOf("status" to status, "source" to "empty"),
  )

// Settings Actions
class NavigateFromSettingsAction(
  destination: String,
) : NavigationAction(
    actionName = "settings_$destination",
    origin = "settings",
    parameters = emptyMap(), // destination: "backup", "privacy", "about" or "debug"
  )
