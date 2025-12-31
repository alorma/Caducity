package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductWithInstances(
  val product: Product,
  val instances: ImmutableList<ProductInstance>,
)
