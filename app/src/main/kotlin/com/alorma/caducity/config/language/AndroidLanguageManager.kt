package com.alorma.caducity.config.language

import android.app.LocaleConfig
import android.content.Context
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class AndroidLanguageManager(private val context: Context) : LanguageManager() {

  override fun getLocale(): Locale {
    val locales = AppCompatDelegate.getApplicationLocales()

    return if (!locales.isEmpty) {
      locales[0] ?: Locale.getDefault()
    } else {
      Locale.getDefault()
    }
  }

  override fun applyLocale(locale: Locale) {
    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(locale.language)
    AppCompatDelegate.setApplicationLocales(appLocale)
  }

  override fun loadSupportedLocales(): List<Locale> {
    val localeList = LocaleConfig(context).supportedLocales ?: LocaleList()

    return buildList {
      for (i in 0 until localeList.size()) {
        val locale = localeList.get(i)
        add(locale)
      }
      sortWith(compareBy { it.displayName })
    }

  }
}
