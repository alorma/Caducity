package com.alorma.caducity.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Home: ImageVector
        get() {
            if (_home != null) {
                return _home!!
            }
            _home = ImageVector.Builder(
                name = "Home",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(10f, 20f)
                    verticalLineTo(14f)
                    horizontalLineTo(14f)
                    verticalLineTo(20f)
                    horizontalLineTo(19f)
                    verticalLineTo(12f)
                    horizontalLineTo(22f)
                    lineTo(12f, 3f)
                    lineTo(2f, 12f)
                    horizontalLineTo(5f)
                    verticalLineTo(20f)
                    horizontalLineTo(10f)
                    close()
                }
            }.build()
            return _home!!
        }
    
    private var _home: ImageVector? = null

    val Settings: ImageVector
        get() {
            if (_settings != null) {
                return _settings!!
            }
            _settings = ImageVector.Builder(
                name = "Settings",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(19.14f, 12.94f)
                    curveTo(19.18f, 12.64f, 19.2f, 12.33f, 19.2f, 12f)
                    curveTo(19.2f, 11.68f, 19.18f, 11.36f, 19.13f, 11.06f)
                    lineTo(21.16f, 9.48f)
                    curveTo(21.34f, 9.34f, 21.39f, 9.07f, 21.28f, 8.87f)
                    lineTo(19.36f, 5.55f)
                    curveTo(19.24f, 5.33f, 18.99f, 5.26f, 18.77f, 5.33f)
                    lineTo(16.38f, 6.29f)
                    curveTo(15.88f, 5.91f, 15.35f, 5.59f, 14.76f, 5.35f)
                    lineTo(14.4f, 2.81f)
                    curveTo(14.36f, 2.57f, 14.16f, 2.4f, 13.92f, 2.4f)
                    horizontalLineTo(10.08f)
                    curveTo(9.84f, 2.4f, 9.65f, 2.57f, 9.61f, 2.81f)
                    lineTo(9.25f, 5.35f)
                    curveTo(8.66f, 5.59f, 8.12f, 5.92f, 7.63f, 6.29f)
                    lineTo(5.24f, 5.33f)
                    curveTo(5.02f, 5.25f, 4.77f, 5.33f, 4.65f, 5.55f)
                    lineTo(2.74f, 8.87f)
                    curveTo(2.62f, 9.08f, 2.66f, 9.34f, 2.86f, 9.48f)
                    lineTo(4.89f, 11.06f)
                    curveTo(4.84f, 11.36f, 4.8f, 11.69f, 4.8f, 12f)
                    curveTo(4.8f, 12.31f, 4.82f, 12.64f, 4.87f, 12.94f)
                    lineTo(2.84f, 14.52f)
                    curveTo(2.66f, 14.66f, 2.61f, 14.93f, 2.72f, 15.13f)
                    lineTo(4.64f, 18.45f)
                    curveTo(4.76f, 18.67f, 5.01f, 18.74f, 5.23f, 18.67f)
                    lineTo(7.62f, 17.71f)
                    curveTo(8.12f, 18.09f, 8.65f, 18.41f, 9.24f, 18.65f)
                    lineTo(9.6f, 21.19f)
                    curveTo(9.65f, 21.43f, 9.84f, 21.6f, 10.08f, 21.6f)
                    horizontalLineTo(13.92f)
                    curveTo(14.16f, 21.6f, 14.36f, 21.43f, 14.39f, 21.19f)
                    lineTo(14.75f, 18.65f)
                    curveTo(15.34f, 18.41f, 15.88f, 18.09f, 16.37f, 17.71f)
                    lineTo(18.76f, 18.67f)
                    curveTo(18.98f, 18.75f, 19.23f, 18.67f, 19.35f, 18.45f)
                    lineTo(21.26f, 15.13f)
                    curveTo(21.38f, 14.91f, 21.34f, 14.66f, 21.14f, 14.52f)
                    lineTo(19.14f, 12.94f)
                    close()
                    moveTo(12f, 15.6f)
                    curveTo(10.02f, 15.6f, 8.4f, 13.98f, 8.4f, 12f)
                    curveTo(8.4f, 10.02f, 10.02f, 8.4f, 12f, 8.4f)
                    curveTo(13.98f, 8.4f, 15.6f, 10.02f, 15.6f, 12f)
                    curveTo(15.6f, 13.98f, 13.98f, 15.6f, 12f, 15.6f)
                    close()
                }
            }.build()
            return _settings!!
        }
    
    private var _settings: ImageVector? = null

    val Add: ImageVector
        get() {
            if (_add != null) {
                return _add!!
            }
            _add = ImageVector.Builder(
                name = "Add",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(19f, 13f)
                    horizontalLineTo(13f)
                    verticalLineTo(19f)
                    horizontalLineTo(11f)
                    verticalLineTo(13f)
                    horizontalLineTo(5f)
                    verticalLineTo(11f)
                    horizontalLineTo(11f)
                    verticalLineTo(5f)
                    horizontalLineTo(13f)
                    verticalLineTo(11f)
                    horizontalLineTo(19f)
                    verticalLineTo(13f)
                    close()
                }
            }.build()
            return _add!!
        }
    
    private var _add: ImageVector? = null
}
