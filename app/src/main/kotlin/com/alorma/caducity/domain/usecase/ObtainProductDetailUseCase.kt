package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.DetailVariant
import com.alorma.caducity.domain.model.ProductDetail
import com.alorma.caducity.domain.model.VariantDatedInstances
import com.alorma.caducity.domain.model.VariantInstance
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
        // Filter out frozen instances
        val activeVariants = product.variants.map { variant ->
          variant.copy(
            instances = variant.instances
              .filter { it.status != InstanceStatus.Frozen }
              .toImmutableList()
          )
        }.filter { it.instances.isNotEmpty() }

        val activeStandaloneInstances = product.standaloneInstances
          .filter { it.status != InstanceStatus.Frozen }

        // Map variants to DetailVariant with their dated instances
        val detailVariants: List<DetailVariant> = activeVariants.map { variant ->
          val dates: List<LocalDate> = variant.instances
            .map { it.expirationDate.date() }
            .distinct()
            .sorted()

          // Group instances by date and create VariantDatedInstances
          val datedInstancesList: List<VariantDatedInstances> = dates.map { date ->
            val instancesForDate: List<VariantInstance> = variant.instances
              .filter { it.expirationDate.date() == date }
              .map { instance ->
                val name = listOfNotNull(
                  instance.identifier.takeIf { it.isNotEmpty() }
                ).joinToString(" - ")
                VariantInstance(
                  id = instance.id,
                  name = name,
                )
              }

            VariantDatedInstances(
              date = date,
              status = instanceStatus(date),
              instances = instancesForDate,
            )
          }

          // Use the earliest date as the representative date for this variant
          val earliestDatedInstances: VariantDatedInstances = datedInstancesList.minByOrNull { it.date }
            ?: VariantDatedInstances(
              date = appClock.nowDate(),
              status = InstanceStatus.Fresh,
              instances = emptyList(),
            )

          DetailVariant(
            id = variant.variant.id,
            name = variant.variant.name,
            datedInstances = earliestDatedInstances,
          )
        }

        // Map standalone instances (non-variant instances)
        val nonVariantInstances: List<VariantInstance> = activeStandaloneInstances.map { instance ->
          val name = listOfNotNull(
            instance.identifier.takeIf { it.isNotEmpty() }
          ).joinToString(" - ")
          VariantInstance(
            id = instance.id,
            name = name,
          )
        }

        ProductDetail(
          product = product.product,
          variants = detailVariants,
          nonVariant = nonVariantInstances,
        )
      }
    }
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
