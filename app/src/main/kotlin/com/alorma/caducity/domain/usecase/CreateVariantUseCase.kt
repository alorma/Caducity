package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.domain.model.Variant

class CreateVariantUseCase(
  private val variantDataSource: VariantDataSource,
) {
  suspend operator fun invoke(productId: String, name: String): Result<Variant> {
    if (name.isBlank()) {
      return Result.failure(IllegalArgumentException("Variant name cannot be blank"))
    }
    return try {
      val variant = variantDataSource.createVariant(productId, name)
      Result.success(variant)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
