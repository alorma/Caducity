package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductWithInstances(
  val product: Product,
  val variants: ImmutableList<ProductVariant>,
  val standaloneInstances: ImmutableList<ProductInstance>,
)

data class ProductVariant(
  val variant: Variant,
  val instances: ImmutableList<ProductInstance>,
)
