package com.alorma.caducity.feature.review

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

/**
 * Google Play Store implementation of in-app review manager.
 * Uses the Play Core library to request in-app reviews.
 */
class PlayStoreInAppReviewManager : InAppReviewManager {
  override suspend fun requestReview(activity: Activity): Result<Unit> =
    try {
      val reviewManager = ReviewManagerFactory.create(activity)

      // Request a ReviewInfo object
      val reviewInfo: ReviewInfo = reviewManager.requestReviewFlow().await()

      // Launch the in-app review flow
      reviewManager.launchReviewFlow(activity, reviewInfo).await()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
