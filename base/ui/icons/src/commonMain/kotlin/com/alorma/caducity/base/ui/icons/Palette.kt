package com.alorma.caducity.base.ui.icons
/*
ISC License

Copyright (c) for portions of Lucide are held by Cole Bemis 2013-2023 as part of Feather (MIT). All other copyright (c) for Lucide are held by Lucide Contributors 2025.

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

---

The MIT License (MIT) (for portions derived from Feather)

Copyright (c) 2013-2023 Cole Bemis

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Palette: ImageVector
  get() {
    if (_Palette != null) return _Palette!!

    _Palette = ImageVector.Builder(
      name = "palette",
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
        moveTo(12f, 22f)
        arcToRelative(1f, 1f, 0f, false, true, 0f, -20f)
        arcToRelative(10f, 9f, 0f, false, true, 10f, 9f)
        arcToRelative(5f, 5f, 0f, false, true, -5f, 5f)
        horizontalLineToRelative(-2.25f)
        arcToRelative(1.75f, 1.75f, 0f, false, false, -1.4f, 2.8f)
        lineToRelative(0.3f, 0.4f)
        arcToRelative(1.75f, 1.75f, 0f, false, true, -1.4f, 2.8f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(14f, 6.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 13.5f, 7f)
        arcTo(0.5f, 0.5f, 0f, false, true, 13f, 6.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 14f, 6.5f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(18f, 10.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 17.5f, 11f)
        arcTo(0.5f, 0.5f, 0f, false, true, 17f, 10.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 18f, 10.5f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(7f, 12.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 6.5f, 13f)
        arcTo(0.5f, 0.5f, 0f, false, true, 6f, 12.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 7f, 12.5f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
      ) {
        moveTo(9f, 7.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 8.5f, 8f)
        arcTo(0.5f, 0.5f, 0f, false, true, 8f, 7.5f)
        arcTo(0.5f, 0.5f, 0f, false, true, 9f, 7.5f)
        close()
      }
    }.build()

    return _Palette!!
  }

private var _Palette: ImageVector? = null

