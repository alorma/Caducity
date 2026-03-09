package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
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

    val categoryId: String? = intent.getStringExtra(EXTRA_CATEGORY_ID)

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this)
      ) {
        App(initialCategoryId = categoryId)
      }
    }
  }

  companion object {
    const val EXTRA_CATEGORY_ID = "extra_category_id"
  }
}
