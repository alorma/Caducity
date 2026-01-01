package com.alorma.caducity.ui.screen.dashboard.components

import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.alorma.caducity.ui.screen.products.ProductsListInstanceUiModel
import com.alorma.caducity.ui.screen.products.ProductsListUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

val productWithInstancesPreview: ProductUiModel.WithInstances
  get() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val inThreeDays = today.plus(3, DateTimeUnit.DAY)
    val inOneWeek = today.plus(7, DateTimeUnit.DAY)
    val inTwoWeeks = today.plus(14, DateTimeUnit.DAY)

    return ProductUiModel.WithInstances(
      id = "preview-product-1",
      name = "Organic Milk",
      description = "Fresh organic whole milk",
      today = today.toString(),
      instances = listOf(
        ProductInstanceUiModel(
          id = "instance-0",
          identifier = "Bottle #0",
          status = InstanceStatus.Expired,
          expirationDate = today,
          expirationDateText = today.toString(),
        ),
        ProductInstanceUiModel(
          id = "instance-1",
          identifier = "Bottle #1",
          status = InstanceStatus.Expired,
          expirationDate = yesterday,
          expirationDateText = yesterday.toString(),
        ),
        ProductInstanceUiModel(
          id = "instance-2",
          identifier = "Bottle #2",
          status = InstanceStatus.Expired,
          expirationDate = yesterday,
          expirationDateText = yesterday.toString(),
        ),
        ProductInstanceUiModel(
          id = "instance-3",
          identifier = "Bottle #3",
          status = InstanceStatus.ExpiringSoon,
          expirationDate = tomorrow,
          expirationDateText = tomorrow.toString(),
        ),
        ProductInstanceUiModel(
          id = "instance-4",
          identifier = "Bottle #4",
          status = InstanceStatus.ExpiringSoon,
          expirationDate = inThreeDays,
          expirationDateText = inThreeDays.toString(),
        ),
        ProductInstanceUiModel(
          id = "instance-5",
          identifier = "Bottle #5",
          status = InstanceStatus.Fresh,
          expirationDate = inOneWeek,
          expirationDateText = inOneWeek.toString(),
        ),
        ProductInstanceUiModel(
          id = "instance-6",
          identifier = "Bottle #6",
          status = InstanceStatus.Fresh,
          expirationDate = inTwoWeeks,
          expirationDateText = inTwoWeeks.toString(),
        ),
      ).toImmutableList(),
    )
  }

val productListWithInstancesPreview: ProductsListUiModel.WithInstances
  get() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val inThreeDays = today.plus(3, DateTimeUnit.DAY)
    val inOneWeek = today.plus(7, DateTimeUnit.DAY)
    val inTwoWeeks = today.plus(14, DateTimeUnit.DAY)

    return ProductsListUiModel.WithInstances(
      id = "preview-product-1",
      name = "Organic Milk",
      description = "Fresh organic whole milk",
      instances = listOf(
        ProductsListInstanceUiModel(
          id = "instance-0",
          identifier = "Bottle #0",
          status = InstanceStatus.Expired,
          expirationDate = today,
          expirationDateText = today.toString(),
        ),
        ProductsListInstanceUiModel(
          id = "instance-1",
          identifier = "Bottle #1",
          status = InstanceStatus.Expired,
          expirationDate = yesterday,
          expirationDateText = yesterday.toString(),
        ),
        ProductsListInstanceUiModel(
          id = "instance-2",
          identifier = "Bottle #2",
          status = InstanceStatus.Expired,
          expirationDate = yesterday,
          expirationDateText = yesterday.toString(),
        ),
        ProductsListInstanceUiModel(
          id = "instance-3",
          identifier = "Bottle #3",
          status = InstanceStatus.ExpiringSoon,
          expirationDate = tomorrow,
          expirationDateText = tomorrow.toString(),
        ),
        ProductsListInstanceUiModel(
          id = "instance-4",
          identifier = "Bottle #4",
          status = InstanceStatus.ExpiringSoon,
          expirationDate = inThreeDays,
          expirationDateText = inThreeDays.toString(),
        ),
        ProductsListInstanceUiModel(
          id = "instance-5",
          identifier = "Bottle #5",
          status = InstanceStatus.Fresh,
          expirationDate = inOneWeek,
          expirationDateText = inOneWeek.toString(),
        ),
        ProductsListInstanceUiModel(
          id = "instance-6",
          identifier = "Bottle #6",
          status = InstanceStatus.Fresh,
          expirationDate = inTwoWeeks,
          expirationDateText = inTwoWeeks.toString(),
        ),
      ).toImmutableList(),
    )
  }