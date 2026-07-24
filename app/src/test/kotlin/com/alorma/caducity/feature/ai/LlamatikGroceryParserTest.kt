package com.alorma.caducity.feature.ai

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class LlamatikGroceryParserTest {
  // ── parseProposals ─────────────────────────────────────────────────────

  @Test
  fun `parseProposals extracts products from a clean JSON array`() {
    val raw =
      """
      [
        {"product_name": "Milk", "category": "Dairy", "quantity": "2"},
        {"product_name": "Bread", "category": "Bakery", "quantity": "1"}
      ]
      """.trimIndent()

    val result = LlamatikGroceryParser.parseProposals(raw)

    expectThat(result).isNotNull().hasSize(2).containsExactly(
      GroceryProposal(productName = "Milk", quantity = 2, category = "Dairy"),
      GroceryProposal(productName = "Bread", quantity = 1, category = "Bakery"),
    )
  }

  @Test
  fun `parseProposals tolerates prose and code fences around the array`() {
    val raw = "Here you go:\n```json\n[{\"product_name\":\"Eggs\",\"category\":\"Dairy\",\"quantity\":\"12\"}]\n```"

    val result = LlamatikGroceryParser.parseProposals(raw)

    expectThat(result).isNotNull().hasSize(1)
    expectThat(result?.first()?.productName).isEqualTo("Eggs")
    expectThat(result?.first()?.quantity).isEqualTo(12)
  }

  @Test
  fun `parseProposals defaults quantity to 1 when missing or invalid`() {
    val raw = """[{"product_name":"Apples","category":"Fruits"}]"""

    val result = LlamatikGroceryParser.parseProposals(raw)

    expectThat(result?.first()?.quantity).isEqualTo(1)
  }

  @Test
  fun `parseProposals drops entries without a product name`() {
    val raw =
      """
      [
        {"product_name":"","category":"Dairy","quantity":"1"},
        {"product_name":"Milk","category":"Dairy","quantity":"1"}
      ]
      """.trimIndent()

    val result = LlamatikGroceryParser.parseProposals(raw)

    expectThat(result).isNotNull().hasSize(1)
  }

  @Test
  fun `parseProposals accepts a single bare object without array brackets`() {
    val raw = """{"product_name":"Milk","category":"Dairy","quantity":"2"}"""

    val result = LlamatikGroceryParser.parseProposals(raw)

    expectThat(result).isNotNull().hasSize(1)
    expectThat(result?.first()?.productName).isEqualTo("Milk")
    expectThat(result?.first()?.quantity).isEqualTo(2)
  }

  @Test
  fun `parseProposals returns empty list for an explicit empty array`() {
    expectThat(LlamatikGroceryParser.parseProposals("[]")).isNotNull().isEmpty()
  }

  @Test
  fun `parseProposals returns null when no JSON array is present`() {
    expectThat(LlamatikGroceryParser.parseProposals("I don't understand")).isNull()
  }

  @Test
  fun `parseProposals returns null for malformed JSON`() {
    expectThat(LlamatikGroceryParser.parseProposals("[{not valid json}]")).isNull()
  }

  // ── buildSystemPrompt ──────────────────────────────────────────────────

  @Test
  fun `buildSystemPrompt lists existing categories when provided`() {
    val prompt = LlamatikGroceryParser.buildSystemPrompt(existingCategories = listOf("Dairy", "Meat"))

    expectThat(prompt)
      .contains("\"Dairy\"")
      .contains("\"Meat\"")
      .contains("MUST pick")
  }

  @Test
  fun `buildSystemPrompt asks the model to infer a category when none exist`() {
    val prompt = LlamatikGroceryParser.buildSystemPrompt(existingCategories = emptyList())

    expectThat(prompt).contains("infer an appropriate grocery category")
  }

  @Test
  fun `buildSystemPrompt instructs the model to ignore dates and includes an example`() {
    val prompt = LlamatikGroceryParser.buildSystemPrompt(existingCategories = emptyList())

    expectThat(prompt)
      .contains("IGNORE any dates")
      .contains("Example output:")
  }

  @Test
  fun `buildSystemPrompt steers output to the current language`() {
    val prompt =
      LlamatikGroceryParser.buildSystemPrompt(
        existingCategories = emptyList(),
        languageName = "Spanish",
      )

    expectThat(prompt).contains("Spanish")
  }

  // ── buildGemma3Prompt ──────────────────────────────────────────────────

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
