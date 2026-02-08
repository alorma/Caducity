package com.alorma.caducity.feature.consent

import com.google.firebase.analytics.FirebaseAnalytics
import com.russhwolf.settings.Settings

/**
 * Manager for handling user consent preferences and applying them to Firebase Analytics.
 *
 * This manager:
 * - Stores user consent preferences persistently
 * - Applies consent settings to Firebase Analytics using setConsent API
 * - Follows Google's consent mode guidelines
 *
 * Based on: https://developers.google.com/tag-platform/security/guides/app-consent
 */
class ConsentManager(
  private val analytics: FirebaseAnalytics,
  private val settings: Settings,
) {
  companion object {
    private const val KEY_ANALYTICS_STORAGE = "consent_analytics_storage"
    private const val KEY_AD_STORAGE = "consent_ad_storage"
    private const val KEY_AD_USER_DATA = "consent_ad_user_data"
    private const val KEY_AD_PERSONALIZATION = "consent_ad_personalization"
    private const val KEY_CONSENT_GIVEN = "consent_given"
  }

  /**
   * Check if user has already provided consent choices.
   */
  fun hasUserProvidedConsent(): Boolean {
    return settings.getBoolean(KEY_CONSENT_GIVEN, false)
  }

  /**
   * Get current consent preferences from storage.
   */
  fun getConsentPreferences(): ConsentPreferences {
    return ConsentPreferences(
      analyticsStorage = getConsentStatus(KEY_ANALYTICS_STORAGE),
      adStorage = getConsentStatus(KEY_AD_STORAGE),
      adUserData = getConsentStatus(KEY_AD_USER_DATA),
      adPersonalization = getConsentStatus(KEY_AD_PERSONALIZATION),
    )
  }

  /**
   * Save consent preferences and apply them to Firebase Analytics.
   */
  fun setConsentPreferences(preferences: ConsentPreferences) {
    // Save to persistent storage
    saveConsentStatus(KEY_ANALYTICS_STORAGE, preferences.analyticsStorage)
    saveConsentStatus(KEY_AD_STORAGE, preferences.adStorage)
    saveConsentStatus(KEY_AD_USER_DATA, preferences.adUserData)
    saveConsentStatus(KEY_AD_PERSONALIZATION, preferences.adPersonalization)
    settings.putBoolean(KEY_CONSENT_GIVEN, true)

    // Apply to Firebase Analytics
    applyConsentToFirebase(preferences)
  }

  /**
   * Apply default consent settings (all denied) before user interaction.
   * Should be called as early as possible in app initialization.
   */
  fun applyDefaultConsent() {
    if (!hasUserProvidedConsent()) {
      applyConsentToFirebase(ConsentPreferences.DEFAULT)
    } else {
      applyConsentToFirebase(getConsentPreferences())
    }
  }

  private fun applyConsentToFirebase(preferences: ConsentPreferences) {
    val consentMap = mapOf(
      FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to toFirebaseConsentStatus(preferences.analyticsStorage),
      FirebaseAnalytics.ConsentType.AD_STORAGE to toFirebaseConsentStatus(preferences.adStorage),
      FirebaseAnalytics.ConsentType.AD_USER_DATA to toFirebaseConsentStatus(preferences.adUserData),
      FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to toFirebaseConsentStatus(preferences.adPersonalization),
    )
    analytics.setConsent(consentMap)
  }

  private fun getConsentStatus(key: String): ConsentStatus {
    val value = settings.getString(key, ConsentStatus.DENIED.name)
    return ConsentStatus.valueOf(value)
  }

  private fun saveConsentStatus(key: String, status: ConsentStatus) {
    settings.putString(key, status.name)
  }

  private fun toFirebaseConsentStatus(status: ConsentStatus): FirebaseAnalytics.ConsentStatus {
    return when (status) {
      ConsentStatus.GRANTED -> FirebaseAnalytics.ConsentStatus.GRANTED
      ConsentStatus.DENIED -> FirebaseAnalytics.ConsentStatus.DENIED
    }
  }
}
