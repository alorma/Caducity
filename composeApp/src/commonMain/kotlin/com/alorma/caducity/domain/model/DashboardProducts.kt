package com.alorma.caducity.domain.model

import com.alorma.caducity.data.model.ProductInstance

data class DashboardProducts(
  val expired: List<ProductInstance>,
  val expiringSoon: List<ProductInstance>,
  val fresh: List<ProductInstance>,
)
