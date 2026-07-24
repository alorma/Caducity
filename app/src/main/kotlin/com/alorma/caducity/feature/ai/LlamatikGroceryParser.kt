package com.alorma.caducity.feature.ai

import com.llamatik.library.platform.LlamaBridge
import java.text.Normalizer
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

/** Intermediate result of the product-extraction phase, before a category is inferred. */
internal data class ProductDraft(
  val name: String,
  val quantity: Int,
)

/**
 * Parses grocery input with the on-device model in two focused passes:
 *
 * 1. Extract the products (name + quantity) from the user's message.
 * 2. For each product, infer a single category — reusing an existing category
 *    when one fits.
 *
 * Splitting the work keeps each prompt simple, which a small model handles far
 * more reliably than asking it to extract and categorise in one shot.
 */
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
          val language = languageProvider.currentLanguageName()

          val rawProducts = generateFresh(buildGemma3Prompt(buildProductSystemPrompt(language), input))
          Timber.tag(TAG).d("PRODUCTS RAW:\n%s", rawProducts)

          val drafts = parseProducts(rawProducts)
          when {
            drafts == null -> GroceryParseResult.Failed(debugDetail = rawProducts)
            drafts.isEmpty() -> GroceryParseResult.NoGroceriesFound
            else -> {
              val proposals =
                drafts.map { draft ->
                  GroceryProposal(
                    productName = draft.name,
                    quantity = draft.quantity,
                    category = inferCategory(draft.name, existingCategories, language),
                  )
                }
              GroceryParseResult.Success(proposals)
            }
          }
        } catch (e: Exception) {
          Timber.tag(TAG).e(e, "Grocery parsing failed")
          GroceryParseResult.Failed(debugDetail = e.message ?: e::class.simpleName)
        }
      }
    }
  }

  /** Second pass: ask the model for a single category, falling back on any failure. */
  private fun inferCategory(
    productName: String,
    existingCategories: List<String>,
    language: String,
  ): String =
    try {
      val raw = generateFresh(buildGemma3Prompt(buildCategorySystemPrompt(existingCategories, language), productName))
      Timber.tag(TAG).d("CATEGORY RAW for '%s':\n%s", productName, raw)
      resolveCategory(raw, existingCategories, language)
    } catch (e: Exception) {
      Timber.tag(TAG).e(e, "Category inference failed for '%s'", productName)
      defaultCategory(language)
    }

  /** Clears the KV cache so every generation is independent, then generates. */
  private fun generateFresh(prompt: String): String {
    LlamaBridge.sessionReset()
    return LlamaBridge.generate(prompt)
  }

  /**
   * Loads the model once and reuses it across generations. The KV cache is
   * cleared per generation in [generateFresh] rather than reloading the whole
   * model, avoiding re-reading hundreds of MB from disk on every message.
   */
  private fun ensureModelLoaded(modelPath: String) {
    if (loadedModelPath == modelPath) return

    LlamaBridge.shutdown()
    // Low temperature = deterministic, factual output. No creativity needed here.
    // JSON repeats structural tokens heavily, so keep the repeat penalty low — a
    // high value pushes small models to emit malformed JSON.
    LlamaBridge.updateGenerateParams(
      temperature = 0.1f,
      maxTokens = 512,
      topP = 0.9f,
      topK = 40,
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
     * Extracts product drafts (name + quantity) from the model's raw output.
     *
     * Returns `null` when the response contained no parseable JSON (a genuine
     * failure), and an empty list when the model reported no groceries (`[]`).
     * Accepts either a JSON array or a single bare object.
     */
    fun parseProducts(raw: String): List<ProductDraft>? =
      try {
        val elements =
          extractBracketed(raw, '[', ']')?.let { json.parseToJsonElement(it).jsonArray }
            ?: extractBracketed(raw, '{', '}')?.let { listOf(json.parseToJsonElement(it)) }
        elements
          ?.map { element ->
            val obj = element.jsonObject
            ProductDraft(
              name = obj["product_name"]?.jsonPrimitive?.content.orEmpty(),
              quantity = obj["quantity"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
            )
          }?.filter { it.name.isNotBlank() }
      } catch (_: Exception) {
        null
      }

    /**
     * Cleans the plain-text category output: takes the first non-blank line,
     * drops any "Category:"-style label and surrounding markdown/quotes.
     * Returns `null` if nothing usable remains.
     */
    fun parseCategory(raw: String): String? {
      val firstLine =
        raw
          .lineSequence()
          .map { it.trim() }
          .firstOrNull { it.isNotBlank() } ?: return null
      val cleaned =
        firstLine
          .substringAfterLast(':', firstLine)
          .trim()
          .trim('"', '\'', '`', '*', '.', '-', ' ')
      return cleaned.ifBlank { null }
    }

    /** Prefers an existing category matching the model's answer, else the cleaned answer, else a default. */
    internal fun resolveCategory(
      raw: String,
      existingCategories: List<String>,
      languageName: String,
    ): String {
      val parsed = parseCategory(raw) ?: return defaultCategory(languageName)
      val normalized = normalizeCategory(parsed)
      val existing = existingCategories.firstOrNull { normalizeCategory(it) == normalized }
      return existing ?: parsed
    }

    internal fun defaultCategory(languageName: String): String =
      when (languageName.lowercase()) {
        "spanish" -> "Otros"
        "catalan" -> "Altres"
        else -> "Other"
      }

    private fun normalizeCategory(value: String): String =
      Normalizer
        .normalize(value.lowercase().trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

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

    /** Phase 1 prompt: extract products and quantities only. */
    fun buildProductSystemPrompt(languageName: String = "English"): String =
      """
      You are a grocery list parser.
      Read ONLY the user's message and list the grocery products it names.
      The message may mention expiration or purchase dates — IGNORE any dates.
      Return ONLY a JSON array, nothing else. No markdown, no prose, no code fences.
      Each array item is an object with exactly these keys:
      - "product_name": the specific food the user bought, written in $languageName.
      - "quantity": the total number of units as an integer (default to 1 if unspecified).
      Rules:
      - If the message names no grocery products, return exactly: []
      - Output EXACTLY ONE object per distinct product the user mentions.
      - Never invent products that are not in the user's message.
      """.trimIndent()

    /** Phase 2 prompt: infer one category for a single product. */
    fun buildCategorySystemPrompt(
      existingCategories: List<String>,
      languageName: String = "English",
    ): String {
      val guidance =
        if (existingCategories.isEmpty()) {
          "Answer with a short, common grocery category (its food group)."
        } else {
          val list = existingCategories.joinToString(", ") { "\"$it\"" }
          "Choose the best match from this list if one fits: $list. Only if none fits, answer with a short new category name."
        }
      return """
        You assign a single grocery product to a food category.
        Reply with ONLY the category name in $languageName — no explanation, no punctuation, no quotes.
        $guidance
        """.trimIndent()
    }
  }
}
