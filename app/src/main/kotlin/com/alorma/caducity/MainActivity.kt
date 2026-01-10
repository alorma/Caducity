package com.alorma.caducity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.ui.theme.AndroidSystemBarsAppearance
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      CompositionLocalProvider(
        LocalSystemBarsAppearance provides AndroidSystemBarsAppearance(this)
      ) {
        App()
      }
    }
  }
}
