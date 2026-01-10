package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.DatedInstances
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.InstanceWithVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.ui.screen.products.RelativeTimeFormatter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime

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

    val todayContent = mapDatedContent(
      productDetail.todayContent,
    )

    val tomorrowContent = mapDatedContent(
      productDetail.tomorrowContent,
    )

    return ProductDetailState.Success(
      product = productUiModel,
      todayContent = todayContent,
      tomorrowContent = tomorrowContent,
    )
  }

  private fun mapDatedContent(
    datedInstances: DatedInstances,
  ): DateInstancesUiModel? {
    return DateInstancesUiModel(
      text = relativeTimeFormatter.format(datedInstances.date),
      status = InstanceStatus.ExpiringSoon,
      date = datedInstances.date,
      instances = datedInstances.instances.map { instance ->
        mapInstanceToUi(instance)
      }.toImmutableList(),
    ).takeIf { datedInstances.instances.isNotEmpty() }
  }

  private fun mapInstanceToUi(
    instance: InstanceWithVariant,
  ): ProductInstanceDetailUiModel {
    return ProductInstanceDetailUiModel(
      id = instance.instance.id,
      status = instance.instance.status,
      expirationDate = instance.instance
        .expirationDate
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date,
      text = instance.variant?.name ?: instance.instance.identifier
    )
  }
}
