package com.alorma.caducity.base.ui.theme.colors

import android.app.UiModeManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.getSystemService

@Composable
fun obtainContrastLevel(): Double {
  return if (!LocalInspectionMode.current) {
    val context = LocalContext.current
    val uiModeManager = context.getSystemService<UiModeManager>()
    uiModeManager?.contrast?.toDouble() ?: 0.0
  } else {
    1.0
  }
}