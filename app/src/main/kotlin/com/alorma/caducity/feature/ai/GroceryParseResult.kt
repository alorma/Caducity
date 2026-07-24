package com.alorma.caducity.feature.ai

/**
 * Outcome of parsing a natural-language grocery description.
 *
 * Distinguishing these cases lets the UI show a precise message instead of a
 * single generic error for three very different situations.
 */
sealed interface GroceryParseResult {
  /** The model produced at least one grocery product. */
  data class Success(
    val proposals: List<GroceryProposal>,
  ) : GroceryParseResult

  /** The model ran but found no grocery products in the input. */
  data object NoGroceriesFound : GroceryParseResult

  /** The on-device model has not finished downloading yet. */
  data object ModelNotReady : GroceryParseResult

  /**
   * Generation or response parsing failed unexpectedly.
   *
   * [debugDetail] carries the raw model output (or error message) for
   * diagnostics; it is only surfaced in the UI on debug builds.
   */
  data class Failed(
    val debugDetail: String? = null,
  ) : GroceryParseResult
}
