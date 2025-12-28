package com.alorma.caducity.ui.screen.dashboard.product

import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import com.alorma.caducity.ui.screen.dashboard.ProductInstanceUiModel
import com.alorma.caducity.ui.screen.dashboard.ProductUiModel
import kotlinx.collections.immutable.toImmutableList

val productWithInstancesPreview = ProductUiModel.WithInstances(
  id = "condimentum",
  name = "Carolina Jordan",
  description = "faucibus",
  today = "31 / 12 / 2025",
  instances = listOf(
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Expired,
      expirationDate = "31 / 12 / 2025",
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Expired,
      expirationDate = "31 / 12 / 2025",
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.ExpiringSoon,
      expirationDate = "31 / 12 / 2025",
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.ExpiringSoon,
      expirationDate = "31 / 12 / 2025",
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Fresh,
      expirationDate = "31 / 12 / 2025",
    ),
    ProductInstanceUiModel(
      id = "atomorum",
      identifier = "atomorum",
      status = InstanceStatus.Fresh,
      expirationDate = "31 / 12 / 2025",
    ),
  ).toImmutableList(),
)