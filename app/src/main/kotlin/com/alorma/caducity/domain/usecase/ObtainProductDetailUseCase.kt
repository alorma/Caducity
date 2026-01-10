package com.alorma.caducity.domain.usecase

import android.util.Log
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.DatedInstances
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.InstanceWithVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.Variant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class ObtainProductDetailUseCase(
  private val appClock: AppClock,
  private val productDataSource: ProductDataSource,
  private val expirationThresholds: ExpirationThresholds,
) {

  fun obtain(productId: String): Flow<Result<ProductDetail>> {
    return productDataSource.getProduct(productId).map { result ->
      result.map { product ->
        val today = appClock.nowDate()

        val allInstances = product.allInstances
        val dates = allInstances
          .map { it.expirationDate.date() }
          .distinct()

        val datedInstances = dates.map { date ->
          extractInstances(
            allInstances = allInstances,
            checkDate = date,
            variants = product.variants.map { it.variant },
          )
        }.filter { dated -> dated.instances.isNotEmpty() }

        ProductDetail(
          product = product.product,
          datedContents = datedInstances.toImmutableList(),
        )
      }
    }
  }

  private fun extractInstances(
    allInstances: ImmutableList<ProductInstance>,
    checkDate: LocalDate,
    variants: List<Variant>,
  ): DatedInstances {
    Log.i("Alorma", "Date: $checkDate")
    val instances = allInstances
      .filter {
        val expirationDate = it.expirationDate.date()
        expirationDate == checkDate
      }
      .map { instance ->
        val variant = variants
          .firstOrNull { variant -> variant.id == instance.variantId }

        val name = listOfNotNull(
          variant?.name,
          instance.identifier.takeIf { it.isNotEmpty() }
        ).joinToString(" - ")

        InstanceWithVariant(
          id = instance.id,
          name = name,
        )
      }.toImmutableList()

    return DatedInstances(
      date = checkDate,
      instances = instances,
      status = instanceStatus(checkDate),
    )
  }

  private fun instanceStatus(
    expirationDate: LocalDate,
  ): InstanceStatus {
    return InstanceStatus.calculateStatus(
      expirationDate = expirationDate.atStartOfDayIn(
        TimeZone.currentSystemDefault()
      ),
      now = appClock.now(),
      soonExpiringThreshold = expirationThresholds.soonExpiringThreshold,
    )
  }

}
