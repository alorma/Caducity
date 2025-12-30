package com.alorma.caducity.base.ui.theme.language

enum class Language(val code: String) {
  ENGLISH("en"),
  SPANISH("es"),
  CATALAN("ca");

  companion object {
    fun fromCode(code: String): Language? {
      return entries.firstOrNull { it.code == code }
    }

    fun fromSystemLanguage(): Language {
      val systemLanguage = java.util.Locale.getDefault().language
      return fromCode(systemLanguage) ?: ENGLISH
    }
  }
}
