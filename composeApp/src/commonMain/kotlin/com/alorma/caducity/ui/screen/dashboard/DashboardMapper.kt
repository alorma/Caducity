package com.alorma.caducity.ui.screen.dashboard

import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import com.alorma.caducity.domain.model.DashboardProducts
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardMapper {
  fun mapToDashboardSections(dashboardProducts: DashboardProducts): List<DashboardSection> {
    return listOf(
      DashboardSection(
        title = Res.string.dashboard_section_expired,
        itemCount = dashboardProducts.expired.sumOf { it.instances.size },
        products = dashboardProducts.expired.map { it.toUiModel() }
      ),
      DashboardSection(
        title = Res.string.dashboard_section_expiring_soon,
        itemCount = dashboardProducts.expiringSoon.sumOf { it.instances.size },
        products = dashboardProducts.expiringSoon.map { it.toUiModel() }
      ),
      DashboardSection(
        title = Res.string.dashboard_section_fresh,
        itemCount = dashboardProducts.fresh.sumOf { it.instances.size },
        products = dashboardProducts.fresh.map { it.toUiModel() }
      )
    )
  }

  private fun ProductWithInstances.toUiModel(): ProductUiModel {
    return ProductUiModel(
      id = product.id,
      name = product.name,
      description = product.description,
      instances = instances.map { instance ->
        val expirationDate = instance.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault())
        val purchaseDate = instance.purchaseDate.toLocalDateTime(TimeZone.currentSystemDefault())

        ProductInstanceUiModel(
          id = instance.id,
          productId = instance.productId,
          expirationDate = expirationDate.date.toString(),
          purchaseDate = purchaseDate.date.toString(),
        )
      }
    )
  }
}
