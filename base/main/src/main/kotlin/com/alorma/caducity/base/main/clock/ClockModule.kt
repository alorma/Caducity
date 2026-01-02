package com.alorma.caducity.base.main.clock

import org.koin.dsl.module

val clockModule = module {
  single<AppClock> { KotlinAppClock() }
}