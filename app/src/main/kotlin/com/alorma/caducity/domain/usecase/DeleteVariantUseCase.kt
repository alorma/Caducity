package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.VariantDataSource

class DeleteVariantUseCase(
  private val variantDataSource: VariantDataSource,
) {
  suspend operator fun invoke(variantId: String): Result<Unit> {
    return variantDataSource.deleteVariant(variantId)
  }
}
