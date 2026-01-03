package com.alorma.caducity.di

import com.alorma.fireandforget.FireAndForgetRunner
import com.alorma.fireandforget.multiplatform.settings.SettingsFireAndForgetRunner
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val fireAndForgetModule = module {
  singleOf(::SettingsFireAndForgetRunner) {
    bind<FireAndForgetRunner>()
  }
}
