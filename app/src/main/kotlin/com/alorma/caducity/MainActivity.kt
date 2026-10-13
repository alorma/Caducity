package com.alorma.caducity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.BundleCompat
import com.alorma.caducity.feature.appicon.AppIconManager
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.ui.theme.AndroidSystemBarsAppearance
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance
import com.alorma.caducity.ui.theme.ThemePreferences
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
  private val consentManager: ConsentManager by inject()
  private val appIconManager: AppIconManager by inject()
  private val themePreferences: ThemePreferences by inject()
  private var pendingDeepLink by mutableStateOf<DeepLinkAction?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Apply default consent settings as early as possible
    consentManager.applyDefaultConsent()

    // Covers the first run and a restored setting (migration) where alias and palette disagree.
    appIconManager.apply(themePreferences.colorPalette.value)

    enableEdgeToEdge()

    // A recreated activity (rotation, theme) must not replay the launch intent.
    if (savedInstanceState == null) pendingDeepLink = intent.deepLinkAction()

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this),
      ) {
        App(
          deepLinkAction = pendingDeepLink,
          onDeepLinkHandled = { pendingDeepLink = null },
        )
      }
    }
  }

  override fun onStop() {
    super.onStop()
    // The launcher icon follows the palette. Swapping it can make some launchers close the app,
    // so it is done when the app goes to the background (and at startup), not while choosing.
    appIconManager.apply(themePreferences.colorPalette.value)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.deepLinkAction()?.let { pendingDeepLink = it }
  }

  private fun Intent.deepLinkAction(): DeepLinkAction? =
    BundleCompat.getParcelable(
      extras ?: Bundle.EMPTY,
      EXTRA_DEEP_LINK_ACTION,
      DeepLinkAction::class.java,
    )

  companion object {
    const val EXTRA_DEEP_LINK_ACTION = "extra_deep_link_action"
  }
}
