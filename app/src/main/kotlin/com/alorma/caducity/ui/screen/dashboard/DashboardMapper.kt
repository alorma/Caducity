package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.DayOfWeek

class DashboardMapper(
  private val appCalendarConfigMapper: AppCalendarConfigMapper,
) {

  fun mapToPerProductState(
    products: ImmutableList<ProductWithInstances>,
    firstDayOfWeek: DayOfWeek,
  ): DashboardState.Success {
    val mapped = products.map { product ->

      val instances = buildList {
        addAll(product.variants.flatMap { it.instances })
        addAll(product.standaloneInstances)
      }

      ProductCalendarState(
        id = product.product.id,
        name = product.product.name,
        appCalendarConfig = appCalendarConfigMapper.createFromInstances(instances, firstDayOfWeek)
      )
    }

    val allInstances = products.flatMap { product ->
      product.variants.flatMap { variant -> variant.instances } + product.standaloneInstances
    }

    val summary = calculateSummary(allInstances)

    return DashboardState.Success.PerProduct(
      summary = summary,
      products = mapped,
    )
  }

  private fun calculateSummary(instances: List<ProductInstance>): DashboardSummary {
    val expiredCount = getStatusCount(instances, InstanceStatus.Expired)
    val expiringSoonCount = getStatusCount(instances, InstanceStatus.ExpiringSoon)
    val freshCount = getStatusCount(instances, InstanceStatus.Fresh)
    val frozenCount = getStatusCount(instances, InstanceStatus.Frozen)

    return DashboardSummary(
      expired = expiredCount,
      expiringSoon = expiringSoonCount,
      fresh = freshCount,
      frozen = frozenCount,
    )
  }

  private fun getStatusCount(
    instances: List<ProductInstance>,
    status: InstanceStatus,
  ): Int {
    return instances
      .filter { instance -> instance.status == status }
      .size
  }
}