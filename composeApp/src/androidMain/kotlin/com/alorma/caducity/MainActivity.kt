package com.alorma.caducity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alorma.caducity.notification.ExpirationNotificationHelper

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()

    // Check if we should show expiring products only (from notification tap)
    val showExpiringOnly = intent?.getBooleanExtra(
      ExpirationNotificationHelper.EXTRA_SHOW_EXPIRING_ONLY,
      false
    ) ?: false

    setContent {
      App(showExpiringOnly = showExpiringOnly)
    }
  }
}
