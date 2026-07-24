package com.alorma.caducity.feature.ai

import java.util.Locale

/**
 * Supplies the current UI language so the assistant can prompt the on-device
 * model to answer in the user's language instead of always in English.
 */
interface LanguageProvider {
  /**
   * The current language name expressed in English (e.g. "Spanish", "Catalan").
   * English is used because it is the language the model understands best for
   * instructions, while still steering its output language.
   */
  fun currentLanguageName(): String
}

class LocaleLanguageProvider : LanguageProvider {
  override fun currentLanguageName(): String =
    Locale.getDefault().getDisplayLanguage(Locale.ENGLISH).ifBlank { "English" }
}
