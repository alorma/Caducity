package com.alorma.caducity.feature.review

import com.alorma.fireandforget.CounterFireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/**
 * Flag to control when to show the in-app review prompt.
 * Uses a counter to suppress review requests for the first 3 successful item actions.
 * After the counter reaches 0, [isEnabled] returns true, indicating the review should be shown.
 */
class ShowAppReviewFlag(runner: FireAndForgetRunner) : CounterFireAndForget(
  fireAndForgetRunner = runner,
  name = "app_review_counter",
  counter = 3,
) {
  /**
   * Returns true when the review should be shown (after 3 actions have been completed).
   * Inverts the counter logic so callers don't need to use negation.
   */
  override fun isEnabled(): Boolean {
    return !super.isEnabled()
  }
}
