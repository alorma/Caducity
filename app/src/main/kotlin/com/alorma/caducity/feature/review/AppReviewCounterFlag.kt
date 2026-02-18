package com.alorma.caducity.feature.review

import com.alorma.fireandforget.CounterFireAndForget
import com.alorma.fireandforget.FireAndForgetRunner

/**
 * Counter flag to track the number of successful item actions before requesting an in-app review.
 * The app will request a review after the user performs 3 successful item actions.
 */
class AppReviewCounterFlag(runner: FireAndForgetRunner) : CounterFireAndForget(
  fireAndForgetRunner = runner,
  name = "app_review_counter",
  counter = 3,
)
