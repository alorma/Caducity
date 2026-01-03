package com.alorma.caducity.ui.components.feedback.snackbar

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.vibrantColors
import com.alorma.caducity.ui.components.feedback.exposeResource
import com.alorma.caducity.ui.components.feedback.softColors
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@Composable
fun rememberAppSnackbarHostState(): AppSnackbarHostState = remember {
  AppSnackbarHostState()
}

val LocalAppSnackbarHostState = compositionLocalOf<AppSnackbarHostState> {
  throw (IllegalStateException("Should be provided from a AppSnackbarHostState"))
}

/**
 * State of the [AppSnackbarHost], controls the queue and the current [AppSnackbar] being shown inside
 * the [AppSnackbarHost].
 *
 * This state usually lives as a part of a [ScaffoldState] and provided to the [AppSnackbarHost]
 * automatically, but can be decoupled from it and live separately when desired.
 */
@Stable
class AppSnackbarHostState {

  /**
   * Only one [AppSnackbar] can be shown at a time.
   * Since a suspending Mutex is a fair queue, this manages our message queue
   * and we don't have to maintain one.
   */
  private val mutex = Mutex()

  /**
   * The current [AppSnackbarData] being shown by the [AppSnackbarHost], of `null` if none.
   */
  var currentAppSnackbarData by mutableStateOf<AppSnackbarData?>(null)
    private set

  /**
   * Shows or queues to be shown a [AppSnackbar] at the bottom of the [IJScaffold] at
   * which this state is attached and suspends until AppSnackbar is disappeared.
   *
   * [AppSnackbarHostState] guarantees to show at most one AppSnackbar at a time. If this function is
   * called while another AppSnackbar is already visible, it will be suspended until this snack
   * bar is shown and subsequently addressed. If the caller is cancelled, the AppSnackbar will be
   * removed from display and/or the queue to be displayed.
   *
   * All of this allows for granular control over the AppSnackbar queue from within:
   *
   * To change the AppSnackbar appearance, change it in 'AppSnackbarHost' on the [IJScaffold].
   *
   * @param message text to be shown in the AppSnackbar
   * @param actionLabel optional action label to show as button in the AppSnackbar
   * @param duration duration to control how long AppSnackbar will be shown in [AppSnackbarHost], either
   * [SnackbarDuration.Short], [SnackbarDuration.Long] or [SnackbarDuration.Indefinite]
   *
   * @return [AppSnackbarResult.ActionPerformed] if option action has been clicked or
   * [AppSnackbarResult.Dismissed] if AppSnackbar has been dismissed via timeout or by the user
   */
  suspend fun showSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    type: AppFeedbackType = AppFeedbackType.Info,
    layout: AppSnackbarLayout = AppSnackbarLayout.ActionLengthBased,
  ): AppSnackbarResult = mutex.withLock {
    try {
      return suspendCancellableCoroutine { continuation ->
        currentAppSnackbarData = AppSnackbarDataImpl(
          message = AppFeedbackResource.AsString(message),
          actionLabel = actionLabel?.let { AppFeedbackResource.AsString(it) },
          duration = duration,
          type = type,
          continuation = continuation,
          layout = layout
        )
      }
    } finally {
      currentAppSnackbarData = null
    }
  }

  suspend fun showSnackbar(
    @StringRes message: Int,
    @StringRes actionLabel: Int? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    type: AppFeedbackType = AppFeedbackType.Info,
    layout: AppSnackbarLayout = AppSnackbarLayout.ActionLengthBased,
  ): AppSnackbarResult = mutex.withLock {
    try {
      return suspendCancellableCoroutine { continuation ->
        currentAppSnackbarData = AppSnackbarDataImpl(
          message = AppFeedbackResource.AsResource(message),
          actionLabel = actionLabel?.let { AppFeedbackResource.AsResource(it) },
          duration = duration,
          type = type,
          continuation = continuation,
          layout = layout
        )
      }
    } finally {
      currentAppSnackbarData = null
    }
  }

  @Stable
  private class AppSnackbarDataImpl(
    override val message: AppFeedbackResource,
    override val actionLabel: AppFeedbackResource?,
    override val duration: SnackbarDuration,
    override val type: AppFeedbackType,
    private val continuation: CancellableContinuation<AppSnackbarResult>,
    override val layout: AppSnackbarLayout = AppSnackbarLayout.ActionLengthBased
  ) : AppSnackbarData {

    override fun performAction() {
      if (continuation.isActive) continuation.resume(AppSnackbarResult.ActionPerformed)
    }

    override fun dismiss() {
      if (continuation.isActive) continuation.resume(AppSnackbarResult.Dismissed)
    }
  }
}

