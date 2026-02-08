package com.alorma.caducity.feature.tracking

/**
 * Concrete Screen event classes for all screens in the app.
 * Each screen has its own type-safe tracking event.
 *
 * All screen names include the "Screen" suffix for consistency in analytics.
 */

// Main feature screens
class DashboardScreen : Screen("DashboardScreen")
class CategoryDetailScreen : Screen("CategoryDetailScreen")
class AddItemScreen : Screen("AddItemScreen")
class CreateCategoryScreen : Screen("CreateCategoryScreen")
class FilteredItemsByStatusScreen : Screen("FilteredItemsByStatusScreen")
class OnboardingScreen : Screen("OnboardingScreen")

// Settings screens
class SettingsScreen : Screen("SettingsScreen")
class AppearanceSettingsScreen : Screen("AppearanceSettingsScreen")
class BackupScreen : Screen("BackupScreen")
class NotificationsSettingsScreen : Screen("NotificationsSettingsScreen")
class PrivacySettingsScreen : Screen("PrivacySettingsScreen")
class DebugSettingsScreen : Screen("DebugSettingsScreen")
class AboutScreen : Screen("AboutScreen")

// Bottom sheets (tracked as screens)
class ItemActionsBottomSheetScreen : Screen("ItemActionsBottomSheetScreen")
class ProductDeleteBottomSheetScreen : Screen("ProductDeleteBottomSheetScreen")
class ProductClearItemsBottomSheetScreen : Screen("ProductClearItemsBottomSheetScreen")
class ProductItemsBottomSheetScreen : Screen("ProductItemsBottomSheetScreen")
