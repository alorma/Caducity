package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.DatedInstances
import com.alorma.caducity.domain.model.InstanceWithVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductInstanceComparator
import com.alorma.caducity.domain.model.ProductWithInstances
import com.kizitonwose.calendar.core.plusDays
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ObtainProductDetailUseCase(
  private val appClock: AppClock,
  private val productDataSource: ProductDataSource,
  private val instanceComparator: ProductInstanceComparator,
) {

  fun obtain(productId: String): Flow<Result<ProductDetail>> {
    return productDataSource.getProduct(productId).map { result ->
      result.map { product ->
        val today = appClock.nowDate()

        val allInstances = product.allInstances

        val todayContent = extractInstances(
          allInstances = allInstances,
          checkDate = today,
          product = product,
        )

        val tomorrowContent = extractInstances(
          allInstances = allInstances,
          checkDate = today.plusDays(1),
          product = product,
        )

        ProductDetail(
          product = product.product,
          todayContent = todayContent,
          tomorrowContent = tomorrowContent,
        )
      }
    }
  }

  private fun extractInstances(
    allInstances: ImmutableList<ProductInstance>,
    checkDate: LocalDate,
    product: ProductWithInstances,
  ): DatedInstances {
    val instances = allInstances
      .filter {
        val expirationDate = it.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
        expirationDate == checkDate
      }
      .map { instance ->
        InstanceWithVariant(
          instance = instance,
          variant = product.variants
            .map { it.variant }
            .firstOrNull { variant -> variant.id == instance.variantId },
        )
      }.toImmutableList()

    return DatedInstances(
      date = checkDate,
      instances = instances,
    )
  }

}
