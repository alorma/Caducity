package com.alorma.caducity.base.ui.theme.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alorma.caducity.base.ui.theme.AppTheme
import com.alorma.caducity.base.ui.theme.ExpirationColorSchemeType
import com.alorma.caducity.base.ui.theme.ThemeMode
import com.alorma.caducity.base.ui.theme.ThemePreferences
import org.koin.compose.KoinApplicationPreview
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("ModifierRequired")
@Composable
fun AppPreview(
  block: @Composable () -> Unit,
) {
  KoinApplicationPreview(application = { modules(themePreviewModule) }) {
    AppTheme { block() }
  }
}

val themePreviewModule = module {
  single {
    object : ThemePreferences {
      override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
      override val useDynamicColors: MutableState<Boolean> = mutableStateOf(false)
      override val expirationColorSchemeType: MutableState<ExpirationColorSchemeType> =
        mutableStateOf(
          ExpirationColorSchemeType.HARMONIZE,
        )

      override fun loadThemeMode(): ThemeMode {
        return themeMode.value
      }

      override fun loadUseDynamicColors(): Boolean {
        return useDynamicColors.value
      }

      override fun loadExpirationColorSchemeType(): ExpirationColorSchemeType {
        return expirationColorSchemeType.value
      }

      override fun setThemeModeState(mode: ThemeMode) {

      }

      override fun setDynamicColorsEnabled(enabled: Boolean) {

      }

      override fun setExpirationColorSchemeType(type: ExpirationColorSchemeType) {

      }

    }
  } bind ThemePreferences::class
}