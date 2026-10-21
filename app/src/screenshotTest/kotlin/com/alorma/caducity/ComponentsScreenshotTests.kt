package com.alorma.caducity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.caducity.ui.components.product.ProductRowFreezePreview
import com.alorma.caducity.ui.components.product.ProductRowPreview
import com.alorma.caducity.ui.components.product.ProductRowUnitTypesPreview
import com.alorma.caducity.ui.screen.add.UnitTypeSheetHandfulsPreview
import com.alorma.caducity.ui.screen.add.UnitTypeSheetMeasurablePreview
import com.alorma.caducity.ui.screen.add.UnitTypeSheetTuppersPreview
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

  @PreviewTest
  @Preview(name = "ProductRow freeze Light", locale = "ca", widthDp = 411, heightDp = 300)
  @Composable
  fun productRowFreezeLight() {
    ProductRowFreezePreview()
  }

  @PreviewTest
  @Preview(
    name = "ProductRow freeze Dark",
    locale = "ca",
    widthDp = 411,
    heightDp = 300,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
  )
  @Composable
  fun productRowFreezeDark() {
    ProductRowFreezePreview()
  }
}

class ProductRowUnitTypesScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = 411, heightDp = 440)
  @Preview(name = "Dark", locale = "ca", widthDp = 411, heightDp = 440, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun productRowUnitTypes() {
    ProductRowUnitTypesPreview()
  }
}

class UnitTypeSheetScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = 411, heightDp = 640)
  @Preview(name = "Dark", locale = "ca", widthDp = 411, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun unitTypeSheet() {
    UnitTypeSheetTuppersPreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = 411, heightDp = 640)
  @Preview(name = "Dark", locale = "ca", widthDp = 411, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun unitTypeSheetMeasurable() {
    UnitTypeSheetMeasurablePreview()
  }

  @PreviewTest
  @Preview(name = "Light", locale = "ca", widthDp = 411, heightDp = 640)
  @Preview(name = "Dark", locale = "ca", widthDp = 411, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun unitTypeSheetHandfuls() {
    UnitTypeSheetHandfulsPreview()
  }
}
