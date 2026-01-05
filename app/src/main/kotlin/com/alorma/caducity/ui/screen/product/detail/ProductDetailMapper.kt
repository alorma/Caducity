package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
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

    // Map variants with their instance counts
    val variants = productWithInstances.variants.map { variantWithInstances ->
      ProductDetailVariantUiModel(
        id = variantWithInstances.variant.id,
        name = variantWithInstances.variant.name,
        instances = variantWithInstances
          .instances
          .map { instance -> mapInstanceToUi(instance) },
      )
    }

    // Map standalone instances
    val standaloneInstances = productWithInstances.standaloneInstances
      .map { instance ->
        mapInstanceToUi(instance)
      }
      .sortedWith(
        compareBy<ProductInstanceDetailUiModel> {
          when (it.status) {
            InstanceStatus.Expired -> 0
            InstanceStatus.ExpiringSoon -> 1
            InstanceStatus.Fresh -> 2
            InstanceStatus.Frozen -> 3
            InstanceStatus.Consumed -> 4
          }
        }.thenBy { it.expirationDate }
      )

    val groups = productWithInstances
      .variants
      .map { variantWithInstances ->
        val groupInstances = variantWithInstances
          .instances
          .map { instance -> mapInstanceToUi(instance) }
          .toImmutableList()

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
          identifier = variantWithInstances.variant.name,
          statusGroups = statusGroups,
          frozenCount = frozenCount,
          instances = groupInstances, // All instances, frozen included
        )
      }.toImmutableList()

    return ProductDetailUiModel(
      id = productWithInstances.product.id,
      name = productWithInstances.product.name,
      description = productWithInstances.product.description,
      variants = variants,
      standaloneInstances = standaloneInstances,
    )
  }

  private fun mapInstanceToUi(instance: ProductInstance): ProductInstanceDetailUiModel {
    val displayLocalDate = instance
      .displayDate
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    return ProductInstanceDetailUiModel(
      id = instance.id,
      identifier = instance.identifier,
      status = instance.status,
      expirationDate = displayLocalDate,
      expirationDateText = dateFormat.format(displayLocalDate),
      expirationInstant = instance.expirationDate,
    )
  }
}
