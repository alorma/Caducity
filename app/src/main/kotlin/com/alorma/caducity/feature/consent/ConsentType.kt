package com.alorma.caducity.feature.consent

/**
 * Represents the different types of consent for data collection.
 * Based on Google's consent mode types.
 */
enum class ConsentType {
  /**
   * Analytics storage - Controls whether analytics data can be collected
   */
  ANALYTICS_STORAGE,

  /**
   * Ad storage - Controls whether ad-related data can be collected
   */
  AD_STORAGE,

  /**
   * Ad user data - Controls whether user data can be sent to Google for advertising purposes
   */
  AD_USER_DATA,

  /**
   * Ad personalization - Controls whether personalized advertising is enabled
   */
  AD_PERSONALIZATION
}
