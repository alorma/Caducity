package com.alorma.caducity.feature.fakedata.models

/**
 * Progress states for fake data generation
 * Used to provide real-time updates to the UI during the multi-step process
 */
sealed interface GenerationProgress {
  /**
   * Generation has started
   */
  data object Started : GenerationProgress

  /**
   * Checking what products already exist in the database
   */
  data object CheckingExisting : GenerationProgress

  /**
   * Calling AI to generate new data
   */
  data object GeneratingWithAI : GenerationProgress

  /**
   * Inserting generated data into the database
   * @param current Number of items inserted so far
   * @param total Total number of items to insert
   */
  data class InsertingToDatabase(
    val current: Int,
    val total: Int
  ) : GenerationProgress

  /**
   * Generation completed successfully
   * @param productsCreated Number of products created/enhanced
   * @param variantsCreated Number of variants created
   * @param instancesCreated Number of instances created
   */
  data class Completed(
    val productsCreated: Int,
    val variantsCreated: Int,
    val instancesCreated: Int
  ) : GenerationProgress

  /**
   * Generation failed with an error
   */
  data class Failed(val error: GeminiError) : GenerationProgress
}
