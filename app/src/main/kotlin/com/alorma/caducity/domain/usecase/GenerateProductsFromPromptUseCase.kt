package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.feature.fakedata.AIPromptDataSource
import com.alorma.caducity.feature.fakedata.ProductMatcher
import com.alorma.caducity.feature.fakedata.models.GeneratedProduct
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.feature.fakedata.models.MatchingResults
import com.alorma.caducity.feature.fakedata.models.toGeminiError
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case for generating products from user natural language prompt
 * Orchestrates: fetch existing products, generate with AI, match products, insert to DB
 */
class GenerateProductsFromPromptUseCase(
  private val AIPromptDataSource: AIPromptDataSource,
  private val productDataSource: ProductDataSource,
  private val variantDataSource: VariantDataSource,
  private val productMatcher: ProductMatcher,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  /**
   * Generates products from user prompt and returns a Flow of progress updates
   * If matches are found, returns AwaitingReview status for user confirmation
   */
  @OptIn(ExperimentalUuidApi::class)
  fun generate(userPrompt: String): Flow<GenerationProgress> = flow {
    try {
      // Step 1: Start
      emit(GenerationProgress.Started)

      // Step 2: Check existing products
      emit(GenerationProgress.CheckingExisting)
      val existingProducts = productDataSource
        .getProducts(ProductsListFilter.All)
        .firstOrNull()
        ?.map { it.product }
        ?: emptyList()

      // Step 3: Generate with AI
      emit(GenerationProgress.GeneratingWithAI)
      val generatedData = AIPromptDataSource.generateProductList(
        userPrompt = userPrompt,
        existingProducts = existingProducts
      ).getOrThrow()

      // Step 4: Match products
      emit(GenerationProgress.MatchingProducts)
      val matchingResults = productMatcher.matchProducts(
        generatedProducts = generatedData.products,
        existingProducts = existingProducts
      )

      // Step 5: If matches found, await user review
      if (matchingResults.shouldReview) {
        emit(GenerationProgress.AwaitingReview(matchingResults))
        return@flow
      }

      // Step 6: No matches - auto-create all products
      var productsCreated = 0
      var variantsCreated = 0
      var instancesCreated = 0
      val totalProducts = matchingResults.noMatches.size

      matchingResults.noMatches.forEachIndexed { index, noMatch ->
        emit(GenerationProgress.InsertingToDatabase(current = index + 1, total = totalProducts))

        val result = createProduct(noMatch.generatedProduct)
        productsCreated++
        variantsCreated += result.variantsCreated
        instancesCreated += result.instancesCreated
      }

      // Step 7: Complete
      emit(
        GenerationProgress.Completed(
          productsCreated = productsCreated,
          variantsCreated = variantsCreated,
          instancesCreated = instancesCreated
        )
      )
    } catch (e: Exception) {
      emit(GenerationProgress.Failed(e.toGeminiError()))
    }
  }

  /**
   * Confirms matches and creates/merges products
   * Call this after user reviews AwaitingReview state
   */
  @OptIn(ExperimentalUuidApi::class)
  fun confirmMatches(matchingResults: MatchingResults): Flow<GenerationProgress> = flow {
    try {
      var productsCreated = 0
      var variantsCreated = 0
      var instancesCreated = 0
      val totalItems = matchingResults.perfectMatches.size + matchingResults.noMatches.size

      var currentIndex = 0

      // Create new products from no-matches
      matchingResults.noMatches.forEach { noMatch ->
        emit(GenerationProgress.InsertingToDatabase(current = ++currentIndex, total = totalItems))
        val result = createProduct(noMatch.generatedProduct)
        productsCreated++
        variantsCreated += result.variantsCreated
        instancesCreated += result.instancesCreated
      }

      // Merge variants/instances for perfect matches
      matchingResults.perfectMatches.forEach { match ->
        emit(GenerationProgress.InsertingToDatabase(current = ++currentIndex, total = totalItems))
        val result = mergeIntoExistingProduct(match.existingProduct, match.generatedProduct)
        variantsCreated += result.variantsCreated
        instancesCreated += result.instancesCreated
      }

      emit(
        GenerationProgress.Completed(
          productsCreated = productsCreated,
          variantsCreated = variantsCreated,
          instancesCreated = instancesCreated
        )
      )
    } catch (e: Exception) {
      emit(GenerationProgress.Failed(e.toGeminiError()))
    }
  }

  /**
   * Merges generated variants/instances into an existing product
   */
  @OptIn(ExperimentalUuidApi::class)
  private suspend fun mergeIntoExistingProduct(
    existingProduct: Product,
    generatedProduct: GeneratedProduct
  ): CreationResult {
    val now = appClock.now()
    var variantsCreated = 0
    var instancesCreated = 0

    // Add variants with their instances
    generatedProduct.variants.forEach { generatedVariant ->
      val variant = variantDataSource.createVariant(
        productId = existingProduct.id,
        name = generatedVariant.name
      )
      variantsCreated++

      generatedVariant.instances.forEach { generatedInstance ->
        val expirationDate = now + generatedInstance.toDuration()
        productDataSource.addInstance(
          productId = existingProduct.id,
          instance = com.alorma.caducity.domain.model.NewProductInstance(
            identifier = generatedInstance.identifier,
            variantId = variant.id,
            expirationDate = expirationDate
          )
        )
        instancesCreated++
      }
    }

    // Add standalone instances
    generatedProduct.standaloneInstances.forEach { generatedInstance ->
      val expirationDate = now + generatedInstance.toDuration()
      productDataSource.addInstance(
        productId = existingProduct.id,
        instance = com.alorma.caducity.domain.model.NewProductInstance(
          identifier = generatedInstance.identifier,
          variantId = null,
          expirationDate = expirationDate
        )
      )
      instancesCreated++
    }

    return CreationResult(
      variantsCreated = variantsCreated,
      instancesCreated = instancesCreated
    )
  }

  @OptIn(ExperimentalUuidApi::class)
  private suspend fun createProduct(generatedProduct: GeneratedProduct): CreationResult {
    val productId = Uuid.random().toString()
    val now = appClock.now()

    val product = Product(
      id = productId,
      name = generatedProduct.name,
      description = generatedProduct.description
    )

    // Create the product FIRST (before variants and instances)
    productDataSource.createProduct(
      product,
      emptyList<com.alorma.caducity.domain.model.ProductInstance>().toImmutableList()
    )

    var variantsCreated = 0
    var instancesCreated = 0

    // Create variants with their instances
    generatedProduct.variants.forEach { generatedVariant ->
      val variant = variantDataSource.createVariant(
        productId = productId,
        name = generatedVariant.name
      )
      variantsCreated++

      // Add instances to this variant
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
    generatedProduct.standaloneInstances.forEach { generatedInstance ->
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

    return CreationResult(
      variantsCreated = variantsCreated,
      instancesCreated = instancesCreated
    )
  }

  private data class CreationResult(
    val variantsCreated: Int,
    val instancesCreated: Int
  )
}
