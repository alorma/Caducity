package com.alorma.caducity.base.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons

val AppIcons.Outlined.Leaf: ImageVector
    get() {
        if (_Leaf != null) return _Leaf!!
        
        _Leaf = ImageVector.Builder(
            name = "leafy-green",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 22f)
                curveToRelative(1.25f, -0.987f, 2.27f, -1.975f, 3.9f, -2.2f)
                arcToRelative(5.56f, 5.56f, 0f, false, true, 3.8f, 1.5f)
                arcToRelative(4f, 4f, 0f, false, false, 6.187f, -2.353f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, 3.69f, -5.116f)
                arcTo(3.5f, 3.5f, 0f, false, false, 20.95f, 8f)
                arcTo(3.5f, 3.5f, 0f, true, false, 16f, 3.05f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, -5.831f, 1.373f)
                arcToRelative(3.5f, 3.5f, 0f, false, false, -5.116f, 3.69f)
                arcToRelative(4f, 4f, 0f, false, false, -2.348f, 6.155f)
                curveTo(3.499f, 15.42f, 4.409f, 16.712f, 4.2f, 18.1f)
                curveTo(3.926f, 19.743f, 3.014f, 20.732f, 2f, 22f)
            }
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 22f)
                lineTo(17f, 7f)
            }
        }.build()
        
        return _Leaf!!
    }

private var _Leaf: ImageVector? = null

