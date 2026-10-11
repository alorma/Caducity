package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedExpandLess: ImageVector? = null

val Symbols.Outlined.ExpandLess: ImageVector
  get() =
    _OutlinedExpandLess ?: ImageVector
      .Builder(
        name = "Outlined.ExpandLess",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M480-529 324-373q-11 11-28 11t-28-11q-11-11-11-28t11-28l184-184q6-6 13-8.5t15-2.5q8 0 15 2.5t13 8.5l184 184q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-529Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedExpandLess = it }

private var _FilledExpandLess: ImageVector? = null

val Symbols.Filled.ExpandLess: ImageVector
  get() =
    _FilledExpandLess ?: ImageVector
      .Builder(
        name = "Filled.ExpandLess",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M480-529 324-373q-11 11-28 11t-28-11q-11-11-11-28t11-28l184-184q6-6 13-8.5t15-2.5q8 0 15 2.5t13 8.5l184 184q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-529Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledExpandLess = it }
