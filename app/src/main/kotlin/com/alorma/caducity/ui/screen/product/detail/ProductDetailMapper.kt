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

    // Map variants to UI models (empty or with instances)
    val variantTabs = productDetail.variants.map { variant ->
      if (variant.datedInstancesGroups.isEmpty()) {
        ProductDetailVariantTabUiModel.Empty(
          id = variant.id,
          name = variant.name,
        )
      } else {
        ProductDetailVariantTabUiModel.WithInstances(
          id = variant.id,
          name = variant.name,
          datedInstancesGroups = variant.datedInstancesGroups.map { datedInstances ->
            mapVariantDatedContent(
              variant = variant,
              datedInstances = datedInstances
            )
          }.toImmutableList(),
        )
      }
    }.toMutableList()

    // Add "Other" tab for non-variant instances if they exist
    if (productDetail.nonVariant.isNotEmpty()) {
      val otherTab = ProductDetailVariantTabUiModel.WithInstances(
        id = "other",
        name = "Other",
        datedInstancesGroups = listOf(
          DateInstancesUiModel(
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
          )
        ).toImmutableList(),
      )
      variantTabs.add(otherTab)
    }

    val today = appClock.nowDate()

    // Collect all dates from variants with instances for calendar
    val variantsWithInstances = productDetail.variants.filter { it.datedInstancesGroups.isNotEmpty() }
    val allDates = variantsWithInstances.flatMap { variant ->
      variant.datedInstancesGroups.map { it.date }
    }
    val startDate = allDates.minOrNull() ?: today
    val endDate = allDates.maxOrNull() ?: today

    // Create calendar config with all dated content from variants with instances
    val allDatedContents = variantsWithInstances.flatMap { variant ->
      variant.datedInstancesGroups.map { datedInstances ->
        mapVariantDatedContent(variant = variant, datedInstances = datedInstances)
      }
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
