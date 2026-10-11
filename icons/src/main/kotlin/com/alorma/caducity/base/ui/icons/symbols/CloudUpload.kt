package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedCloudUpload: ImageVector? = null

val Symbols.Outlined.CloudUpload: ImageVector
  get() =
    _OutlinedCloudUpload ?: ImageVector
      .Builder(
        name = "Outlined.CloudUpload",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "m440-446-36 35q-11 11-27.5 11T348-412q-11-11-11-28t11-28l104-104q12-12 28-12t28 12l104 104q11 11 11.5 27.5T612-412q-11 11-27.5 11.5T556-411l-36-35v206h220q42 0 71-29t29-71q0-42-29-71t-71-29h-60v-80q0-83-58.5-141.5T480-720q-83 0-141.5 58.5T280-520h-20q-58 0-99 41t-41 99q0 58 41 99t99 41h60q17 0 28.5 11.5T360-200q0 17-11.5 28.5T320-160h-60q-91 0-155.5-63T40-377q0-78 47-139t123-78q25-92 100-149t170-57q117 0 198.5 81.5T760-520q69 8 114.5 59.5T920-340q0 75-52.5 127.5T740-160H520q-33 0-56.5-23.5T440-240v-206Zm40 6Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedCloudUpload = it }

private var _FilledCloudUpload: ImageVector? = null

val Symbols.Filled.CloudUpload: ImageVector
  get() =
    _FilledCloudUpload ?: ImageVector
      .Builder(
        name = "Filled.CloudUpload",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M260-160q-91 0-155.5-63T40-377q0-78 47-139t123-78q25-92 100-149t170-57q117 0 198.5 81.5T760-520q69 8 114.5 59.5T920-340q0 75-52.5 127.5T740-160H520v-286l36 35q11 11 27.5 11t28.5-12q11-11 11-28t-11-28L508-572q-12-12-28-12t-28 12L348-468q-11 11-11.5 27.5T348-412q11 11 27.5 11.5T404-411l36-35v286H260Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledCloudUpload = it }
