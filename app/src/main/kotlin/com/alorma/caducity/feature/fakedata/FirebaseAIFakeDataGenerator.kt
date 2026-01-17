package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.feature.fakedata.models.GeneratedGroceryData
import com.alorma.caducity.feature.fakedata.models.GeneratedInstance
import com.alorma.caducity.feature.fakedata.models.GeneratedProduct
import com.alorma.caducity.feature.fakedata.models.GeneratedVariant
import com.alorma.caducity.feature.fakedata.models.toGeminiError
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.RequestOptions
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FirebaseAIFakeDataGenerator : FakeDataGenerator {

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  override suspend fun generateGroceryData(
    existingProducts: List<Product>,
    maxProducts: Int,
    variantsPerProduct: Int,
    instancesPerVariantRange: IntRange
  ): Result<GeneratedGroceryData> = withContext(Dispatchers.IO) {
    try {
      val productsToGenerate = maxOf(0, maxProducts - existingProducts.size)
      if (productsToGenerate <= 0) {
        // No new products needed, return empty
        return@withContext Result.success(GeneratedGroceryData(products = emptyList()))
      }

      val prompt = buildPrompt(
        productsToGenerate = productsToGenerate,
        variantsPerProduct = variantsPerProduct,
        instancesPerVariantRange = instancesPerVariantRange
      )


      val generativeModel = Firebase.ai(
        backend = GenerativeBackend.googleAI()
      ).generativeModel(
        modelName = "gemini-2.5-flash",
        generationConfig = generationConfig {
          temperature = 0.9f
          topK = 40
          topP = 0.95f
          responseMimeType = "application/json"
        },
      )

      val response = generativeModel.generateContent(
        prompt = Content.Builder()
          .text(prompt)
          .build()
      )

      val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini")

      val parsed = parseResponse(responseText)
      Result.success(parsed)
    } catch (e: Exception) {
      Result.failure(e.toGeminiError() as Throwable)
    }
  }

  @OptIn(PublicPreviewAPI::class)
  override suspend fun generateFromUserPrompt(
    userPrompt: String,
    existingProducts: List<Product>
  ): Result<GeneratedGroceryData> = withContext(Dispatchers.IO) {
    try {
      // Input validation
      if (userPrompt.isBlank()) {
        return@withContext Result.success(GeneratedGroceryData(products = emptyList()))
      }


      val generativeModel = Firebase.ai(
        backend = GenerativeBackend.googleAI()
      ).templateGenerativeModel(
        requestOptions = RequestOptions(),
      )

      val response = generativeModel.generateContent(
        templateId = "user-template",
        mapOf("input" to userPrompt)
      )

      val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini")

      val parsed = parseResponse(responseText)

      // Validate output
      val validatedProducts = parsed.products.filter { product ->
        product.name.length <= 100 &&
        product.description.length <= 200 &&
        product.variants.all { variant ->
          variant.instances.all { instance ->
            instance.daysFromNow in -30..365
          }
        } &&
        product.standaloneInstances.all { instance ->
          instance.daysFromNow in -30..365
        }
      }

      Result.success(GeneratedGroceryData(products = validatedProducts))
    } catch (e: Exception) {
      Result.failure(e.toGeminiError() as Throwable)
    }
  }

  private fun buildPrompt(
    productsToGenerate: Int,
    variantsPerProduct: Int,
    instancesPerVariantRange: IntRange
  ): String {
    return """
Generate realistic grocery product data for a food expiration tracker app. Create $productsToGenerate common grocery products.

For each product:
- Generate ${variantsPerProduct} variants (different sizes, brands, or types)
- Each variant should have ${instancesPerVariantRange.first}-${instancesPerVariantRange.last} instances
- Each instance needs an identifier (lot number/batch code) and days until expiration
- 20% of instances should be EXPIRED (daysFromNow: -1 to -30)
- 30% should be EXPIRING SOON (daysFromNow: 1 to 7)
- 50% should be FRESH (daysFromNow: 8 to 90)
- Also generate 1-2 standalone instances per product (without variant)

Product examples: milk, eggs, bread, yogurt, cheese, chicken, vegetables, fruits, etc.
Variant examples: "Whole Milk 1L", "Skim Milk 500ml", "Organic Eggs 12ct", "White Bread 500g"
Identifier examples: "LOT-A123", "BATCH-2024-01-15", "EXP-456789"

Return JSON matching this exact structure:
{
  "products": [
    {
      "name": "Milk",
      "description": "Fresh dairy milk",
      "variants": [
        {
          "name": "Whole Milk 1L",
          "instances": [
            {"identifier": "LOT-A123", "daysFromNow": -5},
            {"identifier": "LOT-A124", "daysFromNow": 2},
            {"identifier": "LOT-A125", "daysFromNow": 15}
          ]
        }
      ],
      "standaloneInstances": [
        {"identifier": "BATCH-X1", "daysFromNow": 10}
      ]
    }
  ]
}

Generate realistic, varied data. Use creative product names, realistic variant descriptions, and authentic-looking identifiers.
""".trimIndent()
  }

  private fun parseResponse(responseText: String): GeneratedGroceryData {
    try {
      val jsonResponse = json.decodeFromString<GeminiResponse>(
        responseText
      )
      return GeneratedGroceryData(
        products = jsonResponse.products.map { product ->
          GeneratedProduct(
            name = product.name,
            description = product.description,
            variants = product.variants.map { variant ->
              GeneratedVariant(
                name = variant.name,
                instances = variant.instances.map { instance ->
                  GeneratedInstance(
                    identifier = instance.identifier,
                    daysFromNow = instance.daysFromNow
                  )
                }
              )
            },
            standaloneInstances = product.standaloneInstances.map { instance ->
              GeneratedInstance(
                identifier = instance.identifier,
                daysFromNow = instance.daysFromNow
              )
            }
          )
        }
      )
    } catch (e: Exception) {
      throw IllegalStateException("Failed to parse Gemini response: ${e.message}", e)
    }
  }

  // Internal serialization models for parsing Gemini JSON response
  @Serializable
  private data class GeminiResponse(
    val products: List<GeminiProduct>
  )

  @Serializable
  private data class GeminiProduct(
    val name: String,
    val description: String,
    val variants: List<GeminiVariant>,
    val standaloneInstances: List<GeminiInstance>
  )

  @Serializable
  private data class GeminiVariant(
    val name: String,
    val instances: List<GeminiInstance>
  )

  @Serializable
  private data class GeminiInstance(
    val identifier: String,
    val daysFromNow: Int
  )
}
