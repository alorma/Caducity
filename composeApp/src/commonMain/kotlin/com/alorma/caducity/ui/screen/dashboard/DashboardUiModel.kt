package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.data.model.ProductInstance
import org.jetbrains.compose.resources.StringResource

data class DashboardSection(
  val title: StringResource,
  val itemCount: Int,
  val products: List<ProductInstance>,
)
