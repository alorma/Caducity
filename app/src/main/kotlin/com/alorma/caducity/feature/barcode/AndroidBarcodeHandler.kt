package com.alorma.caducity.feature.barcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

class AndroidBarcodeHandler(
  private val context: Context,
  private val barcodeContract: BarcodeScannerContract,
) : BarcodeHandler {

  private var barcodeCallback: ((BarcodeModel) -> Unit)? = null

  private lateinit var barcodeLauncher: ActivityResultLauncher<Unit>
  private lateinit var permissionLauncher: ActivityResultLauncher<String>

  override fun hasBarcodeCapability(): Boolean = true

  override fun hasCameraPermission(): Boolean {
    return context.checkSelfPermission(Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
  }

  @Suppress("ModifierRequired")
  @Composable
  override fun registerPermissionContract() {
    barcodeLauncher = rememberLauncherForActivityResult(barcodeContract) { barcode ->
      val callback = barcodeCallback
      if (barcode != null && callback != null) {
        callback.invoke(
          BarcodeModel(
            data = barcode,
            format = null,
          )
        )
      }
    }

    permissionLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { granted ->
      if (granted) {
        barcodeLauncher.launch(Unit)
      }
    }
  }

  override suspend fun scan(
    onBarcodeObtained: (BarcodeModel) -> Unit,
  ) {
    barcodeCallback = onBarcodeObtained

    // Check if we have permission
    if (hasCameraPermission()) {
      barcodeLauncher.launch(Unit)
    } else {
      // Request permission
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }
}