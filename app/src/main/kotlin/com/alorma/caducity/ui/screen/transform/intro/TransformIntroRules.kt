package com.alorma.caducity.ui.screen.transform.intro

/**
 * When the «Novetat Transforma» modal may open over Home. It is held back (not lost: the flag stays pending) while the
 * onboarding is on screen, when the app was opened from a deep link, widget or notification, and while a dialog,
 * sheet or selection is open.
 */
fun shouldShowTransformIntro(
  introPending: Boolean,
  onboardingShowing: Boolean,
  openedFromOutside: Boolean,
  overlayOpen: Boolean,
): Boolean = introPending && !onboardingShowing && !openedFromOutside && !overlayOpen
