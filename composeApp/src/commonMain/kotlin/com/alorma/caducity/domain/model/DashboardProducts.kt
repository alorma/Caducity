package com.alorma.caducity.domain.model

import com.alorma.caducity.data.model.Product
import com.alorma.caducity.data.model.ProductInstance

data class DashboardProducts(
  val expired: List<ProductWithInstances>,
  val expiringSoon: List<ProductWithInstances>,
  val fresh: List<ProductWithInstances>,
)

data class ProductWithInstances(
  val product: Product,
  val instances: List<ProductInstance>,
)
