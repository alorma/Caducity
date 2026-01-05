package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import com.alorma.caducity.domain.model.ProductInstanceGroup
import com.alorma.caducity.domain.model.ProductWithInstances
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainProductDetailUseCase(
  private val productDataSource: ProductDataSource,
) {

  fun obtainProductDetail(productId: String): Flow<Result<ProductWithInstances>> {
    return productDataSource.getProduct(productId)
      .map { result ->
        result.map { productWithInstances ->
          // Group instances by identifier (same logic as ObtainDashboardProductsUseCase)
          val groups = productWithInstances.instances
            .groupBy { it.identifier }
            .map { (identifier, instancesInGroup) ->
              ProductInstanceGroup(
                identifier = identifier,
                instances = instancesInGroup
                  .sortedWith(instanceComparator)
                  .toImmutableList()
              )
            }
            .sortedWith(groupComparator)
            .toImmutableList()

          productWithInstances.copy(groups = groups)
        }
      }
  }

  companion object {
    // Sort instances by status priority, then by expiration date
    private val instanceComparator = compareBy<ProductInstance>(
      { instance ->
        when (instance.status) {
          InstanceStatus.Expired -> 0
          InstanceStatus.ExpiringSoon -> 1
          InstanceStatus.Fresh -> 2
          InstanceStatus.Frozen -> 3
          InstanceStatus.Consumed -> 4
        }
      },
      { instance -> instance.expirationDate }
    )

    // Sort groups by most urgent status in the group, then by earliest expiration date
    private val groupComparator = compareBy<ProductInstanceGroup>(
      { group ->
        // Find the most urgent status in the group
        group.instances.minOfOrNull { instance ->
          when (instance.status) {
            InstanceStatus.Expired -> 0
            InstanceStatus.ExpiringSoon -> 1
            InstanceStatus.Fresh -> 2
            InstanceStatus.Frozen -> 3
            InstanceStatus.Consumed -> 4
          }
        } ?: Int.MAX_VALUE
      },
      { group ->
        // Find the earliest expiration date in the group
        group.instances.minOfOrNull { instance -> instance.expirationDate }
      }
    )
  }
}
