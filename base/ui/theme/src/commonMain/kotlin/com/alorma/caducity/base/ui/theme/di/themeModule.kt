package com.alorma.caducity.base.ui.theme.di

import com.alorma.caducity.base.ui.theme.ThemePreferences
import com.alorma.caducity.base.ui.theme.ThemePreferencesImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val themeModule = module {
  singleOf(::ThemePreferencesImpl) bind ThemePreferences::class
}