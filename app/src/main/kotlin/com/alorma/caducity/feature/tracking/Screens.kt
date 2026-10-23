package com.alorma.caducity.feature.tracking

/**
 * Concrete Screen event classes for all screens in the app.
 * Each screen has its own type-safe tracking event.
 *
 * All screen names include the "Screen" suffix for consistency in analytics.
 */

class CategoryScreen : Screen("CategoryScreen")

class CategoryEditorScreen : Screen("CategoryEditorScreen")

class StatusListScreen(
  status: String,
) : Screen("StatusListScreen_$status")

class OnboardingScreen : Screen("OnboardingScreen")

// Settings screens
class SettingsScreen : Screen("SettingsScreen")

class BackupScreen : Screen("BackupScreen")

class PrivacySettingsScreen : Screen("PrivacySettingsScreen")

class DebugSettingsScreen : Screen("DebugSettingsScreen")

class DebugRemoteConfigsScreen : Screen("DebugRemoteConfigsScreen")

class AboutScreen : Screen("AboutScreen")

// Bottom sheets (tracked as screens)

class HomeScreen : Screen("HomeScreen")

class CalendarScreen : Screen("CalendarScreen")

class PantryScreen : Screen("PantryScreen")
