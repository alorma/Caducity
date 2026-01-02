package com.alorma.caducity.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

interface SystemBarsAppearance {
  fun setLightStatusBars(isLight: Boolean)
  fun setLightNavigationBars(isLight: Boolean)
}

object SystemBarsAppearanceNoOp: SystemBarsAppearance {
  override fun setLightStatusBars(isLight: Boolean) {

  }

  override fun setLightNavigationBars(isLight: Boolean) {
  }

}

val LocalSystemBarsAppearance = staticCompositionLocalOf<SystemBarsAppearance?> {
  null
}
