package com.alorma.caducity.barcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface BarcodeHandler {

  fun hasBarcodeCapability(): Boolean

  fun scan(onBarcodeObtained: (BarcodeModel) -> Unit)

  @Composable
  fun Scanner(modifier: Modifier = Modifier)

}

data class BarcodeModel(
  val data: String,
  val format: String,
)