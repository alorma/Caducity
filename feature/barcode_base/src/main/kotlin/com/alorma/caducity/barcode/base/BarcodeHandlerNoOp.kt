package com.alorma.caducity.barcode.base

import androidx.compose.runtime.Composable

class BarcodeHandlerNoOp : BarcodeHandler {
  override fun hasBarcodeCapability(): Boolean = false

  override fun hasCameraPermission(): Boolean = false

  @Composable
  override fun registerPermissionContract() {

  }

  override suspend fun scan(onBarcodeObtained: (BarcodeModel) -> Unit) {

  }
}