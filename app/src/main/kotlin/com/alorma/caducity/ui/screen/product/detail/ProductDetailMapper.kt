package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.DatedInstances
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.InstanceWithVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import com.alorma.caducity.config.time.RelativeTimeFormatter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class ProductDetailMapper(
  private val appClock: AppClock,
  private val relativeTimeFormatter: RelativeTimeFormatter,
  private val appCalendarConfigMapper: AppCalendarConfigMapper,
) {

  fun mapToProductDetail(
    productDetail: ProductDetail,
    firstDayOfWeek: DayOfWeek,
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

    val today = appClock.nowDate()

    val startDate = allDatedContents.minOfOrNull { it.date } ?: today
    val endDate = allDatedContents.maxOfOrNull { it.date } ?: today

    val appCalendarConfig = appCalendarConfigMapper.createWithDatedContent(
      startDate = startDate,
      endDate = endDate,
      datedContent = allDatedContents,
      firstDayOfWeek = firstDayOfWeek,
    )

    return ProductDetailState.Success(
      today = today,
      product = productUiModel,
      appCalendarConfig = appCalendarConfig,
      datedContent = allDatedContents,
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
