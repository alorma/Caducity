package com.alorma.caducity.feature.barcode

import de.tillhub.scanengine.ScanEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val barcodeModule = module {

  factory { ScanEngine.getInstance(androidContext()) }

  factoryOf(::BarcodeScannerContract)

  factoryOf(::AndroidBarcodeHandler) {
    bind<BarcodeHandler>()
  }
}