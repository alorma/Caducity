package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Variant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface VariantDataSource {

  fun getVariantsByProduct(productId: String): Flow<ImmutableList<Variant>>

  suspend fun getVariant(variantId: String): Variant?

  suspend fun createVariant(productId: String, name: String): Variant

  suspend fun deleteVariant(variantId: String): Result<Unit>

  suspend fun getActiveInstanceCount(variantId: String): Int
}
