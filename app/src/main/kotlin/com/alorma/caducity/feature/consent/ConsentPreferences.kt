package com.alorma.caducity.feature.consent

/**
 * Data class representing user consent preferences.
 */
data class ConsentPreferences(
  val analyticsStorage: ConsentStatus = ConsentStatus.DENIED,
  val adStorage: ConsentStatus = ConsentStatus.DENIED,
  val adUserData: ConsentStatus = ConsentStatus.DENIED,
  val adPersonalization: ConsentStatus = ConsentStatus.DENIED,
) {
  companion object {
    /**
     * Default consent preferences with all types denied.
     */
    val DEFAULT = ConsentPreferences()

    /**
     * All consents granted.
     */
    val ALL_GRANTED = ConsentPreferences(
      analyticsStorage = ConsentStatus.GRANTED,
      adStorage = ConsentStatus.GRANTED,
      adUserData = ConsentStatus.GRANTED,
      adPersonalization = ConsentStatus.GRANTED,
    )

    /**
     * Only analytics consent granted (recommended for privacy-focused apps).
     */
    val ANALYTICS_ONLY = ConsentPreferences(
      analyticsStorage = ConsentStatus.GRANTED,
      adStorage = ConsentStatus.DENIED,
      adUserData = ConsentStatus.DENIED,
      adPersonalization = ConsentStatus.DENIED,
    )
  }
}
