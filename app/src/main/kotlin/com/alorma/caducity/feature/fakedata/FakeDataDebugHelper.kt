package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific helper for debugging fake data generation
 * Provides methods to manually trigger data generation for testing
 */
interface FakeDataDebugHelper {
  /**
   * Triggers fake data generation with specified parameters
   *
   * @param maxProducts Maximum total products (including existing)
   * @param variantsPerProduct Maximum variants per product
   * @param instancesPerVariantRange Min-max instances per variant
   * @return Flow of generation progress updates
   */
  fun generateFakeData(
    maxProducts: Int,
    variantsPerProduct: Int,
    instancesPerVariantRange: IntRange
  ): Flow<GenerationProgress>
}
