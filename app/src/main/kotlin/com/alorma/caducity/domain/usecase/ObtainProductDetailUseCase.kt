package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceWithVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.domain.model.ProductInstanceComparator
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

        val todayInstances = allInstances
          .filter {
            val expirationDate = it.expirationDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
            expirationDate == today
          }
          .map { instance ->
            InstanceWithVariant(
              instance = instance,
              variant = product.variants
                .map { it.variant }
                .firstOrNull { variant -> variant.id == instance.variantId },
            )
          }.toImmutableList()

        ProductDetail(
          product = product.product,
          todayInstances = todayInstances,
        )
      }
    }
  }

}
