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
  private var modelInitialized = false

  override suspend fun parse(
    input: String,
    todayIso: String,
  ): List<GroceryProposal> =
    withContext(Dispatchers.Default) {
      if (!modelManager.isModelReady()) return@withContext emptyList()

      if (!modelInitialized) {
        LlamaBridge.initGenerateModel(modelManager.modelFilePath())
        modelInitialized = true
      }

      // All generateJson* variants in Llamatik use a plain-text prompt builder that
      // ignores the system prompt and skips Gemma3 turn markers, causing the grammar
      // sampler to crash when the instruction-tuned model generates unexpected tokens.
      // Workaround: use generate() (no grammar constraint) with an explicit Gemma3
      // prompt that includes a JSON example in the system instructions, then extract
      // the JSON from the free-text output.
      val prompt = buildGemma3Prompt(
        system = SYSTEM_PROMPT,
        context = "Today's date is $todayIso.",
        userInput = input,
      )

      val raw = LlamaBridge.generate(prompt)
      parseResponse(raw)
    }

  private fun parseResponse(raw: String): List<GroceryProposal> =
    try {
      // Extract the first JSON array from the model's free-text output.
      val start = raw.indexOf('[')
      val end = raw.lastIndexOf(']')
      if (start == -1 || end == -1 || end <= start) return emptyList()
      val arrayJson = raw.substring(start, end + 1)
      val array = json.parseToJsonElement(arrayJson).jsonArray
      array.map { element ->
        val obj = element.jsonObject
        GroceryProposal(
          productName = obj["product_name"]?.jsonPrimitive?.content.orEmpty(),
          quantity = obj["quantity"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
          expirationDate = obj["expiration_date"]?.jsonPrimitive?.content.orEmpty(),
          category = obj["category"]?.jsonPrimitive?.content.orEmpty(),
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
      Extract each distinct product and return ONLY a JSON array. No markdown, no prose.
      Use ISO-8601 format (YYYY-MM-DD) for dates.
      If the user says a relative date like "next Friday" or "in 3 days", resolve it using today's date from the context.
      For each product, infer an appropriate grocery category (e.g. Dairy, Meat, Vegetables, Fruits, Bakery, Beverages, Snacks, Frozen, Condiments, Canned).
      Output format — return ONLY this, nothing else:
      [{"product_name":"Milk","category":"Dairy","quantity":2,"expiration_date":"2026-03-15"}]
      """.trimIndent()

    fun buildGemma3Prompt(
      system: String,
      context: String,
      userInput: String,
    ): String =
      buildString {
        append("<start_of_turn>system\n")
        append(system.trim())
        append("\n<end_of_turn>\n")
        append("<start_of_turn>user\n")
        append(context.trim())
        append("\n\n")
        append(userInput.trim())
        append("\n<end_of_turn>\n")
        append("<start_of_turn>model\n")
      }
  }
}
