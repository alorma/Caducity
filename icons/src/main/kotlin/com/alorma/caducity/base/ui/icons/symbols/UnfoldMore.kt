package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedUnfoldMore: ImageVector? = null

val Symbols.Outlined.UnfoldMore: ImageVector
  get() =
    _OutlinedUnfoldMore ?: ImageVector
      .Builder(
        name = "Outlined.UnfoldMore",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "m480-236 93-93q12-12 29-12t29 12q12 12 12 29t-12 29L508-148q-6 6-13 8.5t-15 2.5q-8 0-15-2.5t-13-8.5L329-271q-12-12-12-29t12-29q12-12 29-12t29 12l93 93Zm0-484-93 93q-12 12-29 12t-29-12q-12-12-12-29t12-29l123-123q6-6 13-8.5t15-2.5q8 0 15 2.5t13 8.5l123 123q12 12 12 29t-12 29q-12 12-29 12t-29-12l-93-93Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedUnfoldMore = it }

private var _FilledUnfoldMore: ImageVector? = null

val Symbols.Filled.UnfoldMore: ImageVector
  get() =
    _FilledUnfoldMore ?: ImageVector
      .Builder(
        name = "Filled.UnfoldMore",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "m480-236 93-93q12-12 29-12t29 12q12 12 12 29t-12 29L508-148q-6 6-13 8.5t-15 2.5q-8 0-15-2.5t-13-8.5L329-271q-12-12-12-29t12-29q12-12 29-12t29 12l93 93Zm0-484-93 93q-12 12-29 12t-29-12q-12-12-12-29t12-29l123-123q6-6 13-8.5t15-2.5q8 0 15 2.5t13 8.5l123 123q12 12 12 29t-12 29q-12 12-29 12t-29-12l-93-93Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledUnfoldMore = it }
