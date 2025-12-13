package com.alorma.caducity

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
  val minHeight = 500
  val minWidth = 600

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
