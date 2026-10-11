package com.alorma.caducity.feature.deeplink

import com.alorma.caducity.ui.navigation.Category
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DeepLinkParsingTest {
  @Test
  fun `OpenCategory maps to the category route`() {
    expectThat(DeepLinkAction.OpenCategory("c1").toRoute()).isEqualTo(Category("c1"))
  }

  @Test
  fun `OpenProduct from an old notification keeps the product`() {
    expectThat(DeepLinkAction.OpenProduct("c1", "p1").toRoute())
      .isEqualTo(Category("c1", initialProductId = "p1"))
  }

  @Test
  fun `OpenProduct without product opens the category`() {
    expectThat(DeepLinkAction.OpenProduct("c1").toRoute()).isEqualTo(Category("c1"))
  }
}
