package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.alorma.caducity.ui.screen.category.detail.product.ProductPageState
import com.alorma.caducity.ui.screen.category.detail.product.ProductTabContentPreview
import com.alorma.caducity.ui.screen.category.detail.product.ProductTabContentPreviewProvider
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.android.tools.screenshot.PreviewTest

class ProductTabContentPreviewTest {

  @PreviewTest
  @PreviewDynamicLightDark
  @Composable
  fun ProductTabContentPreviewTest(
    @PreviewParameter(provider = ProductTabContentPreviewProvider::class)
    state: ProductPageState,
  ) {
    ProductTabContentPreview(
      state = state,
    )
  }
}