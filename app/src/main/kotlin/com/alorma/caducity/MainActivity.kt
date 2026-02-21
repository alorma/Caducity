package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.feature.consent.ConsentManager
import com.alorma.caducity.feature.notification.AndroidExpirationNotificationHelper
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

    val initialStatus = intent?.getStatusExtra()

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this)
      ) {
        App(initialStatus = initialStatus)
      }
    }
  }
}

private fun android.content.Intent.getStatusExtra(): ItemStatus? {
  return when (getStringExtra(AndroidExpirationNotificationHelper.EXTRA_NOTIFICATION_STATUS)) {
    AndroidExpirationNotificationHelper.STATUS_EXPIRED -> ItemStatus.Expired
    AndroidExpirationNotificationHelper.STATUS_EXPIRING_SOON -> ItemStatus.ExpiringSoon
    else -> null
  }
}
