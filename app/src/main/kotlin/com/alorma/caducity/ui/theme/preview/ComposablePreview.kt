package com.alorma.caducity.ui.theme.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.clock.KotlinAppClock
import com.alorma.caducity.config.language.LanguageManager
import com.alorma.caducity.ui.theme.AppTheme
import com.alorma.caducity.ui.theme.LocalSystemBarsAppearance
import com.alorma.caducity.ui.theme.SystemBarsAppearanceNoOp
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
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
      AppTheme { block() }
    }
  }
}

val themePreviewModule = module {
  single<AppClock> { KotlinAppClock() }

  single {
    object : ThemePreferences {
      override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
      override val useDynamicColors: MutableState<Boolean> = mutableStateOf(true)

      override fun loadThemeMode(): ThemeMode {
        return themeMode.value
      }

      override fun loadUseDynamicColors(): Boolean {
        return useDynamicColors.value
      }

      override fun setThemeModeState(mode: ThemeMode) {

      }

      override fun setDynamicColorsEnabled(enabled: Boolean) {

      }
    }
  } bind ThemePreferences::class

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