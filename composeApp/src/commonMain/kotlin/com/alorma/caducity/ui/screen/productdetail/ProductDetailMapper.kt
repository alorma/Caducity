package com.alorma.caducity.ui.screen.productdetail

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ProductDetailMapper(
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {
  fun mapToProductDetail(productWithInstances: ProductWithInstances): ProductDetailUiModel {
    val now = appClock.now()

    val today = now
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    val expiringSoonDate = now
      .plus(expirationThresholds.soonExpiringThreshold)
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    val instances = productWithInstances.instances.map { instance ->
      val expirationDate = instance
        .expirationDate
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      ProductInstanceDetailUiModel(
        id = instance.id,
        identifier = instance.identifier,
        status = when {
          expirationDate < today -> InstanceStatus.Expired
          expirationDate in today..<expiringSoonDate -> InstanceStatus.ExpiringSoon
          else -> InstanceStatus.Fresh
        },
        expirationDate = expirationDate,
      )
    }

    // Sort instances: expired > expiring soon > fresh
    val sortedInstances = instances.sortedWith(
      compareBy<ProductInstanceDetailUiModel> {
        when (it.status) {
          InstanceStatus.Expired -> 0
          InstanceStatus.ExpiringSoon -> 1
          InstanceStatus.Fresh -> 2
        }
      }.thenBy { it.expirationDate }
    )

    return ProductDetailUiModel(
      id = productWithInstances.product.id,
      name = productWithInstances.product.name,
      description = productWithInstances.product.description,
      instances = sortedInstances,
    )
  }
}
