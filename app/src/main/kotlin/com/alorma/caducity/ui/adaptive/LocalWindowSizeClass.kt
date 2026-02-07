package com.alorma.caducity.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
  error("No WindowSizeClass provided")
}
