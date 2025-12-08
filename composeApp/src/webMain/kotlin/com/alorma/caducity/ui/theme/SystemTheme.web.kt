package com.alorma.caducity.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.coroutines.awaitCancellation

@Composable
actual fun isSystemInDarkTheme(): Boolean {
  var isDark by remember { 
    mutableStateOf(
      window.matchMedia("(prefers-color-scheme: dark)").matches
    )
  }
  
  LaunchedEffect(Unit) {
    val mediaQuery = window.matchMedia("(prefers-color-scheme: dark)")
    val listener: (dynamic) -> Unit = { event ->
      isDark = event.matches as Boolean
    }
    mediaQuery.addEventListener("change", listener)
    
    // Cleanup: Remove listener when composable is disposed
    try {
      awaitCancellation()
    } finally {
      mediaQuery.removeEventListener("change", listener)
    }
  }
  
  return isDark
}
