package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ProductWithItems(
  val product: Product,
  val items: ImmutableList<Item>,
)
