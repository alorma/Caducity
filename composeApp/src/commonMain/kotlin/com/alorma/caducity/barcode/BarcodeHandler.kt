package com.alorma.caducity.barcode

import androidx.compose.runtime.Composable

interface BarcodeHandler {

  fun hasBarcodeCapability(): Boolean

  /**
   * Checks if the app has camera permission.
   * @return true if camera permission is granted by the system
   */
  fun hasCameraPermission(): Boolean

  /**
   * Registers the permission launcher contract.
   * Must be called in a Composable context before calling scan().
   */
  @Composable
  fun registerPermissionContract()

  /**
   * Requests camera permission and initiates scanning after permission is granted.
   * @param onBarcodeObtained Callback invoked when a barcode is successfully scanned
   */
  suspend fun scan(onBarcodeObtained: (BarcodeModel) -> Unit)
}

data class BarcodeModel(
  val data: String,
  val format: String?,
)