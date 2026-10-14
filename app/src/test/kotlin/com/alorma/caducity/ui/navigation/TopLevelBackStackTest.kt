package com.alorma.caducity.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.domain.model.ExpiryStatus
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class TopLevelBackStackTest {
  private fun stackOf(root: AppRoute = Home) = TopLevelBackStack(mutableStateListOf<NavKey>(root))

  @Test
  fun `starts on home alone`() {
    val stack = stackOf()

    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.backStack).containsExactly(Home)
  }

  @Test
  fun `switching to a tab puts it on top of home`() {
    val stack = stackOf()

    stack.navigate(Pantry)

    expectThat(stack.currentTab).isEqualTo(Pantry)
    expectThat(stack.backStack).containsExactly(Home, Pantry)
  }

  @Test
  fun `switching to home leaves only home`() {
    val stack = stackOf()
    stack.navigate(Pantry)
    stack.navigate(Category("a"))

    stack.navigate(Home)

    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.backStack).containsExactly(Home)
  }

  @Test
  fun `tabs do not keep their history`() {
    val stack = stackOf()
    stack.navigate(Pantry)
    stack.navigate(Category("a"))
    stack.navigate(Home)
    stack.navigate(Pantry)

    expectThat(stack.backStack).containsExactly(Home, Pantry)
  }

  @Test
  fun `switching tab from a secondary route drops it`() {
    val stack = stackOf()
    stack.navigate(StatusList(ExpiryStatus.EXPIRED))

    stack.switchTab(Pantry)

    expectThat(stack.backStack).containsExactly(Home, Pantry)
  }

  @Test
  fun `secondary routes are pushed on top of the current tab`() {
    val stack = stackOf()
    stack.navigate(Pantry)
    stack.navigate(Category("a"))

    expectThat(stack.currentTab).isEqualTo(Pantry)
    expectThat(stack.backStack).containsExactly(Home, Pantry, Category("a"))
  }

  @Test
  fun `settings is a secondary route on the current tab`() {
    val stack = stackOf()
    stack.navigate(Settings)

    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.current).isEqualTo(Settings)
    expectThat(stack.pop()).isTrue()
    expectThat(stack.current).isEqualTo(Home)
  }

  @Test
  fun `pop removes the last entry`() {
    val stack = stackOf()
    stack.navigate(StatusList(ExpiryStatus.EXPIRED))

    expectThat(stack.pop()).isTrue()
    expectThat(stack.backStack).containsExactly(Home)
  }

  @Test
  fun `pop from a tab goes home and then belongs to the system`() {
    val stack = stackOf()
    stack.navigate(Pantry)

    expectThat(stack.pop()).isTrue()
    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.pop()).isFalse()
    expectThat(stack.backStack).containsExactly(Home)
  }

  @Test
  fun `onboarding is the base until reset`() {
    val stack = stackOf(root = Onboarding)

    expectThat(stack.current).isEqualTo(Onboarding)
    expectThat(stack.pop()).isFalse()

    stack.resetTo()

    expectThat(stack.backStack).containsExactly(Home)
  }

  @Test
  fun `openFromOutside lands on a clean home with the route on top`() {
    val stack = stackOf()
    stack.navigate(Pantry)
    stack.navigate(Category("a"))

    stack.openFromOutside(Category("b"))

    expectThat(stack.backStack).containsExactly(Home, Category("b"))
    expectThat(stack.currentTab).isEqualTo(Home)
  }

  @Test
  fun `openFromOutside to a tab leaves it on top of home`() {
    val stack = stackOf()
    stack.navigate(Settings)

    stack.openFromOutside(Pantry)

    expectThat(stack.backStack).containsExactly(Home, Pantry)
  }

  @Test
  fun `openFromOutside is ignored during onboarding`() {
    val stack = stackOf(root = Onboarding)

    stack.openFromOutside(StatusList(ExpiryStatus.EXPIRED))

    expectThat(stack.backStack).containsExactly(Onboarding)
  }
}
