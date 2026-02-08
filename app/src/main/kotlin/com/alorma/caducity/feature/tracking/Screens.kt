package com.alorma.caducity.feature.tracking

/**
 * Concrete Screen event classes for all screens in the app.
 * Each screen has its own type-safe tracking event.
 */

// Main feature screens
class DashboardScreen : Screen("Dashboard")
class CategoryDetailScreen : Screen("CategoryDetail")
class AddItemScreen : Screen("AddItem")
class CreateCategoryScreen : Screen("CreateCategory")
class FilteredItemsByStatusScreen : Screen("FilteredItemsByStatus")
class OnboardingScreen : Screen("Onboarding")

// Settings screens
class SettingsScreen : Screen("Settings")
class AppearanceSettingsScreen : Screen("AppearanceSettings")
class BackupScreen : Screen("Backup")
class NotificationsSettingsScreen : Screen("NotificationsSettings")
class DebugSettingsScreen : Screen("DebugSettings")
class AboutScreen : Screen("About")
