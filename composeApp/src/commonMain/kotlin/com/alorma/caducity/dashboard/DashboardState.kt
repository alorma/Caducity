package com.alorma.caducity.dashboard

import com.alorma.caducity.data.model.Product

sealed class DashboardState {
  data object Loading : DashboardState()

  data class Success(
    val totalItems: Int,
    val activeItems: Int,
    val expiredItems: Int,
    val products: List<Product>
  ) : DashboardState()
}
