package com.alorma.caducity.di

import com.alorma.caducity.time.clock.AppClock
import com.alorma.caducity.time.clock.KotlinAppClock
import org.koin.dsl.module

val appModule = module {
  single<AppClock> { KotlinAppClock() }
}
