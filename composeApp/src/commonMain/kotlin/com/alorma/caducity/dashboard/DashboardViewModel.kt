package com.alorma.caducity.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.data.datasource.ProductDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
  productDataSource: ProductDataSource
) : ViewModel() {

  val state: StateFlow<DashboardState> = productDataSource
    .getAllProducts()
    .map { products ->
      val totalItems = products.size
      val activeItems = products.count { !it.isExpired }
      val expiredItems = products.count { it.isExpired }

      DashboardState.Success(
        totalItems = totalItems,
        activeItems = activeItems,
        expiredItems = expiredItems,
        products = products
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = DashboardState.Loading
    )
}
