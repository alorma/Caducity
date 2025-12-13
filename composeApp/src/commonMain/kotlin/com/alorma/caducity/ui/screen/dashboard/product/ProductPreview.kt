package com.alorma.caducity.ui.screen.dashboard.product

import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import com.kizitonwose.calendar.core.now
import kotlinx.datetime.LocalDate

val productWithInstancesPreview = ProductUiModel.WithInstances(
  id = "condimentum",
  name = "Carolina Jordan",
  description = "faucibus",
  today = LocalDate.now(),
  instances = listOf(
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Expired,
      expirationDate = LocalDate.now(),
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Expired,
      expirationDate = LocalDate.now(),
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.ExpiringSoon,
      expirationDate = LocalDate.now(),
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.ExpiringSoon,
      expirationDate = LocalDate.now(),
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Fresh,
      expirationDate = LocalDate.now(),
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Fresh,
      expirationDate = LocalDate.now(),
    ),
  ),
)