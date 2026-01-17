package com.alorma.caducity.domain.usecase

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.VariantDataSource
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.feature.fakedata.FakeDataGenerator
import com.alorma.caducity.feature.fakedata.models.GeneratedProduct
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.feature.fakedata.models.toGeminiError
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case for generating products from user natural language prompt
 * Orchestrates: fetch existing products, generate with AI, insert to DB
 */
class GenerateProductsFromPromptUseCase(
  private val fakeDataGenerator: FakeDataGenerator,
  private val productDataSource: ProductDataSource,
  private val variantDataSource: VariantDataSource,
  private val appClock: AppClock,
  private val expirationThresholds: ExpirationThresholds,
) {

  /**
   * Generates products from user prompt and returns a Flow of progress updates
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
      val generatedData = fakeDataGenerator.generateFromUserPrompt(
        userPrompt = userPrompt,
        existingProducts = existingProducts
      ).getOrThrow()

      // Step 4: Insert to database
      var productsCreated = 0
      var variantsCreated = 0
      var instancesCreated = 0
      val totalProducts = generatedData.products.size

      generatedData.products.forEachIndexed { index, generatedProduct ->
        emit(GenerationProgress.InsertingToDatabase(current = index + 1, total = totalProducts))

        val result = createProduct(generatedProduct)
        productsCreated++
        variantsCreated += result.variantsCreated
        instancesCreated += result.instancesCreated
      }

      // Step 5: Complete
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
    for (generatedVariant in generatedProduct.variants) {
      val variant = variantDataSource.createVariant(
        productId = productId,
        name = generatedVariant.name
      )
      variantsCreated++

      // Add instances to this variant
      for (generatedInstance in generatedVariant.instances) {
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
    for (generatedInstance in generatedProduct.standaloneInstances) {
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
