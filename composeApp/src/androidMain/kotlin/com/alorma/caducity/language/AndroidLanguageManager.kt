package com.alorma.caducity.language

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import com.alorma.caducity.base.ui.theme.LanguageManager
import com.russhwolf.settings.Settings
import java.util.Locale

class AndroidLanguageManager(
  settings: Settings,
  private val context: Context,
) : LanguageManager(settings) {
  override fun getPlatformLanguageCode(): String? {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (!locales.isEmpty) {
      locales[0]?.language
    } else {
      null
    }
  }

  override fun applyLanguage(languageCode: String) {
    // Use system LocaleManager on Android 13+ for per-app language without recreation
    val locale = Locale.forLanguageTag(languageCode)
    context.getSystemService(LocaleManager::class.java).applicationLocales =
      LocaleList(locale)
  }
}
