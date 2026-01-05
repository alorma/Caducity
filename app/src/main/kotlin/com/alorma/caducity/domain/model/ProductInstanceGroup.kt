package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductInstanceGroup(
  val identifier: String,
  val instances: ImmutableList<ProductInstance>,
)
