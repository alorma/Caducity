package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class ProductDetail(
  val product: Product,
  val datedContents: List<DatedInstances>,
)

data class DatedInstances(
  val date: LocalDate,
  val status: InstanceStatus,
  val instances: ImmutableList<InstanceWithVariant>,
)

data class InstanceWithVariant(
  val id: String,
  val name: String,
)