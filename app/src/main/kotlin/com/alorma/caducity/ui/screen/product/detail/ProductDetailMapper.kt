package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.DatedInstances
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.InstanceWithVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.ui.screen.products.RelativeTimeFormatter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat

class ProductDetailMapper(
  private val appClock: AppClock,
  private val dateFormat: DateTimeFormat<LocalDate>,
  private val relativeTimeFormatter: RelativeTimeFormatter,
) {

  fun mapToProductDetail(
    productDetail: ProductDetail,
  ): ProductDetailState.Success {

    val productUiModel = ProductDetailUiModel(
      id = productDetail.product.id,
      name = productDetail.product.name,
      description = productDetail.product.description,
    )

    val allDatedContents = productDetail
      .datedContents
      .mapNotNull { datedContent -> mapDatedContent(datedInstances = datedContent) }
      .toImmutableList()

    return ProductDetailState.Success(
      product = productUiModel,
      content = allDatedContents,
    )
  }

  private fun mapDatedContent(
    datedInstances: DatedInstances,
  ): DateInstancesUiModel? {
    return DateInstancesUiModel(
      text = relativeTimeFormatter.format(datedInstances.date),
      status = datedInstances.status,
      date = datedInstances.date,
      instances = datedInstances.instances.map { instance ->
        mapInstanceToUi(
          instance = instance,
          status = datedInstances.status,
          expirationDate = datedInstances.date,
        )
      }.toImmutableList(),
    ).takeIf { datedInstances.instances.isNotEmpty() }
  }

  private fun mapInstanceToUi(
    instance: InstanceWithVariant,
    expirationDate: LocalDate,
    status: InstanceStatus,
  ): ProductInstanceDetailUiModel {
    return ProductInstanceDetailUiModel(
      id = instance.id,
      expirationDate = expirationDate,
      status = status,
      text = instance.name,
    )
  }
}
