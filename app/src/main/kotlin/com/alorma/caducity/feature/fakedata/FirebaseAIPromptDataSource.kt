package com.alorma.caducity.feature.fakedata

import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.feature.fakedata.models.GeneratedGroceryData
import com.alorma.caducity.feature.fakedata.models.GeneratedInstance
import com.alorma.caducity.feature.fakedata.models.GeneratedProduct
import com.alorma.caducity.feature.fakedata.models.GeneratedProductVariants
import com.alorma.caducity.feature.fakedata.models.GeneratedVariant
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

class FirebaseAIPromptDataSource : AIPromptDataSource {

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }


  @OptIn(PublicPreviewAPI::class)
  override suspend fun generateFakeData(
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

      val generativeModel = Firebase.ai(
        backend = GenerativeBackend.googleAI(),
      ).templateGenerativeModel(
        requestOptions = RequestOptions(),
      )

      val response = generativeModel.generateContent(
        templateId = "debug-fake-generation",
        mapOf(
          "productsToGenerate" to productsToGenerate.toString(),
          "variantsPerProduct" to variantsPerProduct.toString(),
          "instancesMin" to instancesPerVariantRange.first.toString(),
          "instancesMax" to instancesPerVariantRange.last.toString()
        )
      )

      val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini")

      val parsed = parseResponse(responseText)
      Result.success(parsed)
    } catch (e: Exception) {
      Timber.e(e, "generateFakeData failed: ${e.message}")
      Result.failure(e)
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
        templateId = "product-list-generation",
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
      Timber.e(e, "generateFromUserPrompt failed: ${e.message}")
      Result.failure(e)
    }
  }

  @OptIn(PublicPreviewAPI::class)
  override suspend fun generateVariantsForProduct(
    userPrompt: String,
    productName: String
  ): Result<GeneratedProductVariants> = withContext(Dispatchers.IO) {
    try {
      // Input validation
      if (userPrompt.isBlank()) {
        return@withContext Result.success(
          GeneratedProductVariants(
            variants = emptyList(),
            standaloneInstances = emptyList()
          )
        )
      }

      val generativeModel = Firebase.ai(
        backend = GenerativeBackend.googleAI()
      ).templateGenerativeModel(
        requestOptions = RequestOptions(),
      )

      val response = generativeModel.generateContent(
        templateId = "product-detail-variants",
        mapOf(
          "productName" to productName,
          "userPrompt" to userPrompt
        )
      )

      val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini")

      val parsed = parseVariantsResponse(responseText)

      // Validate output
      val validatedVariants = parsed.variants.filter { variant ->
        variant.instances.all { instance ->
          instance.daysFromNow in -30..365
        }
      }
      val validatedStandaloneInstances = parsed.standaloneInstances.filter { instance ->
        instance.daysFromNow in -30..365
      }

      Result.success(
        GeneratedProductVariants(
          variants = validatedVariants,
          standaloneInstances = validatedStandaloneInstances
        )
      )
    } catch (e: Exception) {
      Timber.e(e, "generateVariantsForProduct failed: ${e.message}")
      Result.failure(e)
    }
  }


  private fun parseResponse(responseText: String): GeneratedGroceryData {
    try {
      // Clean the response text - lite models may add markdown code blocks or extra text
      val cleanedJson = cleanJsonResponse(responseText)

      val jsonResponse = json.decodeFromString<GeminiResponse>(cleanedJson)
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
      throw IllegalStateException("Failed to parse Gemini response: ${e.message}\nResponse: $responseText", e)
    }
  }

  private fun parseVariantsResponse(responseText: String): GeneratedProductVariants {
    try {
      val cleanedJson = cleanJsonResponse(responseText)

      val jsonResponse = json.decodeFromString<GeminiVariantsResponse>(cleanedJson)
      return GeneratedProductVariants(
        variants = jsonResponse.variants.map { variant ->
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
        standaloneInstances = jsonResponse.standaloneInstances.map { instance ->
          GeneratedInstance(
            identifier = instance.identifier,
            daysFromNow = instance.daysFromNow
          )
        }
      )
    } catch (e: Exception) {
      throw IllegalStateException("Failed to parse Gemini variants response: ${e.message}\nResponse: $responseText", e)
    }
  }

  private fun cleanJsonResponse(responseText: String): String {
    return responseText
      .trim()
      .removePrefix("```json")
      .removePrefix("```")
      .removeSuffix("```")
      .trim()
      .let { text ->
        // Find the first { and last } to extract just the JSON object
        val startIndex = text.indexOf('{')
        val endIndex = text.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
          text.substring(startIndex, endIndex + 1)
        } else {
          text
        }
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

  @Serializable
  private data class GeminiVariantsResponse(
    val variants: List<GeminiVariant>,
    val standaloneInstances: List<GeminiInstance>
  )
}
