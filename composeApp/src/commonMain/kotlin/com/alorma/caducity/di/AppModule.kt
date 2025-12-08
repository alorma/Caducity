package com.alorma.caducity.di

import com.alorma.caducity.time.clock.AppClock
import org.koin.dsl.module

val appModule = module {
  single { AppClock() }
}
