package com.alorma.caducity.barcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
  fun scan(onBarcodeObtained: (BarcodeModel) -> Unit)

  @Composable
  fun Scanner(modifier: Modifier = Modifier)

}

data class BarcodeModel(
  val data: String,
  val format: String,
)