/**
 * Host for [AppSnackbar]s to be used in [Scaffold] to properly show, hide and dismiss items based
 * on material specification and the [hostState].
 *
 * This component with default parameters comes build-in with [Scaffold], if you need to show a
 * default [AppSnackbar], use use [AppSnackbarHostState] and
 * [AppSnackbarHostState.showSnackbar].
 *

 * If you want to customize appearance of the [AppSnackbar], you can pass your own version as a child
 * of the [AppSnackbarHost] to the [Scaffold]:
 *
 * @param hostState state of this component to read and show [AppSnackbar]s accordingly
 * @param modifier optional modifier for this component
 * @param ijSnackbar the instance of the [AppSnackbar] to be shown at the appropriate time with
 * appearance based on the [AppSnackbarData] provided as a param
 */
@Composable
fun AppSnackbarHost(
  hostState: AppSnackbarHostState,
  modifier: Modifier = Modifier,
  ijSnackbar: @Composable (AppSnackbarData) -> Unit = { AppSnackbar(it) },
) {
  val currentAppSnackbarData = hostState.currentAppSnackbarData
  val accessibilityManager = LocalAccessibilityManager.current
  LaunchedEffect(currentAppSnackbarData) {
    if (currentAppSnackbarData != null) {
      val duration = currentAppSnackbarData.duration.toMillis(
        currentAppSnackbarData.actionLabel != null,
        accessibilityManager
      )
      delay(duration)
      currentAppSnackbarData.dismiss()
    }
  }

  FadeInFadeOutWithScale(
    current = hostState.currentAppSnackbarData,
    modifier = modifier,
    content = ijSnackbar
  )
}

@Composable
fun AppSnackbar(
  snackbarData: AppSnackbarData,
  modifier: Modifier = Modifier,
) {
  // Expose resources at composable call site
  val messageText = exposeResource(snackbarData.message).toString()
  val actionLabelText = snackbarData.actionLabel?.let { exposeResource(it).toString() }

  val actionLength = actionLabelText?.length ?: 0

  val actionOnNewLine = when (snackbarData.layout) {
    AppSnackbarLayout.ActionLengthBased -> actionLength >= AppSnackbarDefaults.MaxSizeToDisplayActionOnNewLine
    AppSnackbarLayout.SideAction -> false
    AppSnackbarLayout.StackedAction -> true
  }

  val colors = snackbarData.type.vibrantColors()

  Snackbar(
    modifier = modifier,
    snackbarData = object : SnackbarData {
      override fun dismiss() {
        snackbarData.dismiss()
      }

      override val visuals: SnackbarVisuals = object : SnackbarVisuals {
        override val message: String = messageText
        override val actionLabel: String? = actionLabelText
        override val duration: SnackbarDuration = snackbarData.duration
        override val withDismissAction: Boolean = false
      }

      override fun performAction() {
        snackbarData.performAction()
      }
    },
    actionOnNewLine = actionOnNewLine,
    containerColor = colors.container,
    contentColor = colors.onContainer,
    actionColor = colors.onContainer,
  )
}

/**
 * Interface to represent one particular [AppSnackbar] as a piece of the [AppSnackbarHostState]
 *
 * @property message text to be shown in the [AppSnackbar]
 * @property actionLabel optional action label to show as button in the AppSnackbar
 * @property duration duration of the AppSnackbar
 * @property type type of the AppSnackbar
 */
interface AppSnackbarData {
  val message: AppFeedbackResource
  val actionLabel: AppFeedbackResource?
  val duration: SnackbarDuration
  val type: AppFeedbackType
  val layout: AppSnackbarLayout

  /**
   * Function to be called when AppSnackbar action has been performed to notify the listeners
   */
  fun performAction()

  /**
   * Function to be called when AppSnackbar is dismissed either by timeout or by the user
   */
  fun dismiss()
}


/**
 * Possible results of the [AppSnackbarHostState.showSnackbar] call
 */
enum class AppSnackbarResult {
  /**
   * [AppSnackbar] that is shown has been dismissed either by timeout of by user
   */
  Dismissed,

