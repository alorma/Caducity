package com.alorma.caducity.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Genera el Baseline Profile de l'app: arrencada, scroll de Home i navegació entre les pestanyes.
 *
 * Necessita l'app amb dades (Home amb productes) i la interfície en anglès: les pestanyes es troben per la seva
 * descripció («Pantry», «Settings»). Es llança amb `./gradlew :app:generateReleaseBaselineProfile`.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
  @get:Rule
  val rule = BaselineProfileRule()

  @Test
  fun generate() =
    rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
      pressHome()
      startActivityAndWait()

      // Home: espera que es pintin les dades i fa scroll
      device.wait(Until.hasObject(By.scrollable(true)), UI_TIMEOUT)
      device.findObject(By.scrollable(true))?.apply {
        setGestureMargin(device.displayWidth / 5)
        fling(Direction.DOWN)
        fling(Direction.UP)
      }

      // Pantry
      device.findObject(By.desc("Pantry"))?.click()
      device.waitForIdle()
      device.findObject(By.scrollable(true))?.apply {
        setGestureMargin(device.displayWidth / 5)
        fling(Direction.DOWN)
        fling(Direction.UP)
      }

      // Settings
      device.findObject(By.desc("Settings"))?.click()
      device.waitForIdle()
    }

  private companion object {
    const val PACKAGE_NAME = "com.alorma.caducity"
    const val UI_TIMEOUT = 5_000L
  }
}
