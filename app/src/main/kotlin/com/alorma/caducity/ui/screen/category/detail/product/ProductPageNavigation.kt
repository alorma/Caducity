package com.alorma.caducity.ui.screen.category.detail.product

sealed interface ProductPageNavigation {
  data object AddItem : ProductPageNavigation

  data object ProductDeleted : ProductPageNavigation
}
