package com.alorma.caducity.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Marker sealed interface for ViewModels that don't have non-navigation side effects.
 *
 * Use this when your ViewModel only has navigation side effects and no other side effects
 * like dialogs, snackbars, or bottom sheets.
 *
 * Example:
 * ```kotlin
 * class DashboardViewModel : BaseViewModel<
 *   DashboardNavigation,
 *   DashboardNavigationSideEffect,
 *   NoSideEffect  // No dialogs, snackbars, etc.
 * >()
 * ```
 */
sealed interface NoSideEffect

/**
 * Marker sealed interface for ViewModels that don't have navigation.
 *
 * Use this when your ViewModel only has side effects (dialogs, snackbars, callbacks)
 * but doesn't navigate to other screens. Typically used for bottom sheets, dialogs,
 * or utility ViewModels.
 *
 * When using NoNavigation, you must still implement the navigate() method,
 * but it will never be called. You can provide an empty implementation.
 *
 * Example:
 * ```kotlin
 * class ItemActionsViewModel : BaseViewModel<
 *   NoNavigation,  // No screen navigation
 *   ItemActionSideEffect,
 *   ItemActionSideEffect
 * >() {
 *   override fun navigate(navigation: NoNavigation) {
 *     // Empty - this ViewModel doesn't navigate
 *   }
 * }
 * ```
 */
sealed interface NoNavigation

/**
 * Base ViewModel that provides common navigation and side effect patterns.
 *
 * This base class encapsulates the channel-based side effect and navigation pattern,
 * reducing boilerplate in concrete ViewModels.
 *
 * @param NavigationIntent The sealed interface representing user navigation intents
 * @param NavigationSideEffect The sealed interface representing navigation side effects to be handled by the UI
 * @param SideEffect The sealed interface representing non-navigation side effects (dialogs, snackbars, etc.)
 *
 * Example usage:
 * ```kotlin
 * class DashboardViewModel(
 *   private val eventTracker: EventTracker,
 *   // ... other dependencies
 * ) : BaseViewModel<DashboardNavigation, DashboardNavigationSideEffect, DashboardSideEffect>() {
 *
 *   val state: StateFlow<DashboardState> = ...
 *
 *   override fun navigate(navigation: DashboardNavigation) {
 *     when (navigation) {
 *       DashboardNavigation.CreateCategory -> {
 *         eventTracker.trackAction(NavigateToCreateCategoryAction())
 *         emitNavigationSideEffect(DashboardNavigationSideEffect.NavigateToCreateCategory)
 *       }
 *       // ... other navigation cases
 *     }
 *   }
 *
 *   fun onShowDialog() {
 *     emitSideEffect(DashboardSideEffect.ShowDialog)
 *   }
 * }
 * ```
 */
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
