package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.domain.usecase.GenerateFakeDataUseCase
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import kotlinx.coroutines.flow.Flow

/**
 * Android implementation of FakeDataDebugHelper
 * Uses GenerateFakeDataUseCase to trigger data generation
 */
class AndroidFakeDataDebugHelper(
  private val generateFakeDataUseCase: GenerateFakeDataUseCase
) : FakeDataDebugHelper {

  override fun generateFakeData(
    maxProducts: Int,
    variantsPerProduct: Int,
    instancesPerVariantRange: IntRange
  ): Flow<GenerationProgress> {
    return generateFakeDataUseCase.generate(
      maxProducts = maxProducts,
      variantsPerProduct = variantsPerProduct,
      instancesPerVariantRange = instancesPerVariantRange
    )
  }
}
