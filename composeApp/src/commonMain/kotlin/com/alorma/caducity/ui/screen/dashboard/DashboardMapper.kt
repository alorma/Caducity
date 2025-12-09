package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.DashboardProducts
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardMapper {
  fun mapToDashboardSections(dashboardProducts: DashboardProducts): List<DashboardSection> {
    return listOf(
      DashboardSection(
        type = SectionType.EXPIRED,
        itemCount = dashboardProducts.expired.sumOf { it.instances.size },
        products = dashboardProducts.expired.map { it.toUiModel() }
      ),
      DashboardSection(
        type = SectionType.EXPIRING_SOON,
        itemCount = dashboardProducts.expiringSoon.sumOf { it.instances.size },
        products = dashboardProducts.expiringSoon.map { it.toUiModel() }
      ),
      DashboardSection(
        type = SectionType.FRESH,
        itemCount = dashboardProducts.fresh.sumOf { it.instances.size },
        products = dashboardProducts.fresh.map { it.toUiModel() }
      ),
      DashboardSection(
        type = SectionType.EMPTY,
        itemCount = dashboardProducts.empty.size,
        products = dashboardProducts.empty.map { it.toUiModel() }
      )
    )
  }

  private fun ProductWithInstances.getDateRange(): Pair<LocalDate, LocalDate> {
    val dates = instances.map { instance ->
      instance.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    return dates.min() to dates.max()
  }

  private fun ProductWithInstances.toUiModel(): ProductUiModel {
    val (startDate, endDate) = getDateRange()
    return ProductUiModel(
      id = product.id,
      name = product.name,
      description = product.description,
      startDate = startDate,
      endDate = endDate,
      instances = instances.map { instance ->
        val expirationDate =
          instance.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault())
        val purchaseDate = instance.purchaseDate.toLocalDateTime(TimeZone.currentSystemDefault())

        ProductInstanceUiModel(
          id = instance.id,
          productId = instance.productId,
          expirationDate = expirationDate.date.toString(),
          expirationDateInstant = instance.expirationDate,
          purchaseDate = purchaseDate.date.toString(),
          purchaseDateInstant = instance.purchaseDate,
        )
      }
    )
  }
}
