package com.alorma.caducity.feature.consent

import org.junit.Assert.assertEquals
import org.junit.Test

class ConsentPreferencesTest {
  @Test
  fun `default preferences should have all consents denied`() {
    val preferences = ConsentPreferences.DEFAULT
    assertEquals(ConsentStatus.DENIED, preferences.analyticsStorage)
    assertEquals(ConsentStatus.DENIED, preferences.adStorage)
    assertEquals(ConsentStatus.DENIED, preferences.adUserData)
    assertEquals(ConsentStatus.DENIED, preferences.adPersonalization)
  }

  @Test
  fun `all granted preferences should have all consents granted`() {
    val preferences = ConsentPreferences.ALL_GRANTED
    assertEquals(ConsentStatus.GRANTED, preferences.analyticsStorage)
    assertEquals(ConsentStatus.GRANTED, preferences.adStorage)
    assertEquals(ConsentStatus.GRANTED, preferences.adUserData)
    assertEquals(ConsentStatus.GRANTED, preferences.adPersonalization)
  }

  @Test
  fun `analytics only preferences should only grant analytics`() {
    val preferences = ConsentPreferences.ANALYTICS_ONLY
    assertEquals(ConsentStatus.GRANTED, preferences.analyticsStorage)
    assertEquals(ConsentStatus.DENIED, preferences.adStorage)
    assertEquals(ConsentStatus.DENIED, preferences.adUserData)
    assertEquals(ConsentStatus.DENIED, preferences.adPersonalization)
  }
}
