package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductListItem(
  val product: Product,
  val instances: ImmutableList<ProductInstance>,
)
