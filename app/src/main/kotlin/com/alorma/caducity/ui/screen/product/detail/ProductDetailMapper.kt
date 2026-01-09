package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

class ProductDetailMapper(
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  fun mapToProductDetail(
    productWithInstances: ProductWithInstances,
  ): ProductDetailState.Success {

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

    // Map standalone instances (already sorted by use case)
    val standaloneInstances = productWithInstances.standaloneInstances
      .map { instance ->
        mapInstanceToUi(instance)
      }

    return ProductDetailState.Success(
      product = ProductDetailUiModel(
        id = productWithInstances.product.id,
        name = productWithInstances.product.name,
        description = productWithInstances.product.description,
      ),
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
