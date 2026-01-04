package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.config.navigation.TopLevelDestinationsParam
import com.alorma.caducity.config.navigation.TopLevelRoute
import com.alorma.caducity.feature.notification.ExpirationNotificationHelper
import com.alorma.caducity.ui.theme.AndroidSystemBarsAppearance
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    // Check if we should show expiring products only (from notification tap)
    val initialDestination = parseNavigationDestination()

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this)
      ) {
        App(initialDestination = initialDestination)
      }
    }
  }

  private fun parseNavigationDestination(): TopLevelRoute? {
    val destinationsParam = intent.getParcelableExtra(
      ExpirationNotificationHelper.Destiation,
      TopLevelDestinationsParam::class.java,
    )

    return when (destinationsParam) {
      TopLevelDestinationsParam.Dashboard -> TopLevelRoute.Dashboard
      TopLevelDestinationsParam.Products -> TopLevelRoute.Products
      TopLevelDestinationsParam.Settings -> TopLevelRoute.Settings
      null -> null
    }
  }
}
