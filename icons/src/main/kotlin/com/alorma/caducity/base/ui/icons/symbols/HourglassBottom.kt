package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedHourglassBottom: ImageVector? = null

val Symbols.Outlined.HourglassBottom: ImageVector
  get() =
    _OutlinedHourglassBottom ?: ImageVector
      .Builder(
        name = "Outlined.HourglassBottom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M480-520q66 0 113-47t47-113v-120H320v120q0 66 47 113t113 47ZM200-80q-17 0-28.5-11.5T160-120q0-17 11.5-28.5T200-160h40v-120q0-61 28.5-114.5T348-480q-51-32-79.5-85.5T240-680v-120h-40q-17 0-28.5-11.5T160-840q0-17 11.5-28.5T200-880h560q17 0 28.5 11.5T800-840q0 17-11.5 28.5T760-800h-40v120q0 61-28.5 114.5T612-480q51 32 79.5 85.5T720-280v120h40q17 0 28.5 11.5T800-120q0 17-11.5 28.5T760-80H200Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedHourglassBottom = it }

private var _FilledHourglassBottom: ImageVector? = null

val Symbols.Filled.HourglassBottom: ImageVector
  get() =
    _FilledHourglassBottom ?: ImageVector
      .Builder(
        name = "Filled.HourglassBottom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M480-520q66 0 113-47t47-113v-120H320v120q0 66 47 113t113 47ZM200-80q-17 0-28.5-11.5T160-120q0-17 11.5-28.5T200-160h40v-120q0-61 28.5-114.5T348-480q-51-32-79.5-85.5T240-680v-120h-40q-17 0-28.5-11.5T160-840q0-17 11.5-28.5T200-880h560q17 0 28.5 11.5T800-840q0 17-11.5 28.5T760-800h-40v120q0 61-28.5 114.5T612-480q51 32 79.5 85.5T720-280v120h40q17 0 28.5 11.5T800-120q0 17-11.5 28.5T760-80H200Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledHourglassBottom = it }
