package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.feature.fakedata.AIPromptDataSource
import com.alorma.caducity.feature.fakedata.models.GeneratedProductVariants
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.feature.fakedata.models.GeminiError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import kotlin.uuid.ExperimentalUuidApi

/**
 * Use case for generating variants and instances for an existing product
 * Orchestrates: generate with AI, insert variants/instances to DB
 * Simplified version of GenerateProductsFromPromptUseCase (no product matching needed)
 */
class GenerateVariantsForProductUseCase(
  private val aiPromptDataSource: AIPromptDataSource,
  private val productDataSource: ProductDataSource,
  private val variantDataSource: VariantDataSource,
  private val appClock: AppClock,
) {

  /**
   * Generates variants and instances for an existing product from user prompt
   *
   * @param productId ID of the existing product
   * @param productName Name of the existing product (for AI context)
   * @param userPrompt User's description of variants/instances to add
   * @return Flow of progress updates
   */
  fun generate(
    productId: String,
    productName: String,
    userPrompt: String
  ): Flow<GenerationProgress> = flow {
    try {
      // Step 1: Start
      emit(GenerationProgress.Started)

      // Step 2: Generate with AI
      emit(GenerationProgress.GeneratingWithAI)
      val generatedVariants = aiPromptDataSource.generateVariantsForProduct(
        userPrompt = userPrompt,
        productName = productName
      ).getOrThrow()

      // Step 3: Check if anything was generated
      if (generatedVariants.variants.isEmpty() && generatedVariants.standaloneInstances.isEmpty()) {
        emit(
          GenerationProgress.Completed(
            productsCreated = 0,
            variantsCreated = 0,
            instancesCreated = 0
          )
        )
        return@flow
      }

      // Step 4: Await user review
      // We create a simplified AwaitingReview state by wrapping in MatchingResults
      // This allows us to reuse the review sheet UI pattern
      emit(GenerationProgress.AwaitingReview(
        com.alorma.caducity.feature.fakedata.models.MatchingResults(
          perfectMatches = emptyList(),
          noMatches = emptyList()
        )
      ))
    } catch (e: Exception) {
      Timber.e(e, "generate failed: ${e.message}")
      emit(GenerationProgress.Failed(GeminiError.UnknownError(e)))
    }
  }

  /**
   * Confirms and inserts the generated variants/instances into the database
   * Call this after user reviews and confirms
   *
   * @param productId ID of the existing product
   * @param generatedVariants The variants and instances to insert
   * @return Flow of progress updates
   */
  @OptIn(ExperimentalUuidApi::class)
  fun confirmAndInsert(
    productId: String,
    generatedVariants: GeneratedProductVariants
  ): Flow<GenerationProgress> = flow {
    try {
      val now = appClock.now()
      var variantsCreated = 0
      var instancesCreated = 0

      val totalItems = generatedVariants.variants.size + generatedVariants.standaloneInstances.size
      var currentIndex = 0

      // Create variants with their instances
      generatedVariants.variants.forEach { generatedVariant ->
        emit(GenerationProgress.InsertingToDatabase(current = ++currentIndex, total = totalItems))

        val variant = variantDataSource.createVariant(
          productId = productId,
          name = generatedVariant.name
        )
        variantsCreated++

        generatedVariant.instances.forEach { generatedInstance ->
          val expirationDate = now + generatedInstance.toDuration()
          productDataSource.addInstance(
            productId = productId,
            instance = com.alorma.caducity.domain.model.NewProductInstance(
              identifier = generatedInstance.identifier,
              variantId = variant.id,
              expirationDate = expirationDate
            )
          )
          instancesCreated++
        }
      }

      // Add standalone instances (without variant)
      if (generatedVariants.standaloneInstances.isNotEmpty()) {
        emit(GenerationProgress.InsertingToDatabase(current = ++currentIndex, total = totalItems))

        generatedVariants.standaloneInstances.forEach { generatedInstance ->
          val expirationDate = now + generatedInstance.toDuration()
          productDataSource.addInstance(
            productId = productId,
            instance = com.alorma.caducity.domain.model.NewProductInstance(
              identifier = generatedInstance.identifier,
              variantId = null, // Standalone
              expirationDate = expirationDate
            )
          )
          instancesCreated++
        }
      }

      emit(
        GenerationProgress.Completed(
          productsCreated = 0, // No products created, only variants/instances
          variantsCreated = variantsCreated,
          instancesCreated = instancesCreated
        )
      )
    } catch (e: Exception) {
      Timber.e(e, "confirmAndInsert failed: ${e.message}")
      emit(GenerationProgress.Failed(GeminiError.UnknownError(e)))
    }
  }
}
