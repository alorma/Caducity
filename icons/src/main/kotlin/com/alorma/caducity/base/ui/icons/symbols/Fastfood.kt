package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedFastfood: ImageVector? = null

val Symbols.Outlined.Fastfood: ImageVector
  get() =
    _OutlinedFastfood ?: ImageVector
      .Builder(
        name = "Outlined.Fastfood",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M533-440q-32-45-84.5-62.5T340-520q-56 0-108.5 17.5T147-440h386ZM340-600q46 0 93.5 11.5t88 35.5q40.5 24 69.5 60t39 85q4 19-8 33.5T591-360H89q-19 0-31-14.5T50-408q10-49 39.5-85t70-60q40.5-24 87.5-35.5t93-11.5ZM80-200q-17 0-28.5-11.5T40-240q0-17 11.5-28.5T80-280h520q17 0 28.5 11.5T640-240q0 17-11.5 28.5T600-200H80ZM788-40h-28q-17 0-28.5-11.5T720-80q0-17 11.5-28.5T760-120h16l56-560H488q-17 0-28.5-11.5T448-720q0-17 11.5-28.5T488-760h152v-120q0-17 11.5-28.5T680-920q17 0 28.5 11.5T720-880v120h156q18 0 30 13t10 31L854-98q-3 25-22 41.5T788-40Zm-68-80h56-56ZM80-40q-17 0-28.5-11.5T40-80q0-17 11.5-28.5T80-120h520q17 0 28.5 11.5T640-80q0 17-11.5 28.5T600-40H80Zm260-400Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedFastfood = it }

private var _FilledFastfood: ImageVector? = null

val Symbols.Filled.Fastfood: ImageVector
  get() =
    _FilledFastfood ?: ImageVector
      .Builder(
        name = "Filled.Fastfood",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M340-600q46 0 93.5 11.5t88 35.5q40.5 24 69.5 60t39 85q4 19-8 33.5T591-360H89q-19 0-31-14.5T50-408q10-49 39.5-85t70-60q40.5-24 87.5-35.5t93-11.5ZM80-200q-17 0-28.5-11.5T40-240q0-17 11.5-28.5T80-280h520q17 0 28.5 11.5T640-240q0 17-11.5 28.5T600-200H80Zm0 160q-17 0-28.5-11.5T40-80q0-17 11.5-28.5T80-120h520q17 0 28.5 11.5T640-80q0 17-11.5 28.5T600-40H80Zm732 0h-52q-17 0-28.5-11.5T720-80v-280q0-108-70.5-188.5T475-661q-11-3-18-11.5t-9-19.5l-3-23q-2-18 10-31.5t30-13.5h155v-120q0-17 11.5-28.5T680-920q17 0 28.5 11.5T720-880v120h156q18 0 30 13t10 31L852-76q-2 15-13 25.5T812-40Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledFastfood = it }
