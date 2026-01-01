package com.alorma.caducity.base.ui.theme.di

import com.alorma.caducity.base.ui.theme.ThemePreferences
import com.alorma.caducity.base.ui.theme.ThemePreferencesImpl
import com.alorma.caducity.base.ui.theme.colors.BaseExpirationColors
import com.alorma.caducity.base.ui.theme.colors.CaducityBaseExpirationColors
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val themeModule = module {
  singleOf(::ThemePreferencesImpl) {
    bind<ThemePreferences>()
  }

  singleOf(::CaducityBaseExpirationColors) {
    bind<BaseExpirationColors>()
  }
}