package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

data class ProductDetail(
  val product: Product,
  val variants: List<DetailVariant>,
  val nonVariant: List<VariantInstance>,
)

data class DetailVariant(
  val id: String,
  val name: String,
  val datedInstancesGroups: List<VariantDatedInstances>,
)

data class VariantDatedInstances(
  val date: LocalDate,
  val status: InstanceStatus,
  val instances: List<VariantInstance>,
)

data class VariantInstance(
  val id: String,
  val name: String,
)