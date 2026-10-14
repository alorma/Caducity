package com.alorma.caducity.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Single back stack shared by every tab, with the first route ([Home], or [Onboarding] on first run)
 * as its base.
 *
 * Tabs do not keep history: going to [Pantry] leaves `[Home, Pantry]` and going to [Home] leaves
 * `[Home]`, so the tab you leave loses its state (scroll, search, screens opened on top). Secondary
 * routes are pushed on top (`[Home, Pantry, Category]`). Back removes the last entry; with only the
 * base left it belongs to the system, which closes the app with its own animation.
 *
 * @param entries the saveable stack fed to `NavDisplay` (`rememberNavBackStack` in the app). It must
 *   start with the base route.
 */
class TopLevelBackStack(
  private val entries: MutableList<NavKey>,
) {
  init {
    require(entries.isNotEmpty()) { "The back stack needs a base route" }
  }

  /** Entries to feed to `NavDisplay`: base first, current screen last. */
  val backStack: List<NavKey> get() = entries

  /** Last tab of the stack; [Home] while only the base (or onboarding) is there. */
  val currentTab: TopLevelRoute
    get() = entries.lastOrNull { it is TopLevelRoute } as? TopLevelRoute ?: Home

  val current: NavKey get() = entries.last()

  /** Navigates to a tab, or pushes a secondary route on top. */
  fun navigate(route: AppRoute) {
    when (route) {
      is TopLevelRoute -> switchTab(route)
      else -> entries.add(route)
    }
  }

  /** Leaves `[base]` for [Home] or `[base, tab]` for any other tab, dropping everything else. */
  fun switchTab(tab: TopLevelRoute) {
    while (entries.size > 1) entries.removeAt(entries.lastIndex)
    if (entries.first() != tab) entries.add(tab)
  }

  /**
   * Pops the current route. Returns `false` when only the base is left, so the system
   * can close the app.
   */
  fun pop(): Boolean {
    if (entries.size <= 1) return false
    entries.removeAt(entries.lastIndex)
    return true
  }

  /** Leaves [root] as the only route (onboarding → home). */
  fun resetTo(root: AppRoute = Home) {
    entries.add(root)
    while (entries.size > 1) entries.removeAt(0)
  }

  /** Opens [route] on top of a clean [Home] (notifications, shortcuts). */
  fun openFromOutside(route: AppRoute) {
    if (entries.first() == Onboarding) return
    resetTo(Home)
    navigate(route)
  }
}
