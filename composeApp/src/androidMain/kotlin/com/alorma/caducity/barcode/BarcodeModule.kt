package com.alorma.caducity.barcode

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val barcodeModule = module {
  factoryOf(::AndroidBarcodeHandler) {
    bind<BarcodeHandler>()
  }
}