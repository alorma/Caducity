package com.alorma.caducity.base.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alorma.caducity.base.ui.theme.language.Language
import com.russhwolf.settings.Settings

abstract class LanguageManager(private val settings: Settings) {
  companion object {
    private const val KEY_LANGUAGE = "app_language"
  }

  val selectedLanguage: MutableState<Language> = mutableStateOf(loadLanguage())

  /**
   * Load the saved language from persistent storage.
   * Defaults to system language on first launch.
   */
  fun loadLanguage(): Language {
    // Try to get platform-specific language first (e.g., from AppCompatDelegate on Android)
    val platformCode = getPlatformLanguageCode()
    if (platformCode != null) {
      val language = Language.fromCode(platformCode)
      if (language != null) {
        // Sync Settings with platform language
        settings.putString(KEY_LANGUAGE, language.code)
        return language
      }
    }

    // Fall back to Settings
    val savedCode = settings.getStringOrNull(KEY_LANGUAGE)
    return if (savedCode != null) {
      Language.fromCode(savedCode) ?: Language.fromSystemLanguage()
    } else {
      // First launch: detect system language
      Language.fromSystemLanguage()
    }
  }

  /**
   * Set the language preference and apply it to the system.
   * This persists the preference and calls the platform-specific applyLanguage.
   */
  fun setLanguage(language: Language) {
    selectedLanguage.value = language
    settings.putString(KEY_LANGUAGE, language.code)
    applyLanguage(language.code)
  }

  /**
   * Get the current platform-specific language code.
   * On Android, this reads from AppCompatDelegate.getApplicationLocales().
   * Returns null if no platform language is set.
   */
  protected abstract fun getPlatformLanguageCode(): String?

  /**
   * Platform-specific method to apply the language to the app.
   * Override this in platform implementations (Android, iOS, etc.)
   */
  protected abstract fun applyLanguage(languageCode: String)
}
