package com.alorma.caducity.ui.screen.settings

sealed interface SettingsNavigation {
  data object Backup : SettingsNavigation

  data object CategoryMargins : SettingsNavigation

  data object Privacy : SettingsNavigation

  data object About : SettingsNavigation

  data object Debug : SettingsNavigation
}

sealed interface SettingsNavigationSideEffect {
  data object OpenBackup : SettingsNavigationSideEffect

  data object OpenCategoryMargins : SettingsNavigationSideEffect

  data object OpenPrivacy : SettingsNavigationSideEffect

  data object OpenAbout : SettingsNavigationSideEffect

  data object OpenDebug : SettingsNavigationSideEffect
}
