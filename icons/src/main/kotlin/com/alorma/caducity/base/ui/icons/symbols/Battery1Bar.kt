package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedBattery1Bar: ImageVector? = null

val Symbols.Outlined.Battery1Bar: ImageVector
  get() =
    _OutlinedBattery1Bar ?: ImageVector
      .Builder(
        name = "Outlined.Battery1Bar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M320-80q-17 0-28.5-11.5T280-120v-640q0-17 11.5-28.5T320-800h80v-40q0-17 11.5-28.5T440-880h80q17 0 28.5 11.5T560-840v40h80q17 0 28.5 11.5T680-760v640q0 17-11.5 28.5T640-80H320Zm40-160h240v-480H360v480Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedBattery1Bar = it }

private var _FilledBattery1Bar: ImageVector? = null

val Symbols.Filled.Battery1Bar: ImageVector
  get() =
    _FilledBattery1Bar ?: ImageVector
      .Builder(
        name = "Filled.Battery1Bar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M320-80q-17 0-28.5-11.5T280-120v-640q0-17 11.5-28.5T320-800h80v-40q0-17 11.5-28.5T440-880h80q17 0 28.5 11.5T560-840v40h80q17 0 28.5 11.5T680-760v640q0 17-11.5 28.5T640-80H320Zm40-160h240v-480H360v480Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledBattery1Bar = it }
