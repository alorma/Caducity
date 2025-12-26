package com.alorma.caducity

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
  // Mobile-sized window (typical phone portrait dimensions)
  val minHeight = 800
  val minWidth = 400

  val windowState = rememberWindowState(size = DpSize(minWidth.dp, minHeight.dp))

  Window(
    onCloseRequest = ::exitApplication,
    title = "Caducity",
    state = windowState
  ) {
    window.minimumSize = Dimension(minWidth, minHeight)

    App()
  }
}
