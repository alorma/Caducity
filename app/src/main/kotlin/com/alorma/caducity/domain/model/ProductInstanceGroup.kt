package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductInstanceGroup(
  val identifier: String,
  val variantId: String? = null,
  val variantName: String? = null,
  val isStandalone: Boolean = false,
  val instances: ImmutableList<ProductInstance>,
)
