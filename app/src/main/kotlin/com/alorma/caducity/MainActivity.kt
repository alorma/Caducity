package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.os.BundleCompat
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.deeplink.DeepLinkAction
import com.alorma.caducity.ui.theme.AndroidSystemBarsAppearance
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
  private val consentManager: ConsentManager by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Apply default consent settings as early as possible
    consentManager.applyDefaultConsent()

    enableEdgeToEdge()

    val deepLinkAction =
      BundleCompat.getParcelable(
        intent.extras ?: Bundle.EMPTY,
        EXTRA_DEEP_LINK_ACTION,
        DeepLinkAction::class.java,
      )

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this),
      ) {
        App(deepLinkAction = deepLinkAction)
      }
    }
  }

  companion object {
    const val EXTRA_DEEP_LINK_ACTION = "extra_deep_link_action"
  }
}
