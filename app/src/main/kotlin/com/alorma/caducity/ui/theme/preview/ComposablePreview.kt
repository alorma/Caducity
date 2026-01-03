package com.alorma.caducity.ui.theme.preview

import androidx.compose.runtime.Composable
import com.alorma.caducity.ui.theme.AppThemeContent
import com.alorma.caducity.ui.theme.ThemePreferencesNoOp

@Suppress("ModifierRequired")
@Composable
fun PreviewTheme(
  block: @Composable () -> Unit,
) {
  AppThemeContent(themePreferences = ThemePreferencesNoOp) { block() }
}
