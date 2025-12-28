package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class ProductDetailMapper(
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  fun mapToProductDetail(productWithInstances: ProductWithInstances): ProductDetailUiModel {

    val instances = productWithInstances.instances.map { instance ->
      val expirationLocalDate = instance
        .expirationDate
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

      ProductInstanceDetailUiModel(
        id = instance.id,
        identifier = instance.identifier,
        status = instance.status,
        expirationDate = instance.expirationDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date,
        expirationDateText = dateFormat.format(expirationLocalDate),
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
