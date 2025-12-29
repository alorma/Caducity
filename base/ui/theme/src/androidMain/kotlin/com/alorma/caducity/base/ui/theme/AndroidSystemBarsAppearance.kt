package com.alorma.caducity.base.ui.theme

import android.app.Activity
import androidx.core.view.WindowInsetsControllerCompat

class AndroidSystemBarsAppearance(
  private val activity: Activity
) : SystemBarsAppearance {

  private val windowInsetsController by lazy {
    WindowInsetsControllerCompat(activity.window, activity.window.decorView)
  }

  override fun setLightStatusBars(isLight: Boolean) {
    windowInsetsController.isAppearanceLightStatusBars = isLight
  }

  override fun setLightNavigationBars(isLight: Boolean) {
    windowInsetsController.isAppearanceLightNavigationBars = isLight
  }
}
