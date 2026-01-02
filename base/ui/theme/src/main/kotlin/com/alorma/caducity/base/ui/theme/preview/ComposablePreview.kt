package com.alorma.caducity.base.ui.theme.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.alorma.caducity.base.main.clock.clockModule
import com.alorma.caducity.base.ui.theme.AppTheme
import com.alorma.caducity.base.ui.theme.colors.ExpirationColorSchemeType
import com.alorma.caducity.base.ui.theme.LocalSystemBarsAppearance
import com.alorma.caducity.base.ui.theme.SystemBarsAppearanceNoOp
import com.alorma.caducity.base.ui.theme.ThemeMode
import com.alorma.caducity.base.ui.theme.ThemePreferences
import com.alorma.caducity.base.ui.theme.colors.BaseExpirationColors
import com.alorma.caducity.base.ui.theme.colors.CaducityBaseExpirationColors
import org.koin.compose.KoinApplicationPreview
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

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
  includes(clockModule)
  single {
    object : ThemePreferences {
      override val themeMode: MutableState<ThemeMode> = mutableStateOf(ThemeMode.SYSTEM)
      override val useDynamicColors: MutableState<Boolean> = mutableStateOf(false)
      override val expirationColorSchemeType: MutableState<ExpirationColorSchemeType> =
        mutableStateOf(
          ExpirationColorSchemeType.VIBRANT,
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

  singleOf(::CaducityBaseExpirationColors) {
    bind<BaseExpirationColors>()
  }
}