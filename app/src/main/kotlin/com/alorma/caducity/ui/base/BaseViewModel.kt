package com.alorma.caducity.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<NavigationIntent, NavigationSideEffect, SideEffect> : ViewModel() {
  private val navigationSideEffectChannel = Channel<NavigationSideEffect>(Channel.BUFFERED)
  val navigationSideEffects: Flow<NavigationSideEffect> = navigationSideEffectChannel.receiveAsFlow()

  private val sideEffectChannel = Channel<SideEffect>(Channel.BUFFERED)
  val sideEffects: Flow<SideEffect> = sideEffectChannel.receiveAsFlow()

  /**
   * Handle navigation intent.
   *
   * Implementations should:
   * 1. Track the action via EventTracker
   * 2. Emit the appropriate navigation side effect via emitNavigationSideEffect()
   *
   * Example:
   * ```kotlin
   * override fun navigate(navigation: DashboardNavigation) {
   *   when (navigation) {
   *     DashboardNavigation.Settings -> {
   *       eventTracker.trackAction(NavigateToSettingsAction())
   *       emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToSettings)
   *     }
   *   }
   * }
   * ```
   */
  abstract fun navigate(navigation: NavigationIntent)

  /**
   * Emit a navigation side effect.
   *
   * Navigation side effects should be collected in the UI layer (Screen composable)
   * to trigger actual navigation actions.
   */
  protected fun emitNavigationSideEffect(effect: NavigationSideEffect) {
    viewModelScope.launch {
      navigationSideEffectChannel.send(effect)
    }
  }

  /**
   * Emit a non-navigation side effect.
   *
   * Side effects like showing dialogs, snackbars, or bottom sheets should be
   * collected in the UI layer to trigger UI feedback.
   */
  protected fun emitSideEffect(effect: SideEffect) {
    viewModelScope.launch {
      sideEffectChannel.send(effect)
    }
  }
}
