package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.feature.fakedata.models.GeneratedGroceryData
import com.alorma.caducity.feature.fakedata.models.GeneratedProductVariants

/**
 * Interface for generating fake grocery product data
 * Implementations can use AI (Gemini), mock data, or other sources
 */
interface AIPromptDataSource {
  /**
   * Generates realistic grocery products with variants and instances
   *
   * @param existingProducts Current products in the database (for smart mixing)
   * @param maxProducts Maximum number of total products to have after generation
   * @param variantsPerProduct Maximum variants to generate per product
   * @param instancesPerVariantRange Range of instances to generate per variant (min..max)
   * @return Result containing generated data or error
   */
  suspend fun generateFakeData(
    existingProducts: List<Product>,
    maxProducts: Int,
    variantsPerProduct: Int,
    instancesPerVariantRange: IntRange
  ): Result<GeneratedGroceryData>

  /**
   * Generates grocery products from user natural language prompt
   *
   * @param userPrompt User's description of purchased groceries
   * @param existingProducts Current products in the database (for context)
   * @return Result containing generated data or error
   */
  suspend fun generateFromUserPrompt(
    userPrompt: String,
    existingProducts: List<Product>
  ): Result<GeneratedGroceryData>

  /**
   * Generates variants and instances for an existing product from user prompt
   *
   * @param userPrompt User's description of variants/instances to add
   * @param productName Name of the existing product (for context)
   * @return Result containing generated variants and instances or error
   */
  suspend fun generateVariantsForProduct(
    userPrompt: String,
    productName: String
  ): Result<GeneratedProductVariants>
}
