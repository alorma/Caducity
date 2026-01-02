package com.alorma.caducity.config.language

import java.util.Locale

abstract class LanguageManager {

  abstract fun getLocale(): Locale

  abstract fun applyLocale(locale: Locale)

  abstract fun loadSupportedLocales(): List<Locale>
}