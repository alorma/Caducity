package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductDetail(
  val product: Product,
  val todayInstances: ImmutableList<InstanceWithVariant>,
)

data class InstanceWithVariant(
  val instance: ProductInstance,
  val variant: Variant?,
)