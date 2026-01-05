package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.screen.products.ProductInstanceStatusGroup
import kotlinx.collections.immutable.toImmutableList
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
        // Use displayDate for frozen items (pausedDate) or expirationDate for others
        val displayLocalDate = instance
          .displayDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        ProductInstanceDetailUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = instance.status,
          expirationDate = displayLocalDate,
          expirationDateText = dateFormat.format(displayLocalDate),
          // Keep expiration date for freeze/unfreeze calculations
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

    // Create groups from the domain groups
    val groups = productWithInstances.groups.map { group ->
      // Map instances for this group
      val groupInstances = group.instances.map { instance ->
        val displayLocalDate = instance
          .displayDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        ProductInstanceDetailUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = instance.status,
          expirationDate = displayLocalDate,
          expirationDateText = dateFormat.format(displayLocalDate),
          expirationInstant = instance.expirationDate,
        )
      }.toImmutableList()

      // Calculate status groups (excluding frozen for the bars)
      val frozenCount = groupInstances.count { it.status == InstanceStatus.Frozen }
      val statusGroups = groupInstances
        .filter { it.status != InstanceStatus.Frozen }
        .groupBy { it.status }
        .map { (status, statusInstances) ->
          ProductInstanceStatusGroup(
            status = status,
            count = statusInstances.size,
          )
        }
        .toImmutableList()

      // Keep ALL instances (including frozen) for display in the LazyRow
      ProductInstanceDetailGroup(
        identifier = group.identifier,
        statusGroups = statusGroups,
        frozenCount = frozenCount,
        instances = groupInstances, // All instances, frozen included
      )
    }.toImmutableList()

    return ProductDetailUiModel(
      id = productWithInstances.product.id,
      name = productWithInstances.product.name,
      description = productWithInstances.product.description,
      instances = sortedInstances,
      groups = groups,
    )
  }
}
