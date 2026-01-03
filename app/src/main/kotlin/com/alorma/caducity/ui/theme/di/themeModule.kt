package com.alorma.caducity.ui.theme.di

import com.alorma.caducity.ui.theme.ThemePreferences
import com.alorma.caducity.ui.theme.ThemePreferencesImpl
import com.alorma.caducity.ui.theme.colors.ExpirationColors
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val themeModule = module {
  singleOf(::ThemePreferencesImpl) {
    bind<ThemePreferences>()
  }

  singleOf(::ExpirationColors)
}