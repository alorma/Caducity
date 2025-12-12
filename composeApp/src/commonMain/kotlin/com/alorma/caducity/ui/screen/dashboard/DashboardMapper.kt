package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.DashboardProducts
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardMapper(
  private val appClock: AppClock,
) {
  fun mapToDashboardSections(dashboardProducts: DashboardProducts): List<DashboardSection> {
    return listOf(
      DashboardSection(
        type = SectionType.EXPIRED,
        itemCount = dashboardProducts.expired.size,
        products = dashboardProducts.expired.map { it.toUiModel() }
      ),
      DashboardSection(
        type = SectionType.EXPIRING_SOON,
        itemCount = dashboardProducts.expiringSoon.size,
        products = dashboardProducts.expiringSoon.map { it.toUiModel() }
      ),
      DashboardSection(
        type = SectionType.FRESH,
        itemCount = dashboardProducts.fresh.size,
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
    if (instances.isEmpty()) {
      return ProductUiModel.Empty(
        id = product.id,
        name = product.name,
        description = product.description,
      )
    }

    val (startDate, endDate) = getDateRange()
    val today = appClock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return ProductUiModel.WithInstances(
      id = product.id,
      name = product.name,
      description = product.description,
      startDate = startDate,
      endDate = endDate,
      today = today,
      instances = instances.map { instance ->
        ProductInstanceUiModel(
          id = instance.id,
          expirationDate = instance
            .expirationDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date,
        )
      }
    )
  }
}
