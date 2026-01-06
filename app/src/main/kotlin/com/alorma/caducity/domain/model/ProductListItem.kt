package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductListItem(
  val product: Product,
  val variants: ImmutableList<VariantWithInstances>,
  val standaloneInstances: ImmutableList<ProductInstance>,
)
