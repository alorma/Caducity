package com.alorma.caducity.feature.review

import android.app.Activity

/**
 * Manager for requesting in-app review flow from Google Play Store.
 * Implements an abstract interface to make it testable and reusable.
 */
interface InAppReviewManager {
  /**
   * Requests an in-app review flow.
   * Note: The Play Store has quotas and may not show the review dialog every time.
   * This is expected behavior and should be handled gracefully.
   *
   * @param activity The activity context to show the review dialog
   * @return Result indicating success or failure
   */
  suspend fun requestReview(activity: Activity): Result<Unit>
}
