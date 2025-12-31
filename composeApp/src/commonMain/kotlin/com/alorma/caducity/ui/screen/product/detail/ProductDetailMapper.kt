package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.base.main.InstanceStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class ProductDetailMapper(
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  fun mapToProductDetail(productWithInstances: ProductWithInstances): ProductDetailUiModel {

    val instances = productWithInstances.instances
      // Consumed instances are already filtered at data source level
      .map { instance ->
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
          expirationInstant = instance.expirationDate,
        )
      }

    // Sort instances: expired > expiring soon > fresh > frozen
    val sortedInstances = instances.sortedWith(
      compareBy<ProductInstanceDetailUiModel> {
        when (it.status) {
          InstanceStatus.Expired -> 0
          InstanceStatus.ExpiringSoon -> 1
          InstanceStatus.Fresh -> 2
          InstanceStatus.Frozen -> 3
          InstanceStatus.Consumed -> 4 // Should never happen due to filter
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
