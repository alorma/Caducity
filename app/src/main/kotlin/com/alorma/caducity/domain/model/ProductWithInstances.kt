package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class ProductWithInstances(
  val product: Product,
  val variants: ImmutableList<ProductVariant>,
  val standaloneInstances: ImmutableList<ProductInstance>,
) {
  val allInstances: ImmutableList<ProductInstance> = buildList {
    addAll(variants.flatMap { it.instances })
    addAll(standaloneInstances)
  }.toImmutableList()
}

data class ProductVariant(
  val variant: Variant,
  val instances: ImmutableList<ProductInstance>,
)
