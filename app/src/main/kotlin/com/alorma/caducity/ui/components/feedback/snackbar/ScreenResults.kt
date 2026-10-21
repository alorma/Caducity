package com.alorma.caducity.ui.components.feedback.snackbar

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * What a screen that closes leaves for the one underneath: the «Desfés» after a save or an action sheet.
 * The screen that becomes visible next shows it in its own scaffold; nothing is shown while no screen is.
 */
class ScreenResults {
  private val _pending = MutableStateFlow<SnackbarRequest?>(null)
  val pending: StateFlow<SnackbarRequest?> = _pending

  fun post(request: SnackbarRequest) {
    _pending.value = request
  }

  /** Takes the pending request, so only one screen shows it. */
  fun consume(): SnackbarRequest? {
    val request = _pending.value
    _pending.compareAndSet(request, null)
    return request
  }
}

val LocalScreenResults = staticCompositionLocalOf { ScreenResults() }

/** Shows the result left by the screen above, only while this screen is the visible one. */
@Composable
fun ScreenResultsEffect(snackbarState: AppSnackbarState) {
  val results = LocalScreenResults.current
  val resources = LocalResources.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(results, snackbarState, lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      results.pending.filterNotNull().collect {
        results.consume()?.let { request -> launch { snackbarState.show(request, resources) } }
      }
    }
  }
}

/** Shows [request] and runs its action if the user taps it. */
suspend fun AppSnackbarState.show(
  request: SnackbarRequest,
  resources: Resources,
) {
  val result =
    showSnackbar(
      message = resources.getString(request.message, *request.messageArgs.toTypedArray()),
      actionLabel = request.actionLabel?.let(resources::getString),
      type = request.type,
    )
  if (result == AppSnackbarResult.ActionPerformed) request.onAction?.invoke()
}
