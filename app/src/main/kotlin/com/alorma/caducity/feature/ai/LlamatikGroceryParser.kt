package com.alorma.caducity.feature.ai

import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LlamatikGroceryParser(
  private val modelManager: ModelManager,
) : AiGroceryParser {
  private val json = Json { ignoreUnknownKeys = true }

  override suspend fun parse(
    input: String,
    todayIso: String,
  ): List<GroceryProposal> =
    withContext(Dispatchers.Default) {
      if (!modelManager.isModelReady()) return@withContext emptyList()

      LlamaBridge.initGenerateModel(modelManager.modelFilePath())

      val rawJson =
        LlamaBridge.generateJsonWithContext(
          systemPrompt = SYSTEM_PROMPT,
          contextBlock = "Today's date is $todayIso.",
          userPrompt = input,
          jsonSchema = JSON_SCHEMA,
        )

      parseResponse(rawJson)
    }

  private fun parseResponse(rawJson: String): List<GroceryProposal> =
    try {
      val root = json.parseToJsonElement(rawJson).jsonObject
      val items = root["items"]?.jsonArray ?: return emptyList()
      items.map { element ->
        val obj = element.jsonObject
        GroceryProposal(
          productName = obj["product_name"]?.jsonPrimitive?.content.orEmpty(),
          quantity = obj["quantity"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
          expirationDate = obj["expiration_date"]?.jsonPrimitive?.content.orEmpty(),
        )
      }.filter { it.productName.isNotBlank() && it.expirationDate.isNotBlank() }
    } catch (_: Exception) {
      emptyList()
    }

  companion object {
    private val SYSTEM_PROMPT =
      """
      You are a grocery expiration tracker assistant.
      The user will describe groceries they bought, including quantity and expiration date.
      Extract each distinct product and return ONLY valid JSON matching the provided schema.
      Use ISO-8601 format (YYYY-MM-DD) for dates.
      If the user says a relative date like "next Friday" or "in 3 days", resolve it using today's date from the context.
      Never include explanations or extra text — only the JSON object.
      """.trimIndent()

    private val JSON_SCHEMA =
      """
      {
        "type": "object",
        "properties": {
          "items": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "product_name": { "type": "string" },
                "quantity":     { "type": "integer" },
                "expiration_date": { "type": "string" }
              },
              "required": ["product_name", "quantity", "expiration_date"]
            }
          }
        },
        "required": ["items"]
      }
      """.trimIndent()
  }
}
