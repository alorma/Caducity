package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedRamenDining: ImageVector? = null

val Symbols.Outlined.RamenDining: ImageVector
  get() =
    _OutlinedRamenDining ?: ImageVector
      .Builder(
        name = "Outlined.RamenDining",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M320-150q-98-38-169-112.5T80-440q0-17 11.5-28.5T120-480h40v-284q0-15 10-26t25-13l652-73q14-2 23.5 7t9.5 23q0 11-8 19.5t-19 9.5l-433 49v68h430q13 0 21.5 8.5T880-670q0 13-8.5 21.5T850-640H420v160h420q17 0 28.5 11.5T880-440q0 103-71 177.5T640-150v30q0 17-11.5 28.5T600-80H360q-17 0-28.5-11.5T320-120v-30Zm0-550h40v-61l-40 4v57Zm-100 0h40v-50l-40 4v46Zm100 220h40v-160h-40v160Zm-100 0h40v-160h-40v160Zm180 320h160v-44l50-20q61-24 108-69t68-107H174q21 62 68 107.5T350-224l50 20v44Zm80-240Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedRamenDining = it }

private var _FilledRamenDining: ImageVector? = null

val Symbols.Filled.RamenDining: ImageVector
  get() =
    _FilledRamenDining ?: ImageVector
      .Builder(
        name = "Filled.RamenDining",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M320-150q-98-38-169-112.5T80-440q0-17 11.5-28.5T120-480h40v-284q0-15 10-26t25-13l652-73q14-2 23.5 7t9.5 23q0 11-8 19.5t-19 9.5l-433 49v68h430q13 0 21.5 8.5T880-670q0 13-8.5 21.5T850-640H420v160h420q17 0 28.5 11.5T880-440q0 103-71 177.5T640-150v30q0 17-11.5 28.5T600-80H360q-17 0-28.5-11.5T320-120v-30Zm0-550h40v-61l-40 4v57Zm-100 0h40v-50l-40 4v46Zm100 220h40v-160h-40v160Zm-100 0h40v-160h-40v160Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledRamenDining = it }
