package com.alorma.caducity.ui.screen.product.detail

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.DetailVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.domain.model.VariantInstance
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

    // Map variants to UI models
    val variantTabs = productDetail.variants.map { variant ->
      ProductDetailVariantTabUiModel(
        id = variant.id,
        name = variant.name,
        datedInstances = mapVariantDatedContent(
          variant = variant,
          datedInstances = variant.datedInstances
        ),
      )
    }.toMutableList()

    // Add "Other" tab for non-variant instances if they exist
    if (productDetail.nonVariant.isNotEmpty()) {
      val otherTab = ProductDetailVariantTabUiModel(
        id = "other",
        name = "Other",
        datedInstances = DateInstancesUiModel(
          text = "",
          status = InstanceStatus.Fresh,
          date = appClock.nowDate(),
          instances = productDetail.nonVariant.map { instance ->
            ProductInstanceDetailUiModel(
              id = instance.id,
              expirationDate = appClock.nowDate(),
              status = InstanceStatus.Fresh,
              text = instance.name,
            )
          }.toImmutableList(),
        ),
      )
      variantTabs.add(otherTab)
    }

    val today = appClock.nowDate()

    // Collect all dates from variants for calendar
    val allDates = productDetail.variants.map { it.datedInstances.date }
    val startDate = allDates.minOrNull() ?: today
    val endDate = allDates.maxOrNull() ?: today

    // Create calendar config with all dated content from variants
    val allDatedContents = productDetail.variants.map { variant ->
      mapVariantDatedContent(variant = variant, datedInstances = variant.datedInstances)
    }.toImmutableList()

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
      variantTabs = variantTabs.toImmutableList(),
    )
  }

  private fun mapVariantDatedContent(
    variant: DetailVariant,
    datedInstances: com.alorma.caducity.domain.model.VariantDatedInstances,
  ): DateInstancesUiModel {
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
    )
  }

  private fun mapInstanceToUi(
    instance: VariantInstance,
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
