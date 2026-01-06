package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.domain.model.Variant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class GetProductVariantsUseCase(
  private val variantDataSource: VariantDataSource,
) {
  operator fun invoke(productId: String): Flow<ImmutableList<Variant>> {
    return variantDataSource.getVariantsByProduct(productId)
  }
}
