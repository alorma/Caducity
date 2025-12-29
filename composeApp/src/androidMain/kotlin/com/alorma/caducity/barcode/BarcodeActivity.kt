package com.alorma.caducity.barcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.launch

class BarcodeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val scanEngine = ScanEngine.getInstance(this)
    val scanner = scanEngine.newCameraScanner(this)

    scanner.startCameraScanner()

    lifecycleScope.launch {
      scanner.observeScannerResults().collect { scanEvent ->
        when (scanEvent) {
          is ScannerEvent.Camera.InProgress -> {}
          ScannerEvent.Camera.Canceled -> {}
          ScannerEvent.External.Connected -> {}
          is ScannerEvent.External.Connecting -> {}
          ScannerEvent.External.NotConnected -> {}
          is ScannerEvent.ScanResult -> {
            setResult(
              RESULT_OK,
              Intent().apply {
                putExtra(
                  BarcodeScannerContract.BarcodeKey,
                  scanEvent.value,
                )
              }
            )
            finish()
          }
        }
      }
    }
  }
}

class BarcodeScannerContract : ActivityResultContract<Unit, String?>() {
  override fun createIntent(
    context: Context,
    input: Unit
  ): Intent {
    return Intent(context, BarcodeActivity::class.java)
  }

  override fun parseResult(
    resultCode: Int,
    intent: Intent?
  ): String? {
    return intent?.getStringExtra(BarcodeKey)
  }

  companion object {
    const val BarcodeKey = "BarcodeKey"
  }
}