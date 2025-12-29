package com.alorma.caducity.barcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerController
import org.ncgroup.kscan.ScannerView

class AndroidBarcodeHandler(
  private val context: Context,
) : BarcodeHandler {

  private val showScanner: MutableState<Boolean> = mutableStateOf(false)
  private var barcodeCallback: ((BarcodeModel) -> Unit)? = null
  private lateinit var permissionLauncher: ActivityResultLauncher<String>

  override fun hasBarcodeCapability(): Boolean = true

  override fun hasCameraPermission(): Boolean {
    return context.checkSelfPermission(Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
  }

  @Suppress("ModifierRequired")
  @Composable
  override fun registerPermissionContract() {
    permissionLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { granted ->
      if (granted) {
        // Permission granted, show scanner
        showScanner.value = true
      }
      // If denied, do nothing (scanner won't show)
    }
  }

  override fun scan(
    onBarcodeObtained: (BarcodeModel) -> Unit,
  ) {
    barcodeCallback = onBarcodeObtained

    // Check if we have permission
    if (hasCameraPermission()) {
      showScanner.value = true
    } else {
      // Request permission
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  @Suppress("ModifierDefaultValue")
  @Composable
  override fun Scanner(modifier: Modifier) {
    val callback = barcodeCallback
    if (showScanner.value && callback != null) {
      Dialog(
        onDismissRequest = { showScanner.value = false },
        properties = DialogProperties(
          usePlatformDefaultWidth = false,
        ),
      ) {
        val scannerController = remember { ScannerController() }

        BackHandler(showScanner.value) {
          showScanner.value = false
        }

        ScannerView(
          codeTypes = listOf(
            BarcodeFormats.FORMAT_QR_CODE,
            BarcodeFormats.FORMAT_EAN_13,
          ),
          scannerUiOptions = null,
          scannerController = scannerController,
        ) { result ->
          when (result) {
            is BarcodeResult.OnSuccess -> {
              callback(
                BarcodeModel(
                  data = result.barcode.data,
                  format = result.barcode.format,
                )
              )
              println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
              showScanner.value = false
            }

            is BarcodeResult.OnFailed -> {
              println("Error: ${result.exception.message}")
              showScanner.value = false
            }

            BarcodeResult.OnCanceled -> {
              showScanner.value = false
            }
          }
        }
      }
    }
  }
}