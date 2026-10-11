package com.alorma.caducity.ui.navigation

import com.alorma.caducity.domain.model.ExpiryStatus
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class TopLevelBackStackTest {
  @Test
  fun `starts on home`() {
    val stack = TopLevelBackStack()

    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.backStack).containsExactly(Pantry, Home)
  }

  @Test
  fun `switching tab shows its stack last and keeps the others alive`() {
    val stack = TopLevelBackStack()

    stack.navigate(Pantry)

    expectThat(stack.currentTab).isEqualTo(Pantry)
    expectThat(stack.current).isEqualTo(Pantry)
    expectThat(stack.backStack.toSet()).isEqualTo(setOf(Home, Pantry))
  }

  @Test
  fun `secondary routes are pushed on the current tab and survive tab switches`() {
    val stack = TopLevelBackStack()
    stack.navigate(Pantry)
    stack.navigate(Category("a"))
    stack.navigate(Home)
    stack.navigate(Pantry)

    expectThat(stack.current).isEqualTo(Category("a"))
  }

  @Test
  fun `settings is a secondary route on the current tab`() {
    val stack = TopLevelBackStack()
    stack.navigate(Settings)

    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.current).isEqualTo(Settings)
    expectThat(stack.pop()).isTrue()
    expectThat(stack.current).isEqualTo(Home)
  }

  @Test
  fun `pop removes secondary route first`() {
    val stack = TopLevelBackStack()
    stack.navigate(StatusList(ExpiryStatus.EXPIRED))

    expectThat(stack.pop()).isTrue()
    expectThat(stack.current).isEqualTo(Home)
  }

  @Test
  fun `pop from a tab root goes home and then exits`() {
    val stack = TopLevelBackStack()
    stack.navigate(Pantry)

    expectThat(stack.pop()).isTrue()
    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.pop()).isFalse()
  }

  @Test
  fun `onboarding is the home root until reset`() {
    val stack = TopLevelBackStack(initialRoot = Onboarding)

    expectThat(stack.current).isEqualTo(Onboarding)

    stack.resetTo()

    expectThat(stack.current).isEqualTo(Home)
  }

  @Test
  fun `openFromOutside lands on a clean home with the route on top`() {
    val stack = TopLevelBackStack()
    stack.navigate(Pantry)
    stack.navigate(Category("a"))

    stack.openFromOutside(Category("b"))

    expectThat(stack.currentTab).isEqualTo(Home)
    expectThat(stack.current).isEqualTo(Category("b"))
    stack.pop()
    expectThat(stack.current).isEqualTo(Home)
  }

  @Test
  fun `openFromOutside is ignored during onboarding`() {
    val stack = TopLevelBackStack(initialRoot = Onboarding)

    stack.openFromOutside(StatusList(ExpiryStatus.EXPIRED))

    expectThat(stack.current).isEqualTo(Onboarding)
  }
}
