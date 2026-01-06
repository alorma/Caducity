package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.InstanceDataSource
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductInstance
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObtainDashboardProductsUseCase(
  private val instanceDataSource: InstanceDataSource,
) {

  fun obtainProducts(): Flow<ImmutableList<ProductInstance>> {
    return instanceDataSource.getAllInstances()
      .map { instances ->
        instances
          .filterNot { instance -> instance.status == InstanceStatus.Consumed }
          .sortedBy { instance -> instance.expirationDate }
          .toImmutableList()
      }
  }
}