  /**
   * Action on the [AppSnackbar] has been clicked before the time out passed
   */
  ActionPerformed,
}

enum class AppSnackbarLayout {
  ActionLengthBased,
  SideAction,
  StackedAction,
}

// it's basically tweaked nullable version of Crossfade
@Composable
private fun FadeInFadeOutWithScale(
  current: AppSnackbarData?,
  modifier: Modifier = Modifier,
  content: @Composable (AppSnackbarData) -> Unit,
) {
  val state = remember { FadeInFadeOutState<AppSnackbarData?>() }
  if (current != state.current) {
    state.current = current
    val keys = remember { state.items.map { it.key }.toMutableList() }
    if (!keys.contains(current)) {
      keys.add(current)
    }
    state.items.clear()
    keys.filterNotNull().mapTo(state.items) { key ->
      FadeInFadeOutAnimationItem(key) { children ->
        val isVisible = key == current
        val duration = if (isVisible) AppSnackbarFadeInMillis else AppSnackbarFadeOutMillis
        val delay = AppSnackbarFadeOutMillis + AppSnackbarInBetweenDelayMillis
        val animationDelay = remember {
          if (isVisible && keys.filterNotNull().size != 1) {
            delay
          } else {
            0
          }
        }
        val opacity = animatedOpacity(
          animation = tween(
            easing = LinearEasing,
            delayMillis = animationDelay,
            durationMillis = duration
          ),
          visible = isVisible,
          onAnimationFinish = {
            if (key != state.current) {
              // leave only the current in the list
              state.items.removeAll { it.key == key }
              state.scope?.invalidate()
            }
          }
        )
        val scale = animatedScale(
          animation = tween(
            easing = FastOutSlowInEasing,
            delayMillis = animationDelay,
            durationMillis = duration
          ),
          visible = isVisible
        )
        Box(
          Modifier
            .graphicsLayer(
              scaleX = scale.value,
              scaleY = scale.value,
              alpha = opacity.value
            )
            .semantics {
              liveRegion = LiveRegionMode.Polite
              dismiss { key.dismiss(); true }
            }
        ) {
          children()
        }
      }
    }
  }
  Box(modifier) {
    state.scope = currentRecomposeScope
    state.items.forEach { (item, opacity) ->
      key(item) {
        opacity {
          content(item!!)
        }
      }
    }
  }
}

private class FadeInFadeOutState<T> {
  // we use Any here as something which will not be equals to the real initial value
  var current: Any? = Any()
  var items = mutableListOf<FadeInFadeOutAnimationItem<T>>()
  var scope: RecomposeScope? = null
}

private data class FadeInFadeOutAnimationItem<T>(
  val key: T,
  val transition: FadeInFadeOutTransition,
)

private typealias FadeInFadeOutTransition = @Composable (content: @Composable () -> Unit) -> Unit

@Composable
private fun animatedOpacity(
  animation: AnimationSpec<Float>,
  visible: Boolean,
  onAnimationFinish: () -> Unit = {},
): State<Float> {
  val alpha = remember { Animatable(if (!visible) 1f else 0f) }
  LaunchedEffect(visible, onAnimationFinish) {
    alpha.animateTo(
      if (visible) 1f else 0f,
      animationSpec = animation
    )
    onAnimationFinish()
  }
  return alpha.asState()
}

@Composable
private fun animatedScale(animation: AnimationSpec<Float>, visible: Boolean): State<Float> {
  val scale = remember { Animatable(if (!visible) 1f else 0.8f) }
  LaunchedEffect(visible) {
    scale.animateTo(
      if (visible) 1f else 0.8f,
      animationSpec = animation
    )
  }
  return scale.asState()
}

private const val AppSnackbarFadeInMillis = 150
private const val AppSnackbarFadeOutMillis = 75
private const val AppSnackbarInBetweenDelayMillis = 0

fun SnackbarDuration.toMillis(
  hasAction: Boolean,
  accessibilityManager: AccessibilityManager?,
): Long {
  val original = when (this) {
    SnackbarDuration.Indefinite -> Long.MAX_VALUE
    SnackbarDuration.Long -> 10000L
    SnackbarDuration.Short -> 4000L
  }
  if (accessibilityManager == null) {
    return original
  }
  return accessibilityManager.calculateRecommendedTimeoutMillis(
    original,
    containsIcons = true,
    containsText = true,
    containsControls = hasAction
  )
}
