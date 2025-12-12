package com.alorma.caducity.domain.model

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance

data class ProductWithInstances(
  val product: Product,
  val instances: List<ProductInstance>,
)
