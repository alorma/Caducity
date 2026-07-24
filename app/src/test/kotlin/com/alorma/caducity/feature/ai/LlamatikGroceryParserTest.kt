package com.alorma.caducity.feature.ai

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class LlamatikGroceryParserTest {
  // ── parseProducts ──────────────────────────────────────────────────────

  @Test
  fun `parseProducts extracts products and quantities from a clean JSON array`() {
    val raw =
      """
      [
        {"product_name": "Milk", "quantity": "2"},
        {"product_name": "Bread", "quantity": "1"}
      ]
      """.trimIndent()

    val result = LlamatikGroceryParser.parseProducts(raw)

    expectThat(result).isNotNull().hasSize(2)
    expectThat(result?.get(0)).isEqualTo(ProductDraft(name = "Milk", quantity = 2))
    expectThat(result?.get(1)).isEqualTo(ProductDraft(name = "Bread", quantity = 1))
  }

  @Test
  fun `parseProducts tolerates prose and code fences around the array`() {
    val raw = "Here you go:\n```json\n[{\"product_name\":\"Eggs\",\"quantity\":\"12\"}]\n```"

    val result = LlamatikGroceryParser.parseProducts(raw)

    expectThat(result).isNotNull().hasSize(1)
    expectThat(result?.first()?.name).isEqualTo("Eggs")
    expectThat(result?.first()?.quantity).isEqualTo(12)
  }

  @Test
  fun `parseProducts defaults quantity to 1 when missing or invalid`() {
    val raw = """[{"product_name":"Apples"}]"""

    expectThat(LlamatikGroceryParser.parseProducts(raw)?.first()?.quantity).isEqualTo(1)
  }

  @Test
  fun `parseProducts drops entries without a product name`() {
    val raw =
      """
      [
        {"product_name":"","quantity":"1"},
        {"product_name":"Milk","quantity":"1"}
      ]
      """.trimIndent()

    expectThat(LlamatikGroceryParser.parseProducts(raw)).isNotNull().hasSize(1)
  }

  @Test
  fun `parseProducts accepts a single bare object without array brackets`() {
    val raw = """{"product_name":"Milk","quantity":"2"}"""

    val result = LlamatikGroceryParser.parseProducts(raw)

    expectThat(result).isNotNull().hasSize(1)
    expectThat(result?.first()?.name).isEqualTo("Milk")
  }

  @Test
  fun `parseProducts returns empty list for an explicit empty array`() {
    expectThat(LlamatikGroceryParser.parseProducts("[]")).isNotNull().isEmpty()
  }

  @Test
  fun `parseProducts returns null when no JSON is present`() {
    expectThat(LlamatikGroceryParser.parseProducts("I don't understand")).isNull()
  }

  @Test
  fun `parseProducts returns null for malformed JSON`() {
    expectThat(LlamatikGroceryParser.parseProducts("[{not valid json}]")).isNull()
  }

  // ── parseCategory ──────────────────────────────────────────────────────

  @Test
  fun `parseCategory returns a plain category word`() {
    expectThat(LlamatikGroceryParser.parseCategory("Dairy")).isEqualTo("Dairy")
  }

  @Test
  fun `parseCategory strips markdown and quotes`() {
    expectThat(LlamatikGroceryParser.parseCategory("**\"Làctics\"**")).isEqualTo("Làctics")
  }

  @Test
  fun `parseCategory drops a leading label`() {
    expectThat(LlamatikGroceryParser.parseCategory("Category: Meat")).isEqualTo("Meat")
  }

  @Test
  fun `parseCategory takes the first non-blank line`() {
    expectThat(LlamatikGroceryParser.parseCategory("\n\nFruits\nsome trailing chatter")).isEqualTo("Fruits")
  }

  @Test
  fun `parseCategory returns null when empty`() {
    expectThat(LlamatikGroceryParser.parseCategory("   \n  ")).isNull()
  }

  // ── resolveCategory ────────────────────────────────────────────────────

  @Test
  fun `resolveCategory reuses an existing category ignoring case and accents`() {
    val result = LlamatikGroceryParser.resolveCategory("lactics", listOf("Làctics"), "Catalan")

    expectThat(result).isEqualTo("Làctics")
  }

  @Test
  fun `resolveCategory keeps the model's answer when no existing category matches`() {
    val result = LlamatikGroceryParser.resolveCategory("Meat", listOf("Dairy"), "English")

    expectThat(result).isEqualTo("Meat")
  }

  @Test
  fun `resolveCategory falls back to a localized default when the answer is empty`() {
    expectThat(LlamatikGroceryParser.resolveCategory("  ", emptyList(), "Spanish")).isEqualTo("Otros")
    expectThat(LlamatikGroceryParser.resolveCategory("", emptyList(), "Catalan")).isEqualTo("Altres")
    expectThat(LlamatikGroceryParser.resolveCategory("", emptyList(), "English")).isEqualTo("Other")
  }

  // ── prompts ────────────────────────────────────────────────────────────

  @Test
  fun `buildProductSystemPrompt ignores dates, forbids invented products, and has no copyable example`() {
    val prompt = LlamatikGroceryParser.buildProductSystemPrompt()

    expectThat(prompt)
      .contains("IGNORE any dates")
      .contains("Never invent products")
    // A concrete few-shot example gets echoed verbatim by small models.
    expectThat(prompt).not().contains("Example output")
  }

  @Test
  fun `buildProductSystemPrompt steers product names to the current language`() {
    expectThat(LlamatikGroceryParser.buildProductSystemPrompt("Spanish")).contains("Spanish")
  }

  @Test
  fun `buildCategorySystemPrompt lists existing categories when provided`() {
    val prompt = LlamatikGroceryParser.buildCategorySystemPrompt(listOf("Dairy", "Meat"), "English")

    expectThat(prompt)
      .contains("\"Dairy\"")
      .contains("\"Meat\"")
  }

  @Test
  fun `buildCategorySystemPrompt steers the category to the current language`() {
    expectThat(LlamatikGroceryParser.buildCategorySystemPrompt(emptyList(), "Catalan")).contains("Catalan")
  }

  @Test
  fun `buildGemma3Prompt folds the system prompt into the user turn`() {
    val prompt = LlamatikGroceryParser.buildGemma3Prompt(system = "SYS", userInput = "2 milks")

    expectThat(prompt)
      .contains("<start_of_turn>user")
      .contains("SYS")
      .contains("2 milks")
      .contains("<start_of_turn>model")
    // Gemma has no system role — the instructions live inside the user turn.
    expectThat(prompt).not().contains("<start_of_turn>system")
  }
}
