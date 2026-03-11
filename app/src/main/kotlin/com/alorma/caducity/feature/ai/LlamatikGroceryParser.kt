package com.alorma.caducity.feature.ai

import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

class LlamatikGroceryParser(
  private val modelManager: ModelManager,
) : AiGroceryParser {
  private val json = Json { ignoreUnknownKeys = true }

  override suspend fun parse(
    input: String,
    existingCategories: List<String>,
  ): List<GroceryProposal> =
    withContext(Dispatchers.Default) {
      if (!modelManager.isModelReady()) return@withContext emptyList()

      // Shutdown + reinitialize before every generation to free native resources
      // and clear the model's KV cache / context state from any previous run.
      LlamaBridge.shutdown()
      LlamaBridge.initGenerateModel(modelManager.modelFilePath())

      // Low temperature = deterministic, factual output. No creativity needed here.
      LlamaBridge.updateGenerateParams(
        temperature = 0.1f,
        maxTokens = 512,
        topP = 0.9f,
        topK = 40,
        repeatPenalty = 1.3f,
      )

      val systemPrompt = buildSystemPrompt(existingCategories)
      val prompt = buildGemma3Prompt(system = systemPrompt, userInput = input)
      Timber.tag("LlamatikParser").d("PROMPT:\n%s", prompt)
      val raw = LlamaBridge.generate(prompt)
      Timber.tag("LlamatikParser").d("RAW RESPONSE:\n%s", raw)
      parseResponse(raw)
    }

  private fun parseResponse(raw: String): List<GroceryProposal> =
    try {
      val start = raw.indexOf('[')
      val end = raw.lastIndexOf(']')
      if (start == -1 || end == -1 || end <= start) return emptyList()
      val arrayJson = raw.substring(start, end + 1)
      val array = json.parseToJsonElement(arrayJson).jsonArray
      array
        .map { element ->
          val obj = element.jsonObject
          GroceryProposal(
            productName = obj["product_name"]?.jsonPrimitive?.content.orEmpty(),
            quantity = obj["quantity"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
            category = obj["category"]?.jsonPrimitive?.content.orEmpty(),
          )
        }.filter { it.productName.isNotBlank() }
    } catch (_: Exception) {
      emptyList()
    }

  companion object {
    fun buildGemma3Prompt(
      system: String,
      userInput: String,
    ): String =
      buildString {
        append("<start_of_turn>system\n")
        append(system.trim())
        append("\n<end_of_turn>\n")
        append("<start_of_turn>user\n")
        append(userInput.trim())
        append("\n<end_of_turn>\n")
        append("<start_of_turn>model\n")
      }

    fun buildSystemPrompt(existingCategories: List<String>): String {
      val categoryInstruction =
        if (existingCategories.isEmpty()) {
          "- For category, infer an appropriate grocery category (e.g. Dairy, Meat, Vegetables, Fruits, Bakery, Beverages, Snacks, Frozen, Condiments, Canned)."
        } else {
          val list = existingCategories.joinToString(", ") { "\"$it\"" }
          "- For category, you MUST pick the most appropriate one from this list: $list. Only infer a new category name if none of the existing ones fits."
        }
      return """
        You are a grocery expiration tracker assistant.
        The user will describe groceries they bought, including quantity.
        Extract each distinct product and return ONLY a JSON array. No markdown, no prose, no code fences.
        Rules:
        - If the input contains no grocery products, return exactly: []
        - Output EXACTLY ONE object per distinct product.
        - Every object MUST have all three fields: product_name, category, quantity.
        - quantity is the total number of individual units mentioned.
        - Choose ONE category per product.
        $categoryInstruction
        """.trimIndent()
    }
  }
}
