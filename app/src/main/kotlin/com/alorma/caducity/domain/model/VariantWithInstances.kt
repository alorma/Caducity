package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class VariantWithInstances(
  val variant: Variant,
  val instances: ImmutableList<ProductInstance>,
)
