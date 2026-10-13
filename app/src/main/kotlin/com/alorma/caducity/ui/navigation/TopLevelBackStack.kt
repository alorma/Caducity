package com.alorma.caducity.ui.navigation

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Back stack with one independent stack per tab ([Home], [Pantry]).
 *
 * Navigation 3 has no native tabs, so this keeps every tab stack alive and exposes them flattened in
 * [backStack] (current tab last) so each tab keeps its entries — and therefore scroll and search
 * state — while another tab is shown. Back from a tab root returns to [Home]; back from [Home] exits.
 *
 * @param initialRoot first route of the [Home] stack; [Onboarding] on first run.
 */
class TopLevelBackStack(
  initialRoot: AppRoute = Home,
) {
  private val stacks: Map<TopLevelRoute, SnapshotStateList<AppRoute>> =
    TABS.associateWith { tab ->
      mutableStateListOf(if (tab == Home) initialRoot else tab)
    }

  var currentTab: TopLevelRoute by mutableStateOf(Home)
    private set

  /** Entries to feed to `NavDisplay`: [Home] first, current tab last. */
  val backStack: List<AppRoute> by derivedStateOf {
    val others = TABS.filter { it != currentTab }
    val ordered = others.sortedBy { if (it == Home) 0 else 1 } + currentTab
    ordered.flatMap { stacks.getValue(it) }
  }

  val current: AppRoute get() = backStack.last()

  /** Navigates to a tab, or pushes a secondary route on the current tab. */
  fun navigate(route: AppRoute) {
    when (route) {
      is TopLevelRoute -> switchTab(route)
      else -> stacks.getValue(currentTab).add(route)
    }
  }

  fun switchTab(tab: TopLevelRoute) {
    currentTab = tab
  }

  /**
   * Pops the current route. Returns `false` when nothing is left to pop, so the system
   * can close the app.
   */
  fun pop(): Boolean {
    val stack = stacks.getValue(currentTab)
    return when {
      stack.size > 1 -> {
        stack.removeAt(stack.lastIndex)
        true
      }
      currentTab != Home -> {
        currentTab = Home
        true
      }
      else -> false
    }
  }

  /** Replaces the first route of [Home] (onboarding → home). Clears every tab's secondary routes. */
  fun resetTo(root: AppRoute = Home) {
    stacks.forEach { (tab, stack) ->
      stack.clear()
      stack.add(if (tab == Home) root else tab)
    }
    currentTab = Home
  }

  /** Opens [route] on top of a clean [Home] (notifications, shortcuts). */
  fun openFromOutside(route: AppRoute) {
    val home = stacks.getValue(Home)
    if (home.first() == Onboarding) return
    resetTo(home.first())
    if (route is TopLevelRoute) switchTab(route) else home.add(route)
  }

  companion object {
    val TABS: List<TopLevelRoute> = listOf(Home, Pantry)
  }
}
