package com.alorma.caducity.feature.ai

import com.llamatik.library.platform.LlamaBridge
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

class LlamatikGroceryParser(
  private val modelManager: ModelManager,
  private val languageProvider: LanguageProvider,
) : AiGroceryParser {
  // Native llama.cpp state is a process-wide singleton, so serialize every
  // access to it and keep inference off the shared Default/IO pools by pinning
  // it to a single dedicated thread.
  private val inferenceDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  private val mutex = Mutex()

  // Path of the model currently loaded into LlamaBridge, or null if none.
  private var loadedModelPath: String? = null

  override suspend fun parse(
    input: String,
    existingCategories: List<String>,
  ): GroceryParseResult {
    if (!modelManager.isModelReady()) return GroceryParseResult.ModelNotReady

    return mutex.withLock {
      withContext(inferenceDispatcher) {
        try {
          ensureModelLoaded(modelManager.modelFilePath())

          val systemPrompt =
            buildSystemPrompt(
              existingCategories = existingCategories,
              languageName = languageProvider.currentLanguageName(),
            )
          val prompt = buildGemma3Prompt(system = systemPrompt, userInput = input)
          Timber.tag(TAG).d("PROMPT:\n%s", prompt)
          val raw = LlamaBridge.generate(prompt)
          Timber.tag(TAG).d("RAW RESPONSE:\n%s", raw)

          when (val parsed = parseProposals(raw)) {
            null -> GroceryParseResult.Failed(debugDetail = raw)
            else -> if (parsed.isEmpty()) GroceryParseResult.NoGroceriesFound else GroceryParseResult.Success(parsed)
          }
        } catch (e: Exception) {
          Timber.tag(TAG).e(e, "Grocery parsing failed")
          GroceryParseResult.Failed(debugDetail = e.message ?: e::class.simpleName)
        }
      }
    }
  }

  /**
   * Loads the model once and reuses it. Between generations the KV cache is
   * cleared with [LlamaBridge.sessionReset] instead of reloading the whole
   * model, which avoids re-reading hundreds of MB from disk on every message.
   */
  private fun ensureModelLoaded(modelPath: String) {
    if (loadedModelPath == modelPath) {
      LlamaBridge.sessionReset()
      return
    }

    LlamaBridge.shutdown()
    // Low temperature = deterministic, factual output. No creativity needed here.
    LlamaBridge.updateGenerateParams(
      temperature = 0.1f,
      maxTokens = 512,
      topP = 0.9f,
      topK = 40,
      // JSON repeats structural tokens ({ } " , field names) heavily, so keep the
      // repeat penalty low — a high value pushes small models to emit malformed JSON.
      repeatPenalty = 1.1f,
      contextLength = 2048,
      numThreads = Runtime.getRuntime().availableProcessors().coerceAtMost(4),
      useMmap = true,
      flashAttention = true,
      batchSize = 512,
    )
    LlamaBridge.initGenerateModel(modelPath)
    loadedModelPath = modelPath
  }

  companion object {
    private const val TAG = "LlamatikParser"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Extracts grocery proposals from the model's raw output.
     *
     * Returns `null` when the response contained no parseable JSON (a genuine
     * failure), and an empty list when the model correctly reported no
     * groceries (`[]`). Accepts either a JSON array or a single bare object, so
     * a model that forgets the array brackets still yields a proposal.
     */
    fun parseProposals(raw: String): List<GroceryProposal>? =
      try {
        val elements =
          extractBracketed(raw, '[', ']')?.let { json.parseToJsonElement(it).jsonArray }
            ?: extractBracketed(raw, '{', '}')?.let { listOf(json.parseToJsonElement(it)) }
        elements
          ?.map { element ->
            val obj = element.jsonObject
            GroceryProposal(
              productName = obj["product_name"]?.jsonPrimitive?.content.orEmpty(),
              quantity = obj["quantity"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
              category = obj["category"]?.jsonPrimitive?.content.orEmpty(),
            )
          }?.filter { it.productName.isNotBlank() }
      } catch (_: Exception) {
        null
      }

    /** Returns the substring from the first [open] to the last [close], or null if absent. */
    private fun extractBracketed(
      raw: String,
      open: Char,
      close: Char,
    ): String? {
      val start = raw.indexOf(open)
      val end = raw.lastIndexOf(close)
      return if (start == -1 || end == -1 || end <= start) null else raw.substring(start, end + 1)
    }

    fun buildGemma3Prompt(
      system: String,
      userInput: String,
    ): String =
      // Gemma has no dedicated system role — only user/model turns. The
      // instructions must be folded into the first user turn; emitting a
      // <start_of_turn>system block the model was never trained on makes it
      // ignore the instructions and reply conversationally instead.
      buildString {
        append("<start_of_turn>user\n")
        append(system.trim())
        append("\n\n")
        append(userInput.trim())
        append("\n<end_of_turn>\n")
        append("<start_of_turn>model\n")
      }

    fun buildSystemPrompt(
      existingCategories: List<String>,
      languageName: String = "English",
    ): String {
      val categoryInstruction =
        if (existingCategories.isEmpty()) {
          "- For category, infer an appropriate grocery category (e.g. Dairy, Meat, Vegetables, Fruits, Bakery, Beverages, Snacks, Frozen, Condiments, Canned)."
        } else {
          val list = existingCategories.joinToString(", ") { "\"$it\"" }
          "- For category, you MUST pick the most appropriate one from this list: $list. Only infer a new category name if none of the existing ones fits."
        }
      return """
        You are a grocery expiration tracker assistant.
        The user will describe groceries they bought, possibly with quantities.
        The input may mention expiration or purchase dates — IGNORE any dates, extract only products.
        The user's language is $languageName. Write product_name and category values in $languageName.
        Extract each distinct product and return ONLY a JSON array. No markdown, no prose, no code fences.
        Rules:
        - If the input contains no grocery products, return exactly: []
        - Output EXACTLY ONE object per distinct product.
        - Every object MUST have all three fields: product_name, category, quantity.
        - quantity is the total number of individual units mentioned (default to 1 if unspecified).
        - Choose ONE category per product.
        $categoryInstruction

        Example input: "2 strawberry yogurts and a liter of milk"
        Example output: [{"product_name":"Strawberry yogurt","category":"Dairy","quantity":2},{"product_name":"Milk","category":"Dairy","quantity":1}]
        """.trimIndent()
    }
  }
}
