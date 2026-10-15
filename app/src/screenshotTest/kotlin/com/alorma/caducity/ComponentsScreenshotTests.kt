package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.components.product.ProductRowPreview
import com.android.tools.screenshot.PreviewTest

class ComponentsScreenshotTests {
  @PreviewTest
  @Preview(name = "ProductRow Light", locale = "ca", widthDp = 411, heightDp = 380)
  @Composable
  fun productRowLight() {
    ProductRowPreview()
  }

  @PreviewTest
  @Preview(
    name = "ProductRow Dark",
    locale = "ca",
    widthDp = 411,
    heightDp = 380,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
  )
  @Composable
  fun productRowDark() {
    ProductRowPreview()
  }
}
