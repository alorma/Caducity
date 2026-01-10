package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class ProductDetail(
  val product: Product,
  val todayContent: DatedInstances,
  val tomorrowContent: DatedInstances,
)

data class DatedInstances(
  val date: LocalDate,
  val instances: ImmutableList<InstanceWithVariant>,
)

data class InstanceWithVariant(
  val instance: ProductInstance,
  val variant: Variant?,
)