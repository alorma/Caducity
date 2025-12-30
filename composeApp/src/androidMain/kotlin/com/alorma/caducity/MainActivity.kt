package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.base.ui.theme.AndroidSystemBarsAppearance
import com.alorma.caducity.base.ui.theme.LanguageManager
import com.alorma.caducity.base.ui.theme.LocalSystemBarsAppearance
import com.alorma.caducity.notification.ExpirationNotificationHelper
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
  private val languageManager: LanguageManager by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    // Check if we should show expiring products only (from notification tap)
    val showExpiringOnly = intent?.getBooleanExtra(
      ExpirationNotificationHelper.EXTRA_SHOW_EXPIRING_ONLY,
      false
    ) ?: false

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this)
      ) {
        App(showExpiringOnly = showExpiringOnly)
      }
    }
  }
}
