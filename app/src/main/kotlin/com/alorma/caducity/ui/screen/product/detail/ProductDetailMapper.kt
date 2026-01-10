package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.config.clock.AppClock
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

    val todayItems = DateInstancesUiModel(
      text = relativeTimeFormatter.format(appClock.nowDate()),
      status = InstanceStatus.ExpiringSoon,
      date = appClock.nowDate(),
      instances = productDetail.todayInstances.map { instance ->
        mapInstanceToUi(instance)
      }.toImmutableList(),
    ).takeIf { productDetail.todayInstances.isNotEmpty() }

    return ProductDetailState.Success(
      product = productUiModel,
      todayContent = todayItems,
    )
  }

  private fun mapInstanceToUi(instance: InstanceWithVariant): ProductInstanceDetailUiModel {
    val expirationDate = instance
      .instance
      .expirationDate
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    return ProductInstanceDetailUiModel(
      id = instance.instance.id,
      identifier = instance.instance.identifier,
      status = instance.instance.status,
      variantName = instance.variant?.name,
      expirationDate = expirationDate,
      expirationDateText = dateFormat.format(expirationDate),
    )
  }
}
