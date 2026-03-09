package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.feature.consent.ConsentManager
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

    val filterStatus: ItemStatus? = when (intent.getStringExtra(EXTRA_FILTER_STATUS)) {
      STATUS_EXPIRING_SOON -> ItemStatus.ExpiringSoon
      STATUS_EXPIRED -> ItemStatus.Expired
      else -> null
    }

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this)
      ) {
        App(initialFilterStatus = filterStatus)
      }
    }
  }

  companion object {
    const val EXTRA_FILTER_STATUS = "extra_filter_status"
    const val STATUS_EXPIRING_SOON = "expiring_soon"
    const val STATUS_EXPIRED = "expired"
  }
}
