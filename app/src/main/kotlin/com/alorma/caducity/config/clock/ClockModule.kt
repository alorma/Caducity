package com.alorma.caducity.config.clock

import org.koin.dsl.module

val clockModule = module {
  single<AppClock> { KotlinAppClock() }
}