package com.alorma.caducity.domain.model

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance
import kotlinx.collections.immutable.ImmutableList

data class ProductWithInstances(
  val product: Product,
  val instances: ImmutableList<ProductInstance>,
)
