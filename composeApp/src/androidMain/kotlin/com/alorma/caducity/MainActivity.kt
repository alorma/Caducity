package com.alorma.caducity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alorma.caducity.settings.initializeSettings

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initializeSettings(this)
    enableEdgeToEdge()

    setContent {
      App()
    }
  }
}
