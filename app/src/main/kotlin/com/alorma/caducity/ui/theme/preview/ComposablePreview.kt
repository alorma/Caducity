package com.alorma.caducity.ui.theme.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.clock.KotlinAppClock
import com.alorma.caducity.config.language.LanguageManager
import com.alorma.caducity.ui.theme.AppTheme
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance
import com.alorma.caducity.ui.theme.SystemBarsAppearanceNoOp
import com.alorma.caducity.ui.theme.ThemePreferencesNoOp
import org.koin.compose.KoinApplicationPreview
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.Locale

@Suppress("ModifierRequired")
@Composable
fun AppPreview(
  module: Module = module { },
  block: @Composable () -> Unit,
) {
  KoinApplicationPreview(application = {
    modules(
      themePreviewModule,
      module,
    )
  }) {
    CompositionLocalProvider(
      LocalSystemBarsAppearance provides SystemBarsAppearanceNoOp,
    ) {
      AppTheme(themePreferences = ThemePreferencesNoOp) { block() }
    }
  }
}

val themePreviewModule = module {
  single<AppClock> { KotlinAppClock() }

  single {
    object : LanguageManager() {
      override fun getLocale(): Locale {
        return Locale.ENGLISH
      }

      override fun applyLocale(locale: Locale) {

      }

      override fun loadSupportedLocales(): List<Locale> {
        return emptyList()
      }

    }
  } bind LanguageManager::class

